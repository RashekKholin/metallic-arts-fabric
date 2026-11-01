package dev.metallicarts.domain;

import dev.metallicarts.allomancy.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ReserveLedgerTest {
    private MetalItemData invested(long mass){return new MetalItemData(1,UUID.randomUUID(),Metal.IRON,mass,1,7,Optional.of(UUID.randomUUID()));}
    @Test void investedIsSubsetNotExtraFuel(){
        var charge=invested(10000);var state=new Spiritweb(1,UUID.randomUUID(),List.of(),Map.of(Metal.IRON,15000L),List.of(charge));
        assertEquals(5000,ReserveLedger.ordinary(state,Metal.IRON));
        var after=ReserveLedger.debit(state,Metal.IRON,5000);
        assertEquals(10000,after.reserves().get(Metal.IRON));assertEquals(List.of(charge),after.investedReserves());
        assertThrows(IllegalArgumentException.class,()->ReserveLedger.debit(after,Metal.IRON,1));
    }
    @Test void mismatchedAndDuplicateInvestedRecordsRefused(){
        var charge=invested(10000);
        assertFalse(ReserveLedger.valid(new Spiritweb(1,UUID.randomUUID(),List.of(),Map.of(),List.of(charge))));
        assertFalse(ReserveLedger.valid(new Spiritweb(1,UUID.randomUUID(),List.of(),Map.of(Metal.IRON,20000L),List.of(charge,charge))));
    }
    @Test void fractionalCostsAreExactOverTwentyTicks(){
        for(long rate:List.of(1L,7L,100L,203L,400L)){
            var budget=new BurnBudget();long spent=0;
            for(int tick=0;tick<20;tick++)spent+=budget.charge(rate);
            assertEquals(rate,spent);
        }
    }
    @Test void repeatedTogglesCannotMakeFuelFree(){
        var budget=new BurnBudget();long spent=0;
        for(int n=0;n<20;n++){spent+=budget.charge(1);budget.reset();}
        assertEquals(20,spent);
    }
    @Test void invalidDebitsAndRatesRefused(){
        assertThrows(IllegalArgumentException.class,()->ReserveLedger.debit(Spiritweb.empty(),Metal.IRON,-1));
        assertThrows(IllegalArgumentException.class,()->new BurnBudget().charge(0));
    }
    @Test void saveRoundTripPreservesMixedInvestedAndOrdinaryMass(){
        var payload=invested(10000);
        var state=new Spiritweb(1,UUID.randomUUID(),List.of(),Map.of(Metal.IRON,15000L),List.of(payload));
        var after=ReserveLedger.debit(state,Metal.IRON,321);
        var json=Spiritweb.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,after).getOrThrow();
        var loaded=Spiritweb.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,json).getOrThrow();
        assertEquals(after,loaded);assertEquals(4679,ReserveLedger.ordinary(loaded,Metal.IRON));
        json.getAsJsonObject().getAsJsonObject("reserves").addProperty("iron",0);
        assertTrue(Spiritweb.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,json).isError());
    }
    @Test void codecRejectsDuplicatedInvestedInstances(){
        var state=dev.metallicarts.economy.Ingestion.accept(Spiritweb.empty(),invested(10000));
        var json=Spiritweb.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,state).getOrThrow().getAsJsonObject();
        json.getAsJsonArray("invested_reserves").add(json.getAsJsonArray("invested_reserves").get(0).deepCopy());
        json.getAsJsonObject("reserves").addProperty("iron",20000);
        assertTrue(Spiritweb.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,json).isError());
    }
    @Test void capacityAndLargeInvestedMassCannotOverflowOrBecomeFuel(){
        var large=invested(1_000_000_000_000L);
        var invalid=new Spiritweb(1,UUID.randomUUID(),List.of(),Map.of(Metal.IRON,Spiritweb.MAX_RESERVE),List.of(large));
        assertFalse(ReserveLedger.valid(invalid));
        assertFalse(dev.metallicarts.economy.Ingestion.canAccept(invalid,invested(1)));
        var full=ReserveLedger.credit(Spiritweb.empty(),Metal.IRON,Spiritweb.MAX_RESERVE);
        assertThrows(IllegalArgumentException.class,()->ReserveLedger.credit(full,Metal.IRON,1));
        assertThrows(IllegalArgumentException.class,()->ReserveLedger.credit(full,Metal.IRON,Long.MAX_VALUE));
    }
    @Test void revokeOnlyRemovesChosenGrantAndNeverFuelOrOtherSourceGrants(){
        UUID source=UUID.randomUUID();
        var first=new PowerGrant(UUID.randomUUID(),Art.ALLOMANCY,Metal.IRON,PowerGrant.Origin.ADMIN,source,1);
        var second=new PowerGrant(UUID.randomUUID(),Art.ALLOMANCY,Metal.IRON,PowerGrant.Origin.INNATE,source,2);
        var original=Spiritweb.empty();
        var state=ReserveLedger.credit(ReserveLedger.grant(ReserveLedger.grant(original,first),second),Metal.IRON,5000);
        var after=ReserveLedger.revoke(state,first.id());
        assertEquals(List.of(second),after.grants());assertEquals(2,after.strength(Art.ALLOMANCY,Metal.IRON));
        assertEquals(state.reserves(),after.reserves());assertEquals(original.identity(),after.identity());
        assertThrows(IllegalArgumentException.class,()->ReserveLedger.revoke(after,first.id()));
        assertThrows(IllegalArgumentException.class,()->ReserveLedger.grant(after,second));
    }
    @Test void changingRatesNeverCreatesUnpaidFractionalFuel(){
        var budget=new BurnBudget();long paid=0,totalTwentieths=0;
        for(int i=0;i<10000;i++) {
            long rate=i%2==0?7:203;
            totalTwentieths+=rate;paid+=budget.charge(rate);
            assertEquals((totalTwentieths+19)/20,paid);
        }
    }
    @Test void configUsesOnlyExplicitSupportedRatesAndValidatedBounds(){
        var config=AllomancyConfig.DEFAULT;
        assertEquals(0,config.rate(Metal.STEEL,BurnMode.OFF));
        assertEquals(100,config.rate(Metal.STEEL,BurnMode.BURN));
        assertEquals(400,config.rate(Metal.STEEL,BurnMode.FLARE));
        assertEquals(100,config.rate(Metal.GOLD,BurnMode.BURN));
        assertEquals(16,config.rates().size());
        assertThrows(IllegalArgumentException.class,()->BurnMode.from(-1));
        assertThrows(IllegalArgumentException.class,()->BurnMode.from(3));
        assertThrows(IllegalArgumentException.class,()->new AllomancyConfig(1,Map.of("steel",100L),4,1.8,24,48,14,2.4,4));
    }
}
