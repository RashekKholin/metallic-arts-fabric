package dev.metallicarts.test;

import dev.metallicarts.allomancy.*;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.*;
import dev.metallicarts.feruchemy.*;
import dev.metallicarts.network.ContactIntent;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;

public final class PhysicalEquipmentGameTest {
    private static void aim(net.minecraft.server.level.ServerPlayer p,Vec3 point){
        var d=point.subtract(p.getEyePosition());p.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));
        p.setXRot((float)-Math.toDegrees(Math.atan2(d.y,Math.hypot(d.x,d.z))));
    }
    @GameTest public void heavyLoadsRequireStrengthAndDropExactlyOnceWhenStrengthIsStored(GameTestHelper c){
        var p=c.makeMockServerPlayerInLevel();var start=c.absolutePos(new BlockPos(1,1,0));var load=c.absolutePos(new BlockPos(1,1,2));
        p.teleportTo(start.getX()+.5,start.getY(),start.getZ()+.5);
        c.getLevel().setBlock(load,Blocks.IRON_BLOCK.defaultBlockState(),3);aim(p,Vec3.atCenterOf(load));
        c.assertFalse(PhysicalCargo.toggle(p),"Baseline strength cannot lift heavy iron block");
        c.assertTrue(c.getLevel().getBlockState(load).is(Blocks.IRON_BLOCK),"Failed lift preserves block");
        UUID id=UUID.randomUUID();var state=p.getAttachedOrCreate(ModRegistries.SPIRITWEB);
        p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.grant(state,new PowerGrant(id,Art.FERUCHEMY,Metal.PEWTER,PowerGrant.Origin.ADMIN,id,1)));
        p.getInventory().setSelectedSlot(0);p.getInventory().setItem(0,new ItemStack(EconomyContent.item("pewter","bracer")));
        FeruchemyRuntime.act(p,new ContactIntent(0,0,0,0,1),100);
        var data=ContactService.equipment(p).item(0).get(ModRegistries.METALMIND);
        var ledger=MetalmindLedger.get(c.getLevel().getServer());
        ledger.change(data.token(),new dev.metallicarts.equipment.AssetLease(p.getUUID(),0),state.identity(),100000);
        c.assertTrue(FeruchemyRuntime.act(p,new ContactIntent(2,0,0,2,4),100),"Tap strength at 200 percent");
        FeruchemyRuntime.tick(p,101);aim(p,Vec3.atCenterOf(load));
        c.assertTrue(FeruchemyRuntime.carryCapacity(p)==120&&PhysicalCargo.toggle(p),"Greater strength lifts real load");
        c.assertTrue(c.getLevel().getBlockState(load).isAir()&&PhysicalCargo.carriedMass(p)==120,"Original block moves into one cargo entity");
        c.assertTrue(Math.abs(FeruchemyRuntime.mass(p)-232)<.0001,"Load and muscle bulk join effective body mass");
        var cargo=(PhysicalCargo)c.getLevel().getEntity(p.getAttached(ModRegistries.PHYSICAL_CARGO));
        FeruchemyRuntime.act(p,new ContactIntent(2,0,0,1,3),100);FeruchemyRuntime.tick(p,102);
        cargo.tick();cargo.drop();
        c.assertTrue(cargo.isRemoved()&&p.getAttached(ModRegistries.PHYSICAL_CARGO)==null,"Lost strength releases custody");
        var drops=c.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,p.getBoundingBox().inflate(5),
            e->e.getItem().is(Items.IRON_BLOCK));
        c.assertTrue(drops.stream().mapToInt(e->e.getItem().getCount()).sum()==1,"Repeated cleanup returns exactly one block");
        FeruchemyRuntime.stop(p);c.succeed();
    }
    @GameTest public void cargoRespectsBlockBreakProtection(GameTestHelper c){
        var p=c.makeMockServerPlayerInLevel();var start=c.absolutePos(new BlockPos(1,1,0));var load=c.absolutePos(new BlockPos(1,1,2));
        p.teleportTo(start.getX()+.5,start.getY(),start.getZ()+.5);c.getLevel().setBlock(load,Blocks.COBBLESTONE.defaultBlockState(),3);
        aim(p,Vec3.atCenterOf(load));UUID actor=p.getUUID();
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.register((level,player,pos,state,entity)->!player.getUUID().equals(actor));
        c.assertFalse(PhysicalCargo.toggle(p),"Permission callback denies lift");
        c.assertTrue(c.getLevel().getBlockState(load).is(Blocks.COBBLESTONE)&&p.getAttached(ModRegistries.PHYSICAL_CARGO)==null,"Denied lift changes nothing");
        c.succeed();
    }
    @GameTest public void pouchFeedsOneComponentBearingCoinAndRejectsNonCoins(GameTestHelper c){
        var p=c.makeMockServerPlayerInLevel();var pos=c.absolutePos(new BlockPos(2,3,2));p.teleportTo(pos.getX(),pos.getY(),pos.getZ());
        var pouch=new ItemStack(EconomyContent.ITEMS.get("coin_pouch"));
        var coins=new ItemStack(EconomyContent.item("steel","coin"),3);
        var data=new MetalItemData(1,UUID.randomUUID(),Metal.STEEL,100000,1,0,Optional.empty());coins.set(ModRegistries.METAL_DATA,data);
        var contents=new BundleContents.Mutable(BundleContents.EMPTY);c.assertTrue(contents.tryInsert(coins)==3,"Bundle accounts for three actual coins");
        pouch.set(DataComponents.BUNDLE_CONTENTS,contents.toImmutable());p.getInventory().setItem(0,pouch);
        c.assertTrue(CoinProjectile.toss(p),"Toss from pouch");
        c.assertTrue(pouch.get(DataComponents.BUNDLE_CONTENTS).itemCopyStream().mapToInt(ItemStack::getCount).sum()==2,"Exactly one removed");
        c.assertValueEqual(data,CoinPouchItem.peek(pouch).orElseThrow().get(ModRegistries.METAL_DATA),"Coin component retained");
        var container=new net.minecraft.world.SimpleContainer(1);container.setItem(0,pouch);var stone=new ItemStack(Items.STONE,4);
        boolean inserted=((CoinPouchItem)pouch.getItem()).overrideOtherStackedOnMe(pouch,stone,new net.minecraft.world.inventory.Slot(container,0,0,0),
            net.minecraft.world.inventory.ClickAction.SECONDARY,p,net.minecraft.world.entity.SlotAccess.of(()->container.getItem(0),stack->container.setItem(0,stack)));
        c.assertFalse(inserted,"Noncoin insertion refused");c.assertTrue(stone.getCount()==4,"Rejected cursor unchanged");
        c.succeed();
    }
}
