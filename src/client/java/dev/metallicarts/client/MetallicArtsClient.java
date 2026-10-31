package dev.metallicarts.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.domain.Spiritweb;
import dev.metallicarts.network.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import dev.metallicarts.economy.EconomyContent;
import net.minecraft.client.gui.screens.MenuScreens;

public final class MetallicArtsClient implements ClientModInitializer {
    static Spiritweb snapshot;
    public static Spiritweb snapshot() { return snapshot; }
    @Override public void onInitializeClient() {
        MenuScreens.register(EconomyContent.TABLE_MENU, MetallurgyScreen::new);
        var category = KeyMapping.Category.register(MetallicArts.id("general"));
        KeyMapping open = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.metallicarts.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, category));
        ClientPlayNetworking.registerGlobalReceiver(SpiritwebSnapshot.TYPE, (payload, context) -> snapshot = payload.state());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> snapshot = null);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (open.consumeClick()) {
                if (client.player == null || client.screen != null) continue;
                if (ClientPlayNetworking.canSend(RequestSpiritweb.TYPE))
                    ClientPlayNetworking.send(new RequestSpiritweb(RequestSpiritweb.PROTOCOL));
                client.setScreen(new SpiritwebScreen());
            }
        });
    }
}
