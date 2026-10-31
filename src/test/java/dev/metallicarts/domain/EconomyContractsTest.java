package dev.metallicarts.domain;

import dev.metallicarts.economy.*;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class EconomyContractsTest {
    private MetalItemData data(long mass,long charge,Optional<UUID> key,double purity) {
        return new MetalItemData(1,UUID.randomUUID(),Metal.IRON,mass,purity,charge,key);
    }
    @Test void ingestionAddsFuelWithoutGrantingOrActivatingPowers() {
        var state=Ingestion.accept(Spiritweb.empty(),data(Materials.PORTION_MASS,0,Optional.empty(),1));
        assertEquals(Materials.PORTION_MASS,state.reserves().get(Metal.IRON));
        assertTrue(state.grants().isEmpty()); assertTrue(state.investedReserves().isEmpty());
    }
    @Test void investedFuelRetainsIdentityChargeAndInstance() {
        var payload=data(10000,31,Optional.of(UUID.randomUUID()),1);
        var state=Ingestion.accept(Spiritweb.empty(),payload);
        var json=Spiritweb.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();
        assertEquals(List.of(payload),Spiritweb.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow().investedReserves());
    }
    @Test void phaseOneSaveHasEmptyInvestedCollection() {
        var json=Spiritweb.CODEC.encodeStart(JsonOps.INSTANCE,Spiritweb.empty()).getOrThrow().getAsJsonObject();
        json.remove("invested_reserves");
        assertTrue(Spiritweb.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow().investedReserves().isEmpty());
    }
    @Test void fullOrUnsafeReservesRejectWholeVial() {
        var state=new Spiritweb(1,UUID.randomUUID(),List.of(),Map.of(Metal.IRON,Spiritweb.MAX_RESERVE-1));
        assertFalse(Ingestion.canAccept(state,data(2,0,Optional.empty(),1)));
        assertFalse(Ingestion.canAccept(Spiritweb.empty(),data(1,0,Optional.empty(),0.97)));
        assertThrows(IllegalArgumentException.class,()->Ingestion.accept(state,data(2,0,Optional.empty(),1)));
    }
    @Test void qualityDistinguishesExactImperfectAndInvalid() {
        var target=Map.of("iron",98,"carbon",2);
        assertEquals(1,BatchComposition.purity(target,target));
        assertEquals("Imperfect",new BatchComposition(Map.of("iron",97,"carbon",3),BatchComposition.purity(Map.of("iron",97,"carbon",3),target)).quality());
        assertEquals("Invalid",new BatchComposition(Map.of("iron",50,"carbon",50),BatchComposition.purity(Map.of("iron",50,"carbon",50),target)).quality());
    }
    @Test void ratioScaleDoesNotAlterPurity() {
        assertEquals(1,BatchComposition.purity(Map.of("tin",182,"lead",18),Map.of("tin",91,"lead",9)));
    }
    @Test void sameInvestedInstanceCannotBeIngestedTwice() {
        var payload=data(10000,7,Optional.of(UUID.randomUUID()),1);
        var state=Ingestion.accept(Spiritweb.empty(),payload);
        assertFalse(Ingestion.canAccept(state,payload));
    }
}
