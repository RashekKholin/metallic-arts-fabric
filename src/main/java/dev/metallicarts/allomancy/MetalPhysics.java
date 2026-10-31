package dev.metallicarts.allomancy;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.economy.EconomyContent;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

/** Bounded, loaded-only aim detection. Physics mass is deliberately separate from credited fuel mass. */
public final class MetalPhysics {
    public static final TagKey<Block> ANCHORS=TagKey.create(Registries.BLOCK,MetallicArts.id("metal_anchors"));
    public static final TagKey<net.minecraft.world.item.Item> ITEMS=TagKey.create(Registries.ITEM,MetallicArts.id("external_metals"));
    public record Target(Vec3 position,Entity entity,double mass,boolean anchored) {}
    public static boolean external(ItemStack stack) {
        if(stack.isEmpty())return false;
        var form=EconomyContent.form(stack.getItem());
        // Invested items resist ordinary forces; alloys retain metal behavior except pure aluminum.
        var data=stack.get(ModRegistries.METAL_DATA);
        if(data!=null&&(data.charge()>0||data.identity().isPresent()||data.metal()==dev.metallicarts.domain.Metal.ALUMINUM))return false;
        return form!=null?!Set.of("aluminum","carbon","silicon").contains(form.material()):stack.is(ITEMS);
    }
    public static Optional<Target> aimed(ServerPlayer player,AllomancyConfig config) {
        Vec3 eye=player.getEyePosition(),look=player.getLookAngle();
        var level=player.level();
        Target best=null;double bestDistance=config.detectionRange()+1;
        // At most 64 block probes. Non-metal blocks do not hide an external metal anchor.
        for(double d=0.5;d<=config.detectionRange();d+=0.5) {
            Vec3 point=eye.add(look.scale(d));BlockPos pos=BlockPos.containing(point);
            if(!level.hasChunkAt(pos))continue;
            if(level.getBlockState(pos).is(ANCHORS)){best=new Target(Vec3.atCenterOf(pos),null,Double.POSITIVE_INFINITY,true);bestDistance=d;break;}
        }
        // Entity query is limited by the loaded spatial index and range, then examined up to the configured cap.
        var entities=level.getEntities(player,player.getBoundingBox().inflate(config.detectionRange()),e->e instanceof ItemEntity||e instanceof LivingEntity||e instanceof CoinProjectile);
        int checked=0;
        for(var entity:entities) {
            if(checked++>=config.maximumTargets())break;
            Vec3 position=entity.getBoundingBox().getCenter(),relative=position.subtract(eye);
            double along=relative.dot(look),distance=relative.length();
            if(along<0||distance>config.detectionRange()||distance>=bestDistance||relative.subtract(look.scale(along)).length()>0.8)continue;
            boolean metallic=false;double mass=80;
            if(entity instanceof CoinProjectile coin) {
                metallic=external(coin.getItem());mass=1;
            } else if(entity instanceof ItemEntity item) {
                metallic=external(item.getItem());
                var form=EconomyContent.form(item.getItem().getItem());
                mass=Math.max(0.01,(form==null?1:form.units()/10.0)*item.getItem().getCount());
            } else if(entity instanceof LivingEntity living) {
                for(var slot:EquipmentSlot.values())if(slot.isArmor()&&external(living.getItemBySlot(slot))){metallic=true;break;}
            }
            if(metallic){best=new Target(position,entity,mass,false);bestDistance=distance;}
        }
        return Optional.ofNullable(best);
    }
    public static Vec3 limited(Vec3 velocity,double maximum) {
        if(!Double.isFinite(velocity.x)||!Double.isFinite(velocity.y)||!Double.isFinite(velocity.z))return Vec3.ZERO;
        return velocity.lengthSqr()>maximum*maximum?velocity.normalize().scale(maximum):velocity;
    }
    public static void apply(ServerPlayer player,Target target,double strength,boolean push,AllomancyConfig config) {
        Vec3 separation=target.position().subtract(player.getBoundingBox().getCenter());
        if(separation.lengthSqr()<0.04)return;
        Vec3 direction=separation.normalize().scale(push?1:-1);
        boolean braced=target.anchored()||(target.entity()!=null&&target.entity().onGround()&&direction.y<-.25);
        double momentum=config.force()*strength;
        if(braced&&!target.anchored())momentum*=Math.min(1,target.mass());
        if(!braced)momentum=Math.min(momentum,target.mass()*config.maximumObjectSpeed());
        Vec3 impulse=direction.scale(momentum);
        player.setDeltaMovement(limited(player.getDeltaMovement().subtract(impulse.scale(1.0/80)),config.maximumPlayerSpeed()));
        player.hurtMarked=true;
        if(!braced&&target.entity()!=null) {
            target.entity().setDeltaMovement(limited(target.entity().getDeltaMovement().add(impulse.scale(1/target.mass())),
                target.entity() instanceof ServerPlayer?config.maximumPlayerSpeed():config.maximumObjectSpeed()));
            target.entity().hurtMarked=true;
        }
    }
    private MetalPhysics(){}
}
