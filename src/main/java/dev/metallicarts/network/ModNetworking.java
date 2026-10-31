package dev.metallicarts.network;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.server.level.ServerPlayer;
import java.util.Map;
import java.util.WeakHashMap;

public final class ModNetworking {
    private static final Map<ServerPlayer, Integer> LAST_REQUEST = new WeakHashMap<>();
    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(RequestSpiritweb.TYPE, RequestSpiritweb.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SpiritwebSnapshot.TYPE, SpiritwebSnapshot.CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            handler.player.getAttachedOrCreate(ModRegistries.SPIRITWEB);
            send(handler.player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LAST_REQUEST.remove(handler.player));
        ServerPlayNetworking.registerGlobalReceiver(RequestSpiritweb.TYPE, (payload, context) -> {
            if (payload.protocol() != RequestSpiritweb.PROTOCOL) return;
            ServerPlayer player = context.player();
            int tick = context.server().getTickCount();
            Integer previous = LAST_REQUEST.get(player);
            if (previous != null && tick - previous < MetallicArts.config.snapshotCooldownTicks()) return;
            LAST_REQUEST.put(player, tick);
            send(player);
        });
    }
    public static void send(ServerPlayer player) {
        if (ServerPlayNetworking.canSend(player, SpiritwebSnapshot.TYPE))
            ServerPlayNetworking.send(player, new SpiritwebSnapshot(player.getAttachedOrCreate(ModRegistries.SPIRITWEB)));
    }
    private ModNetworking() {}
}
