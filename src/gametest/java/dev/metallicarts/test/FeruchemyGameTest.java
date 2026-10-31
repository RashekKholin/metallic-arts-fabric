package dev.metallicarts.test;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.allomancy.*;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.*;
import dev.metallicarts.equipment.*;
import dev.metallicarts.feruchemy.*;
import dev.metallicarts.network.ContactIntent;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class FeruchemyGameTest {
    private static void grant(ServerPlayer p,Art art,Metal metal){
        UUID id=UUID.randomUUID();var state=p.getAttachedOrCreate(ModRegistries.SPIRITWEB);
        p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.grant(state,new PowerGrant(id,art,metal,PowerGrant.Origin.ADMIN,id,1)));
    }
    private static void held(ServerPlayer p,Metal metal){p.getInventory().setSelectedSlot(0);p.getInventory().setItem(0,new ItemStack(EconomyContent.item(metal.id(),"ring")));}
    private static boolean mode(ServerPlayer p,int slot,int mode,int rate){return FeruchemyRuntime.act(p,new ContactIntent(2,slot,0,mode,rate),100);}
    @GameTest public void weightStorageAndTappingChangeMassAndRecoil(GameTestHelper c){
        var p=c.makeMockServerPlayerInLevel();grant(p,Art.FERUCHEMY,Metal.IRON);held(p,Metal.IRON);
        c.assertTrue(FeruchemyRuntime.act(p,new ContactIntent(0,0,0,0,1),100),"Equip actual held ring");
        c.assertTrue(p.getMainHandItem().isEmpty(),"One physical item moved");
        c.assertTrue(mode(p,0,1,3),"Store weight at 75 percent");
        FeruchemyRuntime.tick(p,101);
        c.assertTrue(FeruchemyRuntime.mass(p)==20,"Storing weight reduces effective mass");
        var data=ContactService.equipment(p).item(0).get(ModRegistries.METALMIND);
        var ledger=MetalmindLedger.get(c.getLevel().getServer());
        c.assertTrue(ledger.resolve(data.token()).charge()==750,"Charge matches surrendered weight");
        p.setDeltaMovement(Vec3.ZERO);
        var anchor=new MetalPhysics.Target(p.getBoundingBox().getCenter().add(0,-3,0),null,Double.POSITIVE_INFINITY,true);
        MetalPhysics.apply(p,anchor,1,true,AllomancyConfig.DEFAULT);
        c.assertTrue(Math.abs(p.getDeltaMovement().y-.7)<.000001,"Lighter player gets more anchor recoil");
        c.assertTrue(mode(p,0,2,1),"Switch to tapping");
        FeruchemyRuntime.tick(p,102);
        c.assertTrue(FeruchemyRuntime.mass(p)==100&&ledger.resolve(data.token()).charge()==500,"Tapping spends before changing mass");
        FeruchemyRuntime.stop(p);c.assertTrue(FeruchemyRuntime.mass(p)==80,"Transient weight resets");
        c.succeed();
    }
    @GameTest public void copiedReferencesAndFullInventoryCannotDuplicateOrLoseGear(GameTestHelper c){
        var p=c.makeMockServerPlayerInLevel();held(p,Metal.STEEL);
        c.assertTrue(FeruchemyRuntime.act(p,new ContactIntent(0,0,0,0,1),100),"Equip");
        var copied=ContactService.equipment(p).item(0);
        p.getInventory().setItem(0,copied.copy());
        c.assertFalse(FeruchemyRuntime.act(p,new ContactIntent(0,1,0,0,1),100),"Copied leased asset rejected");
        c.assertTrue(!p.getMainHandItem().isEmpty()&&ContactService.equipment(p).item(1).isEmpty(),"Failed equip is atomic");
        for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.STONE,64));
        c.assertFalse(FeruchemyRuntime.act(p,new ContactIntent(1,0,0,0,1),100),"Full inventory refuses unequip");
        c.assertTrue(!ContactService.equipment(p).item(0).isEmpty(),"Equipped item retained");
        p.getInventory().setItem(1,ItemStack.EMPTY);
        c.assertTrue(FeruchemyRuntime.act(p,new ContactIntent(1,0,0,0,1),100),"Unequip once space exists");
        c.assertTrue(ContactService.equipment(p).item(0).isEmpty(),"Contact removed");
        p.getInventory().setItem(0,copied);
        c.assertFalse(FeruchemyRuntime.act(p,new ContactIntent(0,2,0,0,1),100),"Old copied revision stays invalid after release");
        FeruchemyRuntime.stop(p);c.succeed();
    }
    @GameTest public void foreignIdentityAndLostAccessStopActions(GameTestHelper c){
        var a=c.makeMockServerPlayerInLevel();var b=c.makeMockServerPlayerInLevel();
        grant(a,Art.FERUCHEMY,Metal.IRON);grant(b,Art.FERUCHEMY,Metal.IRON);held(a,Metal.IRON);
        FeruchemyRuntime.act(a,new ContactIntent(0,0,0,0,1),100);mode(a,0,1,1);FeruchemyRuntime.tick(a,101);
        c.assertTrue(FeruchemyRuntime.act(a,new ContactIntent(1,0,0,0,1),100),"Release keyed mind");
        var item=a.getInventory().getItem(0).copy();a.getInventory().setItem(0,ItemStack.EMPTY);
        b.getInventory().setSelectedSlot(0);b.getInventory().setItem(0,item);
        c.assertTrue(FeruchemyRuntime.act(b,new ContactIntent(0,0,0,0,1),100),"Foreign mind can be worn physically");
        c.assertFalse(mode(b,0,2,1),"Different Feruchemist cannot tap keyed charge");
        c.assertFalse(mode(b,0,1,1),"Different Feruchemist cannot mix Identity by storing");
        var state=b.getAttachedOrCreate(ModRegistries.SPIRITWEB);var data=ContactService.equipment(b).item(0).get(ModRegistries.METALMIND);
        var ledger=MetalmindLedger.get(c.getLevel().getServer());long before=ledger.resolve(data.token()).charge();
        b.setAttached(ModRegistries.SPIRITWEB,new Spiritweb(1,state.identity(),List.of(),state.reserves(),state.investedReserves()));
        FeruchemyRuntime.tick(b,102);
        c.assertTrue(before==ledger.resolve(data.token()).charge()&&FeruchemyRuntime.mass(b)==80,"No access means no effect or debit");
        FeruchemyRuntime.stop(a);FeruchemyRuntime.stop(b);c.succeed();
    }
    @GameTest public void allomanticAndFeruchemicalStrengthUseIndependentModifiers(GameTestHelper c){
        var p=c.makeMockServerPlayerInLevel();grant(p,Art.FERUCHEMY,Metal.PEWTER);grant(p,Art.ALLOMANCY,Metal.PEWTER);
        p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.credit(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),Metal.PEWTER,1000));
        held(p,Metal.PEWTER);FeruchemyRuntime.act(p,new ContactIntent(0,0,0,0,1),100);mode(p,0,1,1);
        c.assertTrue(AllomancyRuntime.request(p,2,1,100),"Allomantic Pewter active");
        FeruchemyRuntime.tick(p,101);AllomancyRuntime.tick(p,101);
        var attack=p.getAttribute(Attributes.ATTACK_DAMAGE);
        c.assertTrue(attack.hasModifier(MetallicArts.id("pewter_attack"))&&attack.hasModifier(MetallicArts.id("feruchemical_strength")),"Both systems present");
        c.assertTrue(attack.getModifier(MetallicArts.id("feruchemical_strength")).amount()==-.5,"Storage has physical cost");
        FeruchemyRuntime.stop(p);
        c.assertTrue(attack.hasModifier(MetallicArts.id("pewter_attack"))&&!attack.hasModifier(MetallicArts.id("feruchemical_strength")),"Feruchemy cleanup retains Allomancy");
        AllomancyRuntime.stop(p);c.succeed();
    }
    @GameTest public void fiveTinSensesStaySeparate(GameTestHelper c){
        var p=c.makeMockServerPlayerInLevel();grant(p,Art.FERUCHEMY,Metal.TIN);
        for(int i=0;i<5;i++){
            held(p,Metal.TIN);c.assertTrue(FeruchemyRuntime.act(p,new ContactIntent(0,i,i,0,1),100),"Equip distinct sensory mind");
            c.assertTrue(mode(p,i,1,1),"Store sense independently");
        }
        FeruchemyRuntime.tick(p,101);
        var ledger=MetalmindLedger.get(c.getLevel().getServer());var attributes=new HashSet<FeruchemicalAttribute>();
        for(int i=0;i<5;i++){
            var data=ContactService.equipment(p).item(i).get(ModRegistries.METALMIND);var asset=ledger.resolve(data.token());
            attributes.add(asset.attribute());c.assertTrue(asset.charge()==250,"Each mind has its own charge");
        }
        c.assertTrue(attributes.size()==5,"Five separate sensory types");FeruchemyRuntime.stop(p);c.succeed();
    }
}
