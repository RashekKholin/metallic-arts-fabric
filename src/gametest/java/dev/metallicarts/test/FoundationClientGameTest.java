package dev.metallicarts.test;

import dev.metallicarts.client.SpiritwebScreen;
import dev.metallicarts.client.MetallicArtsClient;
import dev.metallicarts.domain.*;
import dev.metallicarts.registry.ModRegistries;
import dev.metallicarts.network.ModNetworking;
import net.minecraft.world.item.*;
import java.util.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;

public final class FoundationClientGameTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        UUID identity = UUID.randomUUID();
        UUID source = UUID.randomUUID();
        Spiritweb expected = new Spiritweb(1, identity, List.of(new PowerGrant(UUID.randomUUID(), Art.ALLOMANCY,
            Metal.STEEL, PowerGrant.Origin.HEMALURGIC, source, 1.5)), Map.of(Metal.STEEL, 12345L));
        MetalItemData itemData = new MetalItemData(1, UUID.randomUUID(), Metal.GOLD, 100000, 0.99, 321, Optional.of(identity));
        net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave save;
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            save = singleplayer.getWorldSave();
            singleplayer.getServer().runOnServer(server -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                player.setAttached(ModRegistries.SPIRITWEB, expected);
                var item = new ItemStack(Items.GOLD_INGOT);
                item.set(ModRegistries.METAL_DATA, itemData);
                player.getInventory().setItem(0, item);
                ModNetworking.send(player);
            });
            context.waitFor(client -> expected.equals(MetallicArtsClient.snapshot()));
            singleplayer.getClientLevel().waitForChunksRender();
            context.runOnClient(client -> client.setScreen(new SpiritwebScreen()));
            context.waitTicks(140); // Let vanilla recipe toasts clear before visual verification.
            context.takeScreenshot("metallicarts-spiritweb");
            context.clickScreenButton("Guide");context.waitForScreen(dev.metallicarts.client.GuideScreen.class);context.clickScreenButton("Next");context.takeScreenshot("metallicarts-survival-guide");context.clickScreenButton("Close");
        }
        // Full disconnect + integrated server shutdown + disk reload, not just a codec round-trip.
        try (TestSingleplayerContext reopened = save.open()) {
            reopened.getServer().runOnServer(server -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                if (!expected.equals(player.getAttached(ModRegistries.SPIRITWEB))) throw new AssertionError("Spiritweb lost on restart");
                if (!itemData.equals(player.getInventory().getItem(0).get(ModRegistries.METAL_DATA))) throw new AssertionError("Item data lost on restart");
            });
            context.waitFor(client -> expected.equals(MetallicArtsClient.snapshot()));
        }
        // Dedicated server path exercises the same common registration and owner-only payloads.
        Properties serverProperties = new Properties();
        serverProperties.setProperty("server-ip", "127.0.0.1");
        serverProperties.setProperty("server-port", "25568");
        try (TestDedicatedServerContext server = context.worldBuilder().createServer(serverProperties)) {
            try (var connection = server.connect()) {
                server.runOnServer(mcServer -> {
                    var player = mcServer.getPlayerList().getPlayers().getFirst();
                    player.setAttached(ModRegistries.SPIRITWEB, expected);
                    ModNetworking.send(player);
                });
                context.waitFor(client -> expected.equals(MetallicArtsClient.snapshot()));
            }
            // Logout/login reads saved player data through the dedicated server's player storage.
            try (var connection = server.connect()) {
                server.runOnServer(mcServer -> {
                    var player = mcServer.getPlayerList().getPlayers().getFirst();
                    if (!expected.equals(player.getAttached(ModRegistries.SPIRITWEB))) throw new AssertionError("Dedicated relog lost spiritweb");
                });
                context.waitFor(client -> expected.equals(MetallicArtsClient.snapshot()));
            }
        }
    }
}

