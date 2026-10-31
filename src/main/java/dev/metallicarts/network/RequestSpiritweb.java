package dev.metallicarts.network;

import dev.metallicarts.MetallicArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Read-only request. No target UUID or client-supplied ownership state. */
public record RequestSpiritweb(int protocol) implements CustomPacketPayload {
    public static final int PROTOCOL = 1;
    public static final Type<RequestSpiritweb> TYPE = new Type<>(MetallicArts.id("request_spiritweb_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestSpiritweb> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, RequestSpiritweb::protocol, RequestSpiritweb::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
