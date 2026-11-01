package dev.metallicarts.test;

import dev.metallicarts.allomancy.*;
import dev.metallicarts.arts.*;
import dev.metallicarts.domain.*;
import dev.metallicarts.network.ArtAction;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class AdvancedAllomancyGameTest {
    private static void access(ServerPlayer p,Metal metal){UUID id=UUID.randomUUID();var state=ReserveLedger.grant(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),new PowerGrant(id,Art.ALLOMANCY,metal,PowerGrant.Origin.ADMIN,id,1));p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.credit(state,metal,10000));}
    private static void burn(ServerPlayer p,Metal metal,int tick){if(!AllomancyRuntime.request(p,AllomancyConfig.SUPPORTED.indexOf(metal),1,tick))throw new AssertionError("Cannot activate "+metal);}
    @GameTest public void aluminumCancelsOtherEffectsAndClearsInternalFuelOnly(GameTestHelper c){
        var p=c.makeMockServerPlayerInLevel();access(p,Metal.ALUMINUM);access(p,Metal.STEEL);var identity=p.getAttachedOrCreate(ModRegistries.SPIRITWEB).identity();
        burn(p,Metal.STEEL,100);burn(p,Metal.ALUMINUM,100);AllomancyRuntime.prepare(p,100);
        c.assertTrue(p.getAttachedOrCreate(ModRegistries.SPIRITWEB).reserves().isEmpty()&&AllomancyRuntime.powers(p).isEmpty(),"Aluminum removes reserves before other effects");
        c.assertTrue(p.getAttachedOrCreate(ModRegistries.SPIRITWEB).grants().size()==2&&p.getAttachedOrCreate(ModRegistries.SPIRITWEB).identity().equals(identity),"Fuel depletion never removes abilities/Identity");c.succeed();
    }
    @GameTest public void duraluminConsumesActiveMetalsAndProducesOnlyOnePaidBurst(GameTestHelper c){
        var p=c.makeMockServerPlayerInLevel();access(p,Metal.DURALUMIN);access(p,Metal.STEEL);access(p,Metal.IRON);
        burn(p,Metal.STEEL,100);burn(p,Metal.DURALUMIN,100);AllomancyRuntime.prepare(p,100);
        c.assertTrue(AllomancyRuntime.power(p,Metal.STEEL)>1,"Concurrent Steel receives amplification");
        var state=p.getAttachedOrCreate(ModRegistries.SPIRITWEB);long duralumin=ReserveLedger.ordinary(state,Metal.DURALUMIN);c.assertTrue(state.reserves().getOrDefault(Metal.STEEL,0L)==0&&duralumin>0&&state.reserves().get(Metal.IRON)==10000,"Active ordinary metals exhausted, Duralumin pays normal fuel only");
        for(int tick=101;tick<200;tick++){AllomancyRuntime.prepare(p,tick);c.assertTrue(AllomancyRuntime.power(p,Metal.STEEL)>1,"Paid amplified effect lasts all 100 ticks");}
        c.assertTrue(ReserveLedger.ordinary(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),Metal.DURALUMIN)<duralumin,"Duralumin continues paying through the window");
        AllomancyRuntime.prepare(p,200);c.assertTrue(AllomancyRuntime.power(p,Metal.STEEL)==0&&AllomancyRuntime.power(p,Metal.DURALUMIN)==0,"Both burst and Duralumin stop at five seconds");long stopped=ReserveLedger.ordinary(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),Metal.DURALUMIN);AllomancyRuntime.prepare(p,201);c.assertTrue(ReserveLedger.ordinary(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),Metal.DURALUMIN)==stopped,"Automatic off stops further fuel debit");c.succeed();
    }
    @GameTest public void copperCloudAndBronzeUseCurrentPaidStrength(GameTestHelper c){
        var smoker=c.makeMockServerPlayerInLevel();var seeker=c.makeMockServerPlayerInLevel();seeker.teleportTo(smoker.getX()+2,smoker.getY(),smoker.getZ());
        access(smoker,Metal.COPPER);access(seeker,Metal.BRONZE);burn(smoker,Metal.COPPER,100);burn(seeker,Metal.BRONZE,100);AllomancyRuntime.prepare(smoker,100);AllomancyRuntime.prepare(seeker,100);
        c.assertTrue(AdvancedAllomancy.concealment(seeker)==1&&AllomancyRuntime.power(seeker,Metal.BRONZE)==1,"Nearby cloud masks equal-strength pulses");
        AllomancyRuntime.stopMetal(smoker,Metal.COPPER);c.assertTrue(AdvancedAllomancy.concealment(seeker)==0,"Stopping Copper immediately removes concealment");c.succeed();
    }
    @GameTest public void emotionalInfluenceChangesActualEligibleMobStateAndAttributes(GameTestHelper c){
        var p=c.makeMockServerPlayerInLevel();var mob=c.spawn(EntityType.VILLAGER,new BlockPos(2,1,2));p.teleportTo(mob.getX()+1,mob.getY(),mob.getZ());
        for(int i=0;i<160;i++){c.assertTrue(EmotionService.influence(p,mob,EmotionProfile.Emotion.FEAR,.003),"Eligible nearby mind influenced");EmotionService.finishFrame();}
        c.assertTrue(mob.getAttachedOrCreate(ModRegistries.EMOTIONS).value(EmotionProfile.Emotion.FEAR)>.65,"Fear actually increases");
        c.assertTrue(mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).getModifier(dev.metallicarts.MetallicArts.id("emotional_fear"))!=null,"Emotion has a real behavioral attribute cost");c.succeed();
    }
    @GameTest public void electrumOpeningRequiresPredictedDangerFuelAndCooldown(GameTestHelper c){
        var p=c.makeMockServerPlayerInLevel();var pos=c.absolutePos(new BlockPos(2,2,2));p.teleportTo(pos.getX(),pos.getY(),pos.getZ());p.setYRot(-90);p.setXRot(0);access(p,Metal.ELECTRUM);burn(p,Metal.ELECTRUM,100);AllomancyRuntime.prepare(p,100);
        c.assertFalse(AdvancedAllomancy.action(p,new ArtAction(1,0)),"No arbitrary sidestep without predicted danger");
        var arrow=EntityType.ARROW.create(c.getLevel(),EntitySpawnReason.COMMAND);arrow.setNoGravity(true);arrow.setPos(p.getX()+4,p.getEyeY(),p.getZ());arrow.setDeltaMovement(-1,0,0);c.getLevel().addFreshEntity(arrow);
        c.assertTrue(AdvancedAllomancy.threat(p,p.getBoundingBox(),10).isPresent(),"Actual projectile path predicted");
        long before=ReserveLedger.ordinary(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),Metal.ELECTRUM);
        c.assertTrue(AdvancedAllomancy.action(p,new ArtAction(1,0)),"Clear opening produces paid movement");
        c.assertTrue(ReserveLedger.ordinary(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),Metal.ELECTRUM)==before-1000&&Math.abs(p.getDeltaMovement().z)>.1,"Opening consumes fuel before lateral motion");
        c.assertFalse(AdvancedAllomancy.action(p,new ArtAction(1,0)),"Cannot spam the same prediction");c.succeed();
    }
    @GameTest public void temporalBubblesReallyChangeMobClocksAndCollapseAtBoundary(GameTestHelper c){
        var p=c.makeMockServerPlayerInLevel();var pos=c.absolutePos(new BlockPos(2,2,2));p.teleportTo(pos.getX(),pos.getY(),pos.getZ());
        access(p,Metal.CADMIUM);access(p,Metal.BENDALLOY);burn(p,Metal.CADMIUM,100);AllomancyRuntime.prepare(p,100);TemporalEngine.update(p,AllomancyRuntime.powers(p),100);
        c.assertFalse(AllomancyRuntime.request(p,15,1,100),"One owner cannot run contradictory bubbles");
        var mob=c.spawn(EntityType.VILLAGER,new BlockPos(2,2,2));int start=mob.tickCount;
        for(int n=0;n<4;n++)c.getLevel().tickNonPassenger(mob);c.assertTrue(mob.tickCount==start+1,"Cadmium advances actual mob clock once in four world ticks");
        AllomancyRuntime.stopMetal(p,Metal.CADMIUM);TemporalEngine.cleanup();burn(p,Metal.BENDALLOY,101);AllomancyRuntime.prepare(p,101);TemporalEngine.update(p,AllomancyRuntime.powers(p),101);
        start=mob.tickCount;c.getLevel().tickNonPassenger(mob);c.assertTrue(mob.tickCount==start+3,"Bendalloy advances actual mob clock three times");
        p.teleportTo(p.getX()+20,p.getY(),p.getZ());TemporalEngine.update(p,AllomancyRuntime.powers(p),102);
        c.assertTrue(TemporalEngine.rate(c.getLevel(),Vec3.atCenterOf(pos))==1,"Anchored bubble collapses when owner leaves");c.succeed();
    }
}
