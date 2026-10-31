package dev.metallicarts.domain;

import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MetalItemDataTest {
    @Test void keyedChargeSurvivesNbtRoundTrip() {
        var item = new MetalItemData(1, UUID.randomUUID(), Metal.GOLD, 100000, 0.99, 500, Optional.of(UUID.randomUUID()));
        var nbt = MetalItemData.CODEC.encodeStart(NbtOps.INSTANCE, item).getOrThrow();
        assertEquals(item, MetalItemData.CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow());
    }
    @Test void unkeyedIdentityIsExplicitlyAbsent() {
        var item = new MetalItemData(1, UUID.randomUUID(), Metal.IRON, 1, 1, 0, Optional.empty());
        var json = MetalItemData.CODEC.encodeStart(JsonOps.INSTANCE, item).getOrThrow();
        assertEquals(item, MetalItemData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }
    @Test void invalidMassPurityOrChargeRejected() {
        assertThrows(IllegalArgumentException.class, () -> new MetalItemData(1, UUID.randomUUID(), Metal.GOLD, 0, 1, 0, Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> new MetalItemData(1, UUID.randomUUID(), Metal.GOLD, 1, Double.NaN, 0, Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> new MetalItemData(1, UUID.randomUUID(), Metal.GOLD, 1, 1, -1, Optional.empty()));
    }
}
