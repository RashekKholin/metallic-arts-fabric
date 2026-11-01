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
    public record Target(Vec3 position,Entity entity,double mass,boolean anchored,String sourceKey) {public Target(Vec3 position,Entity entity,double mass,boolean anchored){this(position,entity,mass,anchored,null);}}
    public static String key(Target target) {
        return target.sourceKey()!=null?target.sourceKey():target.entity()==null?"block:"+BlockPos.containing(target.position()).asLong():"entity:"+target.entity().getUUID();
    }
    /** Revalidate saved server targets every tick; never load chunks or trust a stale entity/profile. */
    public static Optional<Target> refresh(ServerPlayer player,Target target,AllomancyConfig config) {return refresh(player,target,config,true);}
    public static Optional<Target> validate(ServerPlayer player,Target target,AllomancyConfig config) {return refresh(player,target,config,false);}
    private static Optional<Target> refresh(ServerPlayer player,Target target,AllomancyConfig config,boolean range) {
        if(target.entity()==null) {
            var pos=BlockPos.containing(target.position());
            if(!player.level().hasChunkAt(pos)||!MetalMaterials.block(player.level().getBlockState(pos)))return Optional.empty();
            var group=ConnectedMetals.group(player.level(),pos);var nearest=group.members().stream().filter(player.level()::hasChunkAt).min(Comparator.comparingDouble(v->player.getEyePosition().distanceToSqr(Vec3.atCenterOf(v)))).orElse(pos);if(MetalMaterials.block(player.level().getBlockState(nearest)))pos=nearest;
            if(range&&player.getEyePosition().distanceTo(Vec3.atCenterOf(pos))>config.detectionRange())return Optional.empty();
            return Optional.of(blockTarget(player,pos));
        }
        var entity=target.entity();
        if(entity==player||entity.isRemoved()||entity.level()!=player.level()||entity.isSpectator()||
            entity instanceof dev.metallicarts.feruchemy.PhysicalCargo cargo&&cargo.carrier()==player||
            range&&entity.getBoundingBox().getCenter().distanceTo(player.getEyePosition())>config.detectionRange())return Optional.empty();
        return entityTarget(entity);
    }
    public static boolean candidate(Entity e){return e instanceof ItemEntity||e instanceof LivingEntity||e instanceof CoinProjectile||e instanceof Display.BlockDisplay||e instanceof net.minecraft.world.entity.vehicle.minecart.AbstractMinecart||e instanceof net.minecraft.world.entity.item.FallingBlockEntity||e instanceof net.minecraft.world.entity.projectile.arrow.ThrownTrident||e instanceof net.minecraft.world.entity.decoration.ItemFrame;}
    private static Optional<Target> entityTarget(Entity entity) {
        boolean metallic=false;double mass=entity instanceof ServerPlayer p?dev.metallicarts.feruchemy.FeruchemyRuntime.mass(p):80;
        if(entity instanceof dev.metallicarts.feruchemy.PhysicalCargo cargo){
            var owner=cargo.carrier();
            return cargo.metallic()&&owner!=null&&owner.isAlive()&&!owner.isSpectator()&&owner.level()==entity.level()
                ?Optional.of(new Target(entity.position().add(.5,.5,.5),entity,dev.metallicarts.feruchemy.FeruchemyRuntime.mass(owner),false,"entity:"+owner.getUUID())):Optional.empty();
        }
        if(entity instanceof CoinProjectile coin){metallic=externalInWorld(coin.getItem(),entity);mass=1;}
        else if(entity instanceof ItemEntity item){
            metallic=externalInWorld(item.getItem(),entity);
            var form=EconomyContent.form(item.getItem().getItem());
            mass=Math.max(.01,(form==null?1:form.units()/10.0)*item.getItem().getCount());
        } else if(entity instanceof net.minecraft.world.entity.vehicle.minecart.AbstractMinecart){metallic=true;mass=500;}
        else if(entity instanceof net.minecraft.world.entity.item.FallingBlockEntity falling){metallic=MetalMaterials.block(falling.getBlockState());mass=300;}
        else if(entity instanceof net.minecraft.world.entity.projectile.arrow.ThrownTrident trident){metallic=external(trident.getPickupItemStackOrigin());mass=2;}
        else if(entity instanceof net.minecraft.world.entity.decoration.ItemFrame frame){metallic=external(frame.getItem());mass=10;}
        else if(entity instanceof Display.BlockDisplay display){metallic=MetalMaterials.block(((dev.metallicarts.mixin.BlockDisplayAccess)display).metallicarts$getBlockState());mass=300;}
        else if(entity instanceof LivingEntity living&&living.isAlive()){
            var type=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();if(type.equals("iron_golem")||type.equals("copper_golem")){metallic=true;mass=type.equals("iron_golem")?500:150;}
            for(var slot:EquipmentSlot.values())if(external(living.getItemBySlot(slot))){metallic=true;break;}
            for(var attachment:List.of(ModRegistries.CONTACT_EQUIPMENT,ModRegistries.SPIKES)){var gear=living.getAttached(attachment);if(gear!=null&&gear.items().stream().anyMatch(MetalPhysics::external))metallic=true;}
        }
        return metallic&&!entity.isSpectator()?Optional.of(new Target(entity.getBoundingBox().getCenter(),entity,mass,false)):Optional.empty();
    }
    private static boolean externalInWorld(ItemStack stack,Entity entity){return external(stack);}
    public static boolean external(ItemStack stack){return MetalMaterials.item(stack);}
    public static Target blockTarget(ServerPlayer player,BlockPos pos){var group=ConnectedMetals.group(player.level(),pos);return new Target(Vec3.atCenterOf(pos),null,Double.POSITIVE_INFINITY,true,"block:"+group.root().asLong());}
    /** Scan loaded chunk sections with metal-containing palettes, then deduplicate connected sources. */
    public static List<Target> nearby(ServerPlayer player,AllomancyConfig config){
        var eye=player.getEyePosition();int radius=config.detectionRange();var candidates=new HashMap<String,Target>();
        for(int cx=(int)Math.floor((eye.x-radius)/16);cx<=(int)Math.floor((eye.x+radius)/16);cx++)for(int cz=(int)Math.floor((eye.z-radius)/16);cz<=(int)Math.floor((eye.z+radius)/16);cz++){
            var chunk=player.level().getChunkSource().getChunkNow(cx,cz);if(chunk==null)continue;var sections=chunk.getSections();
            for(int i=0;i<sections.length;i++){int baseY=player.level().getSectionYFromSectionIndex(i)*16;if(baseY>eye.y+radius||baseY+16<eye.y-radius)continue;var section=sections[i];if(section.hasOnlyAir()||!section.maybeHas(MetalMaterials::block))continue;
                for(int y=0;y<16;y++)for(int x=0;x<16;x++)for(int z=0;z<16;z++){var pos=new BlockPos(cx*16+x,baseY+y,cz*16+z);if(eye.distanceToSqr(Vec3.atCenterOf(pos))>(double)radius*radius||!MetalMaterials.block(section.getBlockState(x,y,z)))continue;var target=blockTarget(player,pos);candidates.merge(key(target),target,(old,next)->old.position().distanceToSqr(eye)<=next.position().distanceToSqr(eye)?old:next);}
            }
        }
        for(var entity:player.level().getEntities(player,player.getBoundingBox().inflate(radius),MetalPhysics::candidate)){
            if(entity instanceof dev.metallicarts.feruchemy.PhysicalCargo cargo&&cargo.carrier()==player)continue;entityTarget(entity).filter(t->t.position().distanceToSqr(eye)<=(double)radius*radius).ifPresent(t->candidates.put(key(t),t));
        }
        return candidates.values().stream().sorted(Comparator.comparingDouble(t->t.position().distanceToSqr(eye))).limit(config.maximumTargets()).toList();
    }
    public static Optional<Target> aimed(ServerPlayer player,AllomancyConfig config) {
        Vec3 eye=player.getEyePosition(),look=player.getLookAngle();
        var level=player.level();
        Target best=null;double bestDistance=config.detectionRange()+1;
        // At most 64 block probes. Non-metal blocks do not hide an external metal anchor.
        for(double d=0.5;d<=config.detectionRange();d+=0.5) {
            Vec3 point=eye.add(look.scale(d));BlockPos pos=BlockPos.containing(point);
            if(!level.hasChunkAt(pos))continue;
            if(MetalMaterials.block(level.getBlockState(pos))){best=blockTarget(player,pos);bestDistance=d;break;}
        }
        // Entity query is limited by the loaded spatial index and range, then examined up to the configured cap.
        var entities=level.getEntities(player,player.getBoundingBox().inflate(config.detectionRange()),MetalPhysics::candidate);
        int checked=0;
        for(var entity:entities) {
            if(checked++>=config.maximumTargets())break;
            if(entity instanceof dev.metallicarts.feruchemy.PhysicalCargo cargo&&cargo.carrier()==player)continue;
            Vec3 position=entity instanceof dev.metallicarts.feruchemy.PhysicalCargo?entity.position().add(.5,.5,.5):entity.getBoundingBox().getCenter(),relative=position.subtract(eye);
            double along=relative.dot(look),distance=relative.length();
            if(along<0||distance>config.detectionRange()||distance>=bestDistance||relative.subtract(look.scale(along)).length()>0.8)continue;
            var candidate=entityTarget(entity);
            if(candidate.isPresent()){best=candidate.get();bestDistance=distance;}
        }
        return Optional.ofNullable(best);
    }
    public static Vec3 limited(Vec3 velocity,double maximum) {
        if(!Double.isFinite(velocity.x)||!Double.isFinite(velocity.y)||!Double.isFinite(velocity.z))return Vec3.ZERO;
        return velocity.lengthSqr()>maximum*maximum?velocity.normalize().scale(maximum):velocity;
    }
    public static void apply(ServerPlayer player,Target target,double strength,boolean push,AllomancyConfig config) {
        applyMany(player,List.of(target),strength,push,config);
    }
    private record Motion(Vec3 change,double cap){}
    private static final ThreadLocal<ForceFrame> FRAME=new ThreadLocal<>();
    /** All casters contribute against the same pre-force velocity before any speed cap is applied. */
    public static final class ForceFrame implements AutoCloseable {
        private final Map<Entity,Motion> motion=new HashMap<>();private boolean closed;
        private ForceFrame(){}
        @Override public void close(){
            if(closed)return;closed=true;FRAME.remove();
            motion.forEach((entity,value)->{if(!entity.isRemoved())applyMotion(entity,value.change(),value.cap());});
        }
    }
    public static ForceFrame beginFrame(){
        if(FRAME.get()!=null)throw new IllegalStateException("Nested metal-force frame");
        var frame=new ForceFrame();FRAME.set(frame);return frame;
    }
    public static void externalMotion(Entity entity,Vec3 change,double cap){motion(entity,change,cap);}
    private static void motion(Entity entity,Vec3 change,double cap){
        var frame=FRAME.get();
        if(frame==null){applyMotion(entity,change,cap);return;}
        frame.motion.merge(entity,new Motion(change,cap),(a,b)->new Motion(a.change().add(b.change()),Math.min(a.cap(),b.cap())));
    }
    private static void applyMotion(Entity entity,Vec3 change,double cap){
        entity.setDeltaMovement(limited(entity.getDeltaMovement().add(change),cap));entity.hurtMarked=true;
    }
    /** Split a fixed force budget and combine player recoil before applying the player speed cap. */
    public static void applyMany(ServerPlayer player,List<Target> targets,double strength,boolean push,AllomancyConfig config) {
        if(targets.isEmpty())return;
        Vec3 recoil=Vec3.ZERO;
        var motion=new HashMap<Entity,Vec3>();
        for(Target target:targets){
            Entity protectedBody=target.entity() instanceof dev.metallicarts.feruchemy.PhysicalCargo cargo?cargo.carrier():target.entity();
            if(protectedBody!=null&&!dev.metallicarts.api.ArtsProtection.allowsEntity(player,protectedBody,dev.metallicarts.api.ArtsProtection.Action.FORCE))continue;
            if(protectedBody==null&&!dev.metallicarts.api.ArtsProtection.BLOCK_EVENT.invoker().allow(player,BlockPos.containing(target.position()),dev.metallicarts.api.ArtsProtection.Action.FORCE))continue;
            recoil=recoil.subtract(impulse(player,target,strength/targets.size(),push,config,motion).scale(1.0/dev.metallicarts.feruchemy.FeruchemyRuntime.mass(player)));
        }
        motion(player,recoil,config.maximumPlayerSpeed());
        motion.forEach((entity,change)->{
            motion(entity,change,entity instanceof ServerPlayer?config.maximumPlayerSpeed():config.maximumObjectSpeed());
        });
    }
    private static Vec3 impulse(ServerPlayer player,Target target,double strength,boolean push,AllomancyConfig config,Map<Entity,Vec3> motion) {
        Vec3 separation=target.position().subtract(player.getBoundingBox().getCenter());
        if(separation.lengthSqr()<0.04)return Vec3.ZERO;
        Vec3 direction=separation.normalize().scale(push?1:-1);
        Entity body=target.entity() instanceof dev.metallicarts.feruchemy.PhysicalCargo cargo?cargo.carrier():target.entity();
        boolean braced=target.anchored()||(body!=null&&body.onGround()&&direction.y<-.25);
        double momentum=config.force()*strength;
        if(braced&&!target.anchored())momentum*=Math.min(1,target.mass());
        if(!braced)momentum=Math.min(momentum,target.mass()*config.maximumObjectSpeed());
        Vec3 impulse=direction.scale(momentum*distanceFactor(separation.length()));
        if(!braced&&body!=null)motion.merge(body,impulse.scale(1/target.mass()),Vec3::add);
        return impulse;
    }
    public static double distanceFactor(double distance){return 1/(1+Math.pow(Math.max(0,distance)/8.0,2));}
    private MetalPhysics(){}
}
