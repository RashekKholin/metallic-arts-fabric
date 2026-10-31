package dev.metallicarts.domain;

import dev.metallicarts.allomancy.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class BurnEngineTest {
    private Spiritweb fuel(Metal metal,long amount) {
        UUID id=UUID.randomUUID();
        return new Spiritweb(1,UUID.randomUUID(),List.of(new PowerGrant(id,Art.ALLOMANCY,metal,PowerGrant.Origin.ADMIN,id,1)),Map.of(metal,amount));
    }
    @Test void noImplementedHandlerMeansNoBurnOrFuelConsumption() {
        var engine=new BurnEngine(Set.of());var state=fuel(Metal.STEEL,1000);
        assertFalse(engine.request(state,Metal.STEEL,BurnMode.BURN));
        var step=engine.step(state,AllomancyConfig.DEFAULT);
        assertEquals(state,step.state());assertTrue(step.strengths().isEmpty());
    }
    @Test void ownershipAndFuelBothRequired() {
        var engine=new BurnEngine(Set.of(Metal.STEEL));
        assertFalse(engine.request(ReserveLedger.credit(Spiritweb.empty(),Metal.STEEL,1000),Metal.STEEL,BurnMode.BURN));
        assertFalse(engine.request(fuel(Metal.STEEL,0),Metal.STEEL,BurnMode.BURN));
        assertFalse(engine.request(fuel(Metal.STEEL,1000),Metal.GOLD,BurnMode.FLARE));
    }
    @Test void costIsPaidBeforeEffectsAndExhaustionStopsAutomatically() {
        var engine=new BurnEngine(Set.of(Metal.STEEL));var state=fuel(Metal.STEEL,9);
        assertTrue(engine.request(state,Metal.STEEL,BurnMode.BURN));
        var first=engine.step(state,AllomancyConfig.DEFAULT);
        assertEquals(4,first.state().reserves().get(Metal.STEEL));assertEquals(1,first.strengths().get(Metal.STEEL));
        var last=engine.step(first.state(),AllomancyConfig.DEFAULT);
        assertEquals(first.state(),last.state());assertTrue(last.strengths().isEmpty());
        assertEquals(BurnMode.OFF,engine.mode(Metal.STEEL));
    }
    @Test void revocationStopsBeforeAnotherEffectOrDebit() {
        var engine=new BurnEngine(Set.of(Metal.STEEL));var state=fuel(Metal.STEEL,1000);
        engine.request(state,Metal.STEEL,BurnMode.BURN);
        var revoked=state.withoutSource(state.grants().getFirst().source());
        var step=engine.step(revoked,AllomancyConfig.DEFAULT);
        assertEquals(revoked,step.state());assertTrue(step.strengths().isEmpty());
        assertEquals(BurnMode.OFF,engine.mode(Metal.STEEL));
    }
    @Test void simultaneousFlareAndBurnHaveIndependentCosts() {
        var steel=fuel(Metal.STEEL,1000);var pewter=fuel(Metal.PEWTER,1000);
        var state=ReserveLedger.credit(ReserveLedger.grant(steel,pewter.grants().getFirst()),Metal.PEWTER,1000);
        var engine=new BurnEngine(Set.of(Metal.STEEL,Metal.PEWTER));
        engine.request(state,Metal.STEEL,BurnMode.FLARE);engine.request(state,Metal.PEWTER,BurnMode.BURN);
        var step=engine.step(state,AllomancyConfig.DEFAULT);
        assertEquals(980,step.state().reserves().get(Metal.STEEL));
        assertEquals(990,step.state().reserves().get(Metal.PEWTER));
        assertEquals(1.8,step.strengths().get(Metal.STEEL));
        assertEquals(1,step.strengths().get(Metal.PEWTER));
    }
    @Test void investedOnlyMetalCannotActivateAndResetNeverRestoresBurning() {
        var state=fuel(Metal.STEEL,1000);
        var payload=new MetalItemData(1,UUID.randomUUID(),Metal.STEEL,1000,1,40,Optional.empty());
        var invested=new Spiritweb(1,state.identity(),state.grants(),state.reserves(),List.of(payload));
        var engine=new BurnEngine(Set.of(Metal.STEEL));
        assertFalse(engine.request(invested,Metal.STEEL,BurnMode.BURN));
        engine.request(state,Metal.STEEL,BurnMode.BURN);engine.stopAll();
        assertTrue(engine.step(state,AllomancyConfig.DEFAULT).strengths().isEmpty());
        assertEquals(BurnMode.OFF,new BurnEngine(Set.of(Metal.STEEL)).mode(Metal.STEEL));
    }
}
