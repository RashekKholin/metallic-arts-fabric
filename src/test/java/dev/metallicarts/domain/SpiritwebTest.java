package dev.metallicarts.domain;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SpiritwebTest {
    private final UUID identity = UUID.randomUUID();
    private PowerGrant grant(PowerGrant.Origin origin, UUID source, double strength) {
        return new PowerGrant(UUID.randomUUID(), Art.ALLOMANCY, Metal.STEEL, origin, source, strength);
    }
    private Spiritweb fixture() {
        return new Spiritweb(1, identity, List.of(grant(PowerGrant.Origin.INNATE, identity, 1)), Map.of(Metal.STEEL, 123456L));
    }
    @Test void jsonRoundTripRetainsIdentitySourcesAndReserves() {
        var state = fixture();
        var json = Spiritweb.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow();
        assertEquals(state, Spiritweb.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }
    @Test void nbtRoundTripRetainsData() {
        var state = fixture();
        var nbt = Spiritweb.CODEC.encodeStart(NbtOps.INSTANCE, state).getOrThrow();
        assertEquals(state, Spiritweb.CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow());
    }
    @Test void removingSpikeDoesNotEraseInnatePower() {
        UUID spike = UUID.randomUUID();
        var state = new Spiritweb(1, identity, List.of(grant(PowerGrant.Origin.INNATE, identity, 1), grant(PowerGrant.Origin.HEMALURGIC, spike, 2)), Map.of());
        assertEquals(3, state.strength(Art.ALLOMANCY, Metal.STEEL));
        assertEquals(1, state.withoutSource(spike).strength(Art.ALLOMANCY, Metal.STEEL));
        assertEquals(2, state.grants().size());
    }
    @Test void duplicateGrantIdsRejectedOnDecode() {
        var json = Spiritweb.CODEC.encodeStart(JsonOps.INSTANCE, fixture()).getOrThrow().getAsJsonObject();
        json.getAsJsonArray("grants").add(json.getAsJsonArray("grants").get(0).deepCopy());
        assertTrue(Spiritweb.CODEC.parse(JsonOps.INSTANCE, json).isError());
    }
    @Test void futureSchemaRejected() {
        var json = Spiritweb.CODEC.encodeStart(JsonOps.INSTANCE, fixture()).getOrThrow().getAsJsonObject();
        json.addProperty("schema", 2);
        assertTrue(Spiritweb.CODEC.parse(JsonOps.INSTANCE, json).isError());
    }
    @Test void negativeReserveRejected() {
        var json = Spiritweb.CODEC.encodeStart(JsonOps.INSTANCE, fixture()).getOrThrow().getAsJsonObject();
        json.getAsJsonObject("reserves").addProperty("steel", -1);
        assertTrue(Spiritweb.CODEC.parse(JsonOps.INSTANCE, json).isError());
    }
    @Test void unknownMetalAndEnumsReturnCodecErrors() {
        assertTrue(Metal.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("atium")).isError());
        assertTrue(Art.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("unknown")).isError());
    }
    @Test void immutableCollectionsCannotBeMutated() {
        var state = fixture();
        assertThrows(UnsupportedOperationException.class, () -> state.reserves().put(Metal.IRON, 1L));
        assertThrows(UnsupportedOperationException.class, () -> state.grants().clear());
    }
    @Test void newCharactersHaveIndependentIdentityAndNoPowers() {
        var a = Spiritweb.empty(); var b = Spiritweb.empty();
        assertNotEquals(a.identity(), b.identity());
        assertTrue(a.grants().isEmpty());
        assertEquals(0, a.strength(Art.ALLOMANCY, Metal.IRON));
    }
    @Test void rejectsInvalidStrength() {
        assertThrows(IllegalArgumentException.class, () -> grant(PowerGrant.Origin.INNATE, identity, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> grant(PowerGrant.Origin.INNATE, identity, 0));
    }
    @Test void allSixteenIdsRoundTripUniquely() {
        assertEquals(16, Arrays.stream(Metal.values()).map(Metal::id).distinct().count());
        for (Metal m : Metal.values()) assertEquals(m, Metal.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(m.id())).getOrThrow());
    }
}
