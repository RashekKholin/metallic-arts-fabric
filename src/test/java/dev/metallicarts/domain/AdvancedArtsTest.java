package dev.metallicarts.domain;

import com.mojang.serialization.JsonOps;
import dev.metallicarts.allomancy.*;
import dev.metallicarts.arts.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AdvancedArtsTest {
    @Test void everyStandardMetalCanBeIndependentlyOwnedPaidAndStopped(){
        var grants=new ArrayList<PowerGrant>();var reserves=new EnumMap<Metal,Long>(Metal.class);
        for(var m:Metal.values()){UUID id=UUID.randomUUID();grants.add(new PowerGrant(id,Art.ALLOMANCY,m,PowerGrant.Origin.ADMIN,id,1));reserves.put(m,1000L);}
        var state=new Spiritweb(1,UUID.randomUUID(),grants,reserves);var engine=new BurnEngine(Set.copyOf(AllomancyConfig.SUPPORTED));
        for(var m:Metal.values())assertTrue(engine.request(state,m,BurnMode.BURN));var step=engine.step(state,AllomancyConfig.DEFAULT);
        assertEquals(16,step.strengths().size());for(var m:Metal.values())assertTrue(step.state().reserves().get(m)<1000);
        engine.stopAll();assertTrue(engine.step(step.state(),AllomancyConfig.DEFAULT).strengths().isEmpty());
    }
    @Test void explicitReserveDestructionPreservesSourcesButConsumesSelectedInvestedMaterial(){
        UUID id=UUID.randomUUID();var grant=new PowerGrant(id,Art.ALLOMANCY,Metal.ALUMINUM,PowerGrant.Origin.ADMIN,id,1);
        var invested=new MetalItemData(1,UUID.randomUUID(),Metal.IRON,100,1,99,Optional.of(UUID.randomUUID()));
        var state=new Spiritweb(1,UUID.randomUUID(),List.of(grant),Map.of(Metal.IRON,200L,Metal.STEEL,500L),List.of(invested));
        var emptied=AdvancedAllomancy.drain(state,Set.of(Metal.IRON));
        assertEquals(500,emptied.reserves().get(Metal.STEEL));assertFalse(emptied.reserves().containsKey(Metal.IRON));assertTrue(emptied.investedReserves().isEmpty());
        assertEquals(state.grants(),emptied.grants());assertEquals(state.identity(),emptied.identity());assertTrue(ReserveLedger.valid(emptied));
    }
    @Test void combinedEmotionChangesCancelBeforeBoundsAndRecoverWithoutChangingBaseline(){
        var profile=new EmotionProfile(Map.of(EmotionProfile.Emotion.FEAR,.99),.5,100);
        var combined=profile.influence(Map.of(EmotionProfile.Emotion.FEAR,0.0),100);
        assertEquals(.99,combined.value(EmotionProfile.Emotion.FEAR));
        assertEquals(.25,combined.settle(10000).value(EmotionProfile.Emotion.FEAR),.000001);
        assertEquals(0,combined.settle(10000).awareness());
        assertEquals(combined,EmotionProfile.CODEC.parse(JsonOps.INSTANCE,EmotionProfile.CODEC.encodeStart(JsonOps.INSTANCE,combined).getOrThrow()).getOrThrow());
    }
    @Test void offloadedMemoryIsNotAccessibleOrDuplicableAndRestoresExactly(){
        var entry=new MemoryEntry(UUID.randomUUID(),"discovery","Found a village","minecraft:overworld",1,80,2,10);
        var original=new ArtsJournal(List.of(entry),List.of(entry),List.of("village:1"));var stored=original.forget(entry.id());
        assertTrue(stored.memories().isEmpty());assertTrue(stored.history().isEmpty());assertTrue(stored.discoveries().contains("village:1"));
        assertEquals(entry,stored.remember(entry).memories().getFirst());assertThrows(IllegalArgumentException.class,()->original.remember(entry));
        assertEquals(stored,ArtsJournal.CODEC.parse(JsonOps.INSTANCE,ArtsJournal.CODEC.encodeStart(JsonOps.INSTANCE,stored).getOrThrow()).getOrThrow());
    }
}
