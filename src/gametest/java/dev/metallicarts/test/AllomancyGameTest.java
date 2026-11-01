package dev.metallicarts.test;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.allomancy.*;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.EconomyContent;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class AllomancyGameTest {
    @GameTest public void opposingCastersCombineBeforeCappingTheSameMovingBody(GameTestHelper c){
        var a=c.makeMockServerPlayerInLevel();var b=c.makeMockServerPlayerInLevel();
        var pos=Vec3.atCenterOf(c.absolutePos(new net.minecraft.core.BlockPos(2,2,2)));
        a.teleportTo(pos.x-3,pos.y-a.getBbHeight()/2,pos.z);b.teleportTo(pos.x+3,pos.y-b.getBbHeight()/2,pos.z);
        var item=new ItemEntity(c.getLevel(),pos.x,pos.y,pos.z,new ItemStack(Items.IRON_NUGGET));item.setNoGravity(true);c.getLevel().addFreshEntity(item);
        var target=new MetalPhysics.Target(pos,item,1,false);
        for(boolean reverse:List.of(false,true)){
            item.setDeltaMovement(3.9,0,0);a.setDeltaMovement(Vec3.ZERO);b.setDeltaMovement(Vec3.ZERO);
            try(var frame=MetalPhysics.beginFrame()){
                MetalPhysics.apply(reverse?b:a,target,1,true,AllomancyConfig.DEFAULT);
                MetalPhysics.apply(reverse?a:b,target,1,true,AllomancyConfig.DEFAULT);
                c.assertTrue(Math.abs(item.getDeltaMovement().x-3.9)<.000001,"No intermediate cap is applied inside the force frame");
            }
            c.assertTrue(Math.abs(item.getDeltaMovement().x-3.9)<.000001,"Opposing casters preserve initial motion regardless of order");
            c.assertTrue(a.getDeltaMovement().x<0&&b.getDeltaMovement().x>0,"Both casters receive reciprocal recoil");
        }
        c.succeed();
    }
    @GameTest public void combinedRecoilIsOrderIndependentAndSharesForceBudget(GameTestHelper c) {
        var p=c.makeMockServerPlayerInLevel();var center=p.getBoundingBox().getCenter();
        var left=new MetalPhysics.Target(center.add(-3,0,0),null,Double.POSITIVE_INFINITY,true);
        var right=new MetalPhysics.Target(center.add(3,0,0),null,Double.POSITIVE_INFINITY,true);
        for(var targets:List.of(List.of(left,right),List.of(right,left))){
            p.setDeltaMovement(2.3,0,0);
            MetalPhysics.applyMany(p,targets,2,true,AllomancyConfig.DEFAULT);
            c.assertTrue(Math.abs(p.getDeltaMovement().x-2.3)<.000001,"Opposing anchors cancel before the speed cap");
        }
        p.setDeltaMovement(Vec3.ZERO);
        MetalPhysics.applyMany(p,List.of(right,new MetalPhysics.Target(center.add(4,0,0),null,Double.POSITIVE_INFINITY,true)),1,true,AllomancyConfig.DEFAULT);
        c.assertTrue(Math.abs(p.getDeltaMovement().x+AllomancyConfig.DEFAULT.force()*(MetalPhysics.distanceFactor(3)+MetalPhysics.distanceFactor(4))/160)<.000001,"Selection shares one force budget");
        c.succeed();
    }
    @GameTest public void selectedTargetsAreRevalidatedAgainstWorldAndComponents(GameTestHelper c) {
        var p=c.makeMockServerPlayerInLevel();var pos=c.absolutePos(new net.minecraft.core.BlockPos(2,1,2));
        p.teleportTo(pos.getX(),pos.getY()+2,pos.getZ());
        c.getLevel().setBlock(pos,net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState(),3);
        var anchor=new MetalPhysics.Target(Vec3.atCenterOf(pos),null,Double.POSITIVE_INFINITY,true);
        c.assertTrue(MetalPhysics.refresh(p,anchor,AllomancyConfig.DEFAULT).isPresent(),"Existing metal anchor remains valid");
        c.getLevel().setBlock(pos,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
        c.assertTrue(MetalPhysics.refresh(p,anchor,AllomancyConfig.DEFAULT).isEmpty(),"Replaced anchor is removed");
        var item=new ItemEntity(c.getLevel(),pos.getX()+2,pos.getY()+2,pos.getZ(),new ItemStack(EconomyContent.item("steel","coin")));
        var target=new MetalPhysics.Target(item.position(),item,1,false);
        c.assertTrue(MetalPhysics.refresh(p,target,AllomancyConfig.DEFAULT).isPresent(),"External metal stack remains valid");
        item.setItem(new ItemStack(EconomyContent.item("aluminum","coin")));
        c.assertTrue(MetalPhysics.refresh(p,target,AllomancyConfig.DEFAULT).isEmpty(),"Component/material changes invalidate selection");
        item.setItem(new ItemStack(EconomyContent.item("steel","coin")));item.setPos(p.getX()+100,p.getY(),p.getZ());
        c.assertTrue(MetalPhysics.refresh(p,target,AllomancyConfig.DEFAULT).isEmpty(),"Out-of-range target is removed");
        c.succeed();
    }
    @GameTest public void selectionIsBoundedAndRequiresActivePower(GameTestHelper c) {
        var p=c.makeMockServerPlayerInLevel();var pos=c.absolutePos(new net.minecraft.core.BlockPos(1,3,0));
        p.teleportTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
        c.assertFalse(AllomancyRuntime.select(p,0,100),"No active power cannot select");
        p.setAttached(ModRegistries.SPIRITWEB,power(Metal.STEEL,10000));
        c.assertTrue(AllomancyRuntime.request(p,0,1,100),"Steel enabled");
        for(int i=0;i<9;i++){
            var block=c.absolutePos(new net.minecraft.core.BlockPos((i%3)*2,1+(i/3)*2,6));
            c.getLevel().setBlock(block,net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState(),3);
            Vec3 delta=Vec3.atCenterOf(block).subtract(p.getEyePosition());
            p.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));
            p.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,Math.hypot(delta.x,delta.z))));
            c.assertTrue(AllomancyRuntime.select(p,0,100)==(i<8),"At most eight unique targets");
        }
        c.assertFalse(AllomancyRuntime.select(p,-1,100),"Invalid action refused");
        c.assertTrue(AllomancyRuntime.select(p,1,100),"Clear selection");
        c.assertTrue(AllomancyRuntime.select(p,0,100),"Room available after clear");
        AllomancyRuntime.stop(p);c.succeed();
    }
    private static Spiritweb power(Metal metal,long fuel) {
        UUID id=UUID.randomUUID();
        return new Spiritweb(1,UUID.randomUUID(),List.of(new PowerGrant(id,Art.ALLOMANCY,metal,PowerGrant.Origin.ADMIN,id,1)),Map.of(metal,fuel));
    }
    @GameTest public void pewterConsumesFuelAndCleansOnlyOwnModifiers(GameTestHelper c) {
        var p=c.makeMockServerPlayerInLevel();var state=power(Metal.PEWTER,1000);p.setAttached(ModRegistries.SPIRITWEB,state);
        var attack=p.getAttribute(Attributes.ATTACK_DAMAGE);double base=attack.getValue();
        var foreign=new AttributeModifier(MetallicArts.id("test_foreign"),2,AttributeModifier.Operation.ADD_VALUE);
        attack.addTransientModifier(foreign);
        c.assertTrue(AllomancyRuntime.request(p,2,1,100),"Owned fueled Pewter activates");
        AllomancyRuntime.tick(p,101);
        c.assertTrue(attack.getValue()==base+5,"Pewter and existing modifier combine");
        c.assertTrue(p.getAttachedOrCreate(ModRegistries.SPIRITWEB).reserves().get(Metal.PEWTER)==990,"Cost precedes effect");
        p.setAttached(ModRegistries.SPIRITWEB,p.getAttachedOrCreate(ModRegistries.SPIRITWEB).withoutSource(state.grants().getFirst().source()));
        AllomancyRuntime.tick(p,102);
        c.assertTrue(attack.getValue()==base+2&&attack.hasModifier(foreign.id()),"Revocation cleans only mod-owned effect");
        AllomancyRuntime.stop(p);c.succeed();
    }
    @GameTest public void stopResetsModesAndUnsupportedRequestsNeverConsume(GameTestHelper c) {
        var p=c.makeMockServerPlayerInLevel();p.setAttached(ModRegistries.SPIRITWEB,power(Metal.PEWTER,1000));
        c.assertFalse(AllomancyRuntime.request(p,12,1,100),"Unsupported ordinal");
        c.assertFalse(AllomancyRuntime.request(p,2,3,100),"Invalid mode");
        c.assertTrue(AllomancyRuntime.request(p,2,2,100),"Flare permitted");
        AllomancyRuntime.tick(p,101);AllomancyRuntime.stop(p);
        var saved=p.getAttachedOrCreate(ModRegistries.SPIRITWEB);
        AllomancyRuntime.tick(p,102);
        c.assertValueEqual(saved,p.getAttachedOrCreate(ModRegistries.SPIRITWEB),"Stop does not resume burning");
        c.assertFalse(p.getAttribute(Attributes.ATTACK_DAMAGE).hasModifier(MetallicArts.id("pewter_attack")),"Stop cleans modifier");
        AllomancyRuntime.stop(p);c.succeed();
    }
    @GameTest public void anchorAndLooseMetalHaveReciprocalBoundedMotion(GameTestHelper c) {
        var p=c.makeMockServerPlayerInLevel();p.setDeltaMovement(Vec3.ZERO);
        var center=p.getBoundingBox().getCenter();
        MetalPhysics.apply(p,new MetalPhysics.Target(center.add(0,-3,0),null,Double.POSITIVE_INFINITY,true),1,true,AllomancyConfig.DEFAULT);
        c.assertTrue(p.getDeltaMovement().y>0,"Push down on anchor lifts player");
        p.setDeltaMovement(Vec3.ZERO);
        var item=new ItemEntity(c.getLevel(),center.x+3,center.y,center.z,new ItemStack(EconomyContent.item("steel","coin")));
        item.setDeltaMovement(Vec3.ZERO);
        MetalPhysics.apply(p,new MetalPhysics.Target(item.position(),item,1,false),1,true,AllomancyConfig.DEFAULT);
        c.assertTrue(item.getDeltaMovement().x>0&&p.getDeltaMovement().x<0,"Opposite momentum directions");
        c.assertTrue(Math.abs(item.getDeltaMovement().x+80*p.getDeltaMovement().x)<.00001,"Equal opposite momentum before caps");
        c.assertTrue(item.getDeltaMovement().length()<=AllomancyConfig.DEFAULT.maximumObjectSpeed(),"Object speed bounded");
        c.assertTrue(MetalPhysics.limited(new Vec3(Double.NaN,0,0),3).equals(Vec3.ZERO),"Nonfinite movement rejected");
        c.succeed();
    }
    @GameTest public void aluminumAndNonmetalAreExcludedButInvestedMetalRemainsPhysical(GameTestHelper c) {
        c.assertFalse(MetalPhysics.external(new ItemStack(EconomyContent.item("aluminum","coin"))),"Aluminum immune");
        c.assertFalse(MetalPhysics.external(new ItemStack(EconomyContent.item("carbon","portion"))),"Carbon is not metal");
        var steel=new ItemStack(EconomyContent.item("steel","coin"));
        c.assertTrue(MetalPhysics.external(steel),"Ordinary steel is detectable");
        steel.set(ModRegistries.METAL_DATA,new MetalItemData(1,UUID.randomUUID(),Metal.STEEL,100000,1,2,Optional.empty()));
        c.assertTrue(MetalPhysics.external(steel),"Invested steel remains physical metal");
        c.succeed();
    }
    @GameTest(maxTicks=100) public void thrownCoinIsRemovedOnceAndRecoveredWithComponents(GameTestHelper c) {
        var p=c.makeMockServerPlayerInLevel();var pos=c.absolutePos(new net.minecraft.core.BlockPos(2,3,2));
        p.teleportTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5);p.setXRot(90);
        var stack=new ItemStack(EconomyContent.item("steel","coin"),2);
        var data=new MetalItemData(1,UUID.randomUUID(),Metal.STEEL,100000,1,0,Optional.empty());stack.set(ModRegistries.METAL_DATA,data);
        p.getInventory().setItem(0,stack);
        for(int x=0;x<5;x++)for(int z=0;z<5;z++)c.setBlock(x,0,z,net.minecraft.world.level.block.Blocks.STONE);
        c.assertTrue(CoinProjectile.toss(p),"Coin thrown");
        c.assertTrue(stack.getCount()==1,"Exactly one removed");
        c.succeedWhen(()->{
            var items=c.getLevel().getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(pos).inflate(6));
            var returned=items.stream().filter(e->data.equals(e.getItem().get(ModRegistries.METAL_DATA))).toList();
            c.assertTrue(returned.size()==1&&returned.getFirst().getItem().getCount()==1,"One component-bearing coin recovered");
        });
    }
}
