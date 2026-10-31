package dev.metallicarts.test;

import dev.metallicarts.domain.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import java.util.*;

public final class FoundationGameTest {
    @GameTest
    public void attachmentOwnership(GameTestHelper context) {
        var player = context.makeMockServerPlayerInLevel();
        var original = player.getAttachedOrCreate(ModRegistries.SPIRITWEB);
        UUID spike = UUID.randomUUID();
        var grant = new PowerGrant(UUID.randomUUID(), Art.ALLOMANCY, Metal.STEEL, PowerGrant.Origin.HEMALURGIC, spike, 1);
        var updated = new Spiritweb(1, original.identity(), List.of(grant), Map.of(Metal.STEEL, 500L));
        player.setAttached(ModRegistries.SPIRITWEB, updated);
        context.assertValueEqual(updated, player.getAttached(ModRegistries.SPIRITWEB), "Attachment update");
        player.setAttached(ModRegistries.SPIRITWEB, updated.withoutSource(spike));
        context.assertTrue(player.getAttached(ModRegistries.SPIRITWEB).grants().isEmpty(), "Source removal");
        context.succeed();
    }
}
