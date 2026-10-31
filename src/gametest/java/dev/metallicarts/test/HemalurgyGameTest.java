package dev.metallicarts.test;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.allomancy.ReserveLedger;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.*;
import dev.metallicarts.equipment.*;
import dev.metallicarts.feruchemy.ContactService;
import dev.metallicarts.hemalurgy.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import java.util.*;

public final class HemalurgyGameTest {
    private static ServerPlayer player(GameTestHelper c){
        var p=c.makeMockServerPlayerInLevel();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getInventory().setSelectedSlot(0);return p;
    }
    private static UUID grant(LivingEntity donor,Art art,Metal metal){
        UUID id=UUID.randomUUID();donor.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.grant(donor.getAttachedOrCreate(ModRegistries.SPIRITWEB),new PowerGrant(id,art,metal,PowerGrant.Origin.ADMIN,id,1)));return id;
    }
    private static LivingEntity donor(GameTestHelper c,ServerPlayer p){
        var e=c.spawn(EntityType.VILLAGER,new BlockPos(1,1,1));p.teleportTo(e.getX()+1,e.getY(),e.getZ());return e;
    }
    private static void spike(ServerPlayer p,Metal metal){p.getInventory().setItem(0,new ItemStack(EconomyContent.item(metal.id(),"spike")));}
    @GameTest public void realDonorExtractionImplantationAndRemovalKeepSources(GameTestHelper c){
        var p=player(c);var donor=donor(c,p);UUID removed=grant(donor,Art.ALLOMANCY,Metal.IRON);grant(p,Art.ALLOMANCY,Metal.TIN);spike(p,Metal.STEEL);
        HemalurgyRuntime.extract(p,donor,new StolenPower(StolenPower.Kind.ALLOMANCY,Metal.IRON));
        c.assertFalse(donor.isAlive(),"Extraction has real donor consequence");
        c.assertFalse(donor.getAttachedOrCreate(ModRegistries.SPIRITWEB).grants().stream().anyMatch(g->g.id().equals(removed)),"Donor source removed before death");
        var data=p.getMainHandItem().get(ModRegistries.SPIKE);c.assertTrue(data!=null,"Exactly one held charged physical spike");
        var copied=p.getMainHandItem().copy();HemalurgyRuntime.implant(p,4,false);HemalurgyRuntime.tick(p,101);
        c.assertTrue(p.getAttachedOrCreate(ModRegistries.SPIRITWEB).strength(Art.ALLOMANCY,Metal.IRON)>.8,"Implanted source grants stolen art");
        p.getInventory().setItem(0,copied);boolean blocked=false;try{HemalurgyRuntime.implant(p,5,false);}catch(IllegalArgumentException e){blocked=true;}
        c.assertTrue(blocked&&HemalurgyRuntime.equipment(p).item(5).isEmpty(),"Copied leased spike cannot implant twice");
        p.getInventory().setItem(0,ItemStack.EMPTY);HemalurgyRuntime.remove(p,4);
        var state=p.getAttachedOrCreate(ModRegistries.SPIRITWEB);
        c.assertTrue(state.strength(Art.ALLOMANCY,Metal.IRON)==0&&state.strength(Art.ALLOMANCY,Metal.TIN)==1,"Only the removed spike's power is lost");c.succeed();
    }
    @GameTest public void wrongMaterialMissingPowerAndProtectedAttacksAreAtomic(GameTestHelper c){
        var p=player(c);var donor=donor(c,p);grant(donor,Art.FERUCHEMY,Metal.IRON);spike(p,Metal.STEEL);
        var original=donor.getAttachedOrCreate(ModRegistries.SPIRITWEB);float health=donor.getHealth();
        boolean blocked=false;try{HemalurgyRuntime.extract(p,donor,new StolenPower(StolenPower.Kind.FERUCHEMY,Metal.IRON));}catch(IllegalArgumentException e){blocked=true;}
        c.assertTrue(blocked&&original.equals(donor.getAttachedOrCreate(ModRegistries.SPIRITWEB))&&donor.getHealth()==health&&!p.getMainHandItem().has(ModRegistries.SPIKE),"Wrong material changes nothing");
        spike(p,Metal.PEWTER);donor.addTag("metallicarts_protection_fixture");
        AttackEntityCallback.EVENT.register((actor,level,hand,target,hit)->target.entityTags().contains("metallicarts_protection_fixture")?InteractionResult.FAIL:InteractionResult.PASS);
        blocked=false;try{HemalurgyRuntime.extract(p,donor,new StolenPower(StolenPower.Kind.FERUCHEMY,Metal.IRON));}catch(IllegalArgumentException e){blocked=true;}
        c.assertTrue(blocked&&original.equals(donor.getAttachedOrCreate(ModRegistries.SPIRITWEB))&&donor.getHealth()==health&&!p.getMainHandItem().has(ModRegistries.SPIKE),"Attack protection preserves donor and item");c.succeed();
    }
    @GameTest public void invulnerableDonorRollsBackAndUnownedPowerIsRefused(GameTestHelper c){
        var p=player(c);var donor=donor(c,p);grant(donor,Art.ALLOMANCY,Metal.STEEL);spike(p,Metal.STEEL);donor.setInvulnerable(true);
        var original=donor.getAttachedOrCreate(ModRegistries.SPIRITWEB);boolean blocked=false;
        try{HemalurgyRuntime.extract(p,donor,new StolenPower(StolenPower.Kind.ALLOMANCY,Metal.STEEL));}catch(IllegalArgumentException e){blocked=true;}
        c.assertTrue(blocked&&original.equals(donor.getAttachedOrCreate(ModRegistries.SPIRITWEB))&&!p.getMainHandItem().has(ModRegistries.SPIKE)&&donor.isAlive(),"Damage rejection restores escrow and source");
        donor.setInvulnerable(false);blocked=false;
        try{HemalurgyRuntime.extract(p,donor,new StolenPower(StolenPower.Kind.ALLOMANCY,Metal.PEWTER));}catch(IllegalArgumentException e){blocked=true;}
        c.assertTrue(blocked&&donor.isAlive(),"Cannot invent donor power");c.succeed();
    }
    @GameTest public void playerPolicyAndMalformedIntentStayClosedByDefault(GameTestHelper c){
        var p=player(c);var other=player(c);other.teleportTo(p.getX()+1,p.getY(),p.getZ());grant(other,Art.ALLOMANCY,Metal.STEEL);spike(p,Metal.STEEL);
        boolean blocked=false;try{HemalurgyRuntime.extract(p,other,new StolenPower(StolenPower.Kind.ALLOMANCY,Metal.STEEL));}catch(IllegalArgumentException e){blocked=true;}
        c.assertTrue(blocked&&other.isAlive()&&!p.getMainHandItem().has(ModRegistries.SPIKE),"Default policy or absent consent blocks player extraction");
        c.assertFalse(HemalurgyRuntime.act(p,new dev.metallicarts.network.SpikeIntent(999,-1,999)),"Malformed intent rejected");c.succeed();
    }
    @GameTest public void chargedEarringsCannotBeRepurposedAndFullInventoryKeepsImplants(GameTestHelper c){
        var p=player(c);var ledger=SpikeLedger.get(c.getLevel().getServer());
        var a=ledger.create(Metal.TIN,new StolenPower(StolenPower.Kind.SENSES,Metal.IRON),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1,0);
        var item=new ItemStack(EconomyContent.item("tin","earring"));item.set(ModRegistries.SPIKE,SpikeData.of(a));p.getInventory().setItem(0,item);
        boolean blocked=false;try{ContactService.equip(p,0,0);}catch(IllegalArgumentException e){blocked=true;}
        c.assertTrue(blocked&&!p.getMainHandItem().isEmpty(),"Hemalurgic earring cannot create a Feruchemical pool");
        HemalurgyRuntime.implant(p,2,false);HemalurgyRuntime.tick(p,101);
        c.assertTrue(HemalurgyRuntime.passive(p,StolenPower.Kind.SENSES)>.8,"Innate sensory handler active");
        for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.STONE,64));
        blocked=false;try{HemalurgyRuntime.remove(p,2);}catch(IllegalArgumentException e){blocked=true;}
        c.assertTrue(blocked&&!HemalurgyRuntime.equipment(p).item(2).isEmpty(),"Full inventory does not drop or lose implant");c.succeed();
    }
    @GameTest public void drainedImplantCannotRegenerateItsPower(GameTestHelper c){
        var thief=player(c);var donor=player(c);donor.teleportTo(thief.getX()+1,thief.getY(),thief.getZ());
        var ledger=SpikeLedger.get(c.getLevel().getServer());var a=ledger.create(Metal.STEEL,new StolenPower(StolenPower.Kind.ALLOMANCY,Metal.STEEL),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1,0);
        var item=new ItemStack(EconomyContent.item("steel","spike"));item.set(ModRegistries.SPIKE,SpikeData.of(a));donor.getInventory().setItem(0,item);
        HemalurgyRuntime.implant(donor,4,false);HemalurgyRuntime.tick(donor,101);
        var state=donor.getAttachedOrCreate(ModRegistries.SPIRITWEB);c.assertTrue(state.strength(Art.ALLOMANCY,Metal.STEEL)>.8,"Parent spike grants before extraction");
        ledger.drainSource(a.token().instance());HemalurgyRuntime.tick(donor,102);
        c.assertTrue(donor.getAttachedOrCreate(ModRegistries.SPIRITWEB).strength(Art.ALLOMANCY,Metal.STEEL)==0,"Drained parent source cannot regrant");c.succeed();
    }
    @GameTest public void incompatibleAnatomyAndCriticalIntentAreValidatedBeforeMovingItems(GameTestHelper c){
        var p=player(c);var ledger=SpikeLedger.get(c.getLevel().getServer());
        var a=ledger.create(Metal.STEEL,new StolenPower(StolenPower.Kind.ALLOMANCY,Metal.IRON),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1,c.getLevel().getServer().overworld().getGameTime());
        var stack=new ItemStack(EconomyContent.item("steel","spike"));stack.set(ModRegistries.SPIKE,SpikeData.of(a));p.getInventory().setItem(0,stack);
        boolean blocked=false;try{HemalurgyRuntime.implant(p,10,false);}catch(IllegalArgumentException e){blocked=true;}
        c.assertTrue(blocked&&ledger.resolve(a.token()).lease().isEmpty()&&!p.getMainHandItem().isEmpty(),"Wrong anatomy leaves asset and item unchanged");
        blocked=false;try{HemalurgyRuntime.implant(p,11,false);}catch(IllegalArgumentException e){blocked=true;}
        c.assertTrue(blocked&&ledger.resolve(a.token()).lease().isEmpty(),"Critical insertion requires explicit dangerous action");
        HemalurgyRuntime.implant(p,11,true);c.assertTrue(p.getMainHandItem().isEmpty()&&!HemalurgyRuntime.equipment(p).item(11).isEmpty(),"Explicit compatible critical insertion moves one item");
        var full=new ArrayList<PowerGrant>();for(int i=0;i<128;i++){UUID id=UUID.randomUUID();full.add(new PowerGrant(id,Art.ALLOMANCY,Metal.IRON,PowerGrant.Origin.ADMIN,id,1));}
        var state=p.getAttachedOrCreate(ModRegistries.SPIRITWEB);p.setAttached(ModRegistries.SPIRITWEB,new Spiritweb(1,state.identity(),full,Map.of()));
        HemalurgyRuntime.tick(p,101);c.assertTrue(p.getAttachedOrCreate(ModRegistries.SPIRITWEB).grants().size()==128,"External grant saturation cannot overflow the spiritweb");c.succeed();
    }
    @GameTest public void innateStrengthHasARealHandlerWithoutAnAllomanticGrant(GameTestHelper c){
        var p=player(c);var donor=donor(c,p);spike(p,Metal.IRON);
        HemalurgyRuntime.extract(p,donor,new StolenPower(StolenPower.Kind.STRENGTH,Metal.IRON));
        HemalurgyRuntime.implant(p,0,false);HemalurgyRuntime.tick(p,101);
        c.assertTrue(p.getAttachedOrCreate(ModRegistries.SPIRITWEB).grants().isEmpty(),"Innate strength is not a fabricated Allomantic capability");
        c.assertTrue(dev.metallicarts.feruchemy.FeruchemyRuntime.carryCapacity(p)>70,"Stolen innate strength increases physical carrying");
        c.assertTrue(p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).getModifier(MetallicArts.id("hemalurgic_strength")).amount()>1,"Innate melee modifier is active");
        HemalurgyRuntime.remove(p,0);c.assertTrue(dev.metallicarts.feruchemy.FeruchemyRuntime.carryCapacity(p)==40,"Removal revokes innate handler");c.succeed();
    }
    @GameTest public void aluminumDestructionProducesASpentRecordNotATransferablePower(GameTestHelper c){
        var p=player(c);var donor=donor(c,p);grant(donor,Art.ALLOMANCY,Metal.STEEL);grant(donor,Art.FERUCHEMY,Metal.IRON);spike(p,Metal.ALUMINUM);
        HemalurgyRuntime.extract(p,donor,new StolenPower(StolenPower.Kind.NULLIFICATION,Metal.IRON));
        c.assertTrue(!donor.isAlive()&&donor.getAttachedOrCreate(ModRegistries.SPIRITWEB).grants().isEmpty(),"Explicit destructive extraction removes donor grants");
        var data=p.getMainHandItem().get(ModRegistries.SPIKE);c.assertTrue(data!=null&&data.drained()&&data.potency()==0,"Output is a spent provenance record");
        boolean blocked=false;try{HemalurgyRuntime.implant(p,4,false);}catch(IllegalArgumentException e){blocked=true;}
        c.assertTrue(blocked&&p.getAttachedOrCreate(ModRegistries.SPIRITWEB).grants().isEmpty()&&!p.getMainHandItem().isEmpty(),"Spent aluminum does not grant a fabricated ability");c.succeed();
    }
}
