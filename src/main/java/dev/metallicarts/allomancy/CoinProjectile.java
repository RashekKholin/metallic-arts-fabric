package dev.metallicarts.allomancy;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.economy.EconomyContent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

/** Carries exactly one real inventory coin. Impact returns that same component-bearing stack. */
public final class CoinProjectile extends ThrowableItemProjectile {
    public static EntityType<CoinProjectile> TYPE;
    public static void initialize(){
        var key=ResourceKey.create(Registries.ENTITY_TYPE,MetallicArts.id("coin"));
        TYPE=Registry.register(BuiltInRegistries.ENTITY_TYPE,key,EntityType.Builder.<CoinProjectile>of(CoinProjectile::new,MobCategory.MISC)
            .sized(.15f,.15f).clientTrackingRange(8).updateInterval(1).build(key));
    }
    public CoinProjectile(EntityType<? extends CoinProjectile> type,Level level){super(type,level);}
    public CoinProjectile(ServerLevel level,ServerPlayer player,ItemStack stack){super(TYPE,player,level,stack);}
    @Override protected Item getDefaultItem(){return EconomyContent.item("steel","coin");}
    @Override protected void onHitEntity(EntityHitResult hit){
        super.onHitEntity(hit);
        if(level() instanceof ServerLevel server)
            hit.getEntity().hurtServer(server,damageSources().thrown(this,getOwner()),(float)Math.min(12,getDeltaMovement().length()*3));
    }
    @Override protected void onHit(HitResult hit){super.onHit(hit);recover();}
    private void recover(){
        if(!isRemoved()&&level() instanceof ServerLevel server){var dropped=spawnAtLocation(server,getItem().copyWithCount(1));AllomancyRuntime.recoverCoin(this,dropped);discard();}
    }
    @Override public void tick(){super.tick();if(tickCount>=1200)recover();}
    public static boolean toss(ServerPlayer player){
        if(!player.isAlive()||player.isSpectator())return false;
        var inventory=player.getInventory();
        for(int i=0;i<inventory.getContainerSize();i++){
            var stack=inventory.getItem(i);var form=EconomyContent.form(stack.getItem());
            if(stack.isEmpty()||form==null||!form.kind().equals("coin"))continue;
            var projectile=new CoinProjectile(player.level(),player,stack.copyWithCount(1));
            projectile.shootFromRotation(player,player.getXRot(),player.getYRot(),0,.8f,0);
            if(!player.level().addFreshEntity(projectile))return false;
            stack.shrink(1);AllomancyRuntime.autoSelectCoin(player,projectile);return true;
        }
        for(int i=0;i<inventory.getContainerSize();i++){
            var pouch=inventory.getItem(i);
            if(!(pouch.getItem() instanceof dev.metallicarts.economy.CoinPouchItem))continue;
            var coin=dev.metallicarts.economy.CoinPouchItem.peek(pouch);
            if(coin.isEmpty())continue;
            var projectile=new CoinProjectile(player.level(),player,coin.get());
            projectile.shootFromRotation(player,player.getXRot(),player.getYRot(),0,.8f,0);
            if(!player.level().addFreshEntity(projectile))return false;
            dev.metallicarts.economy.CoinPouchItem.takeOne(pouch);AllomancyRuntime.autoSelectCoin(player,projectile);return true;
        }
        return false;
    }
}
