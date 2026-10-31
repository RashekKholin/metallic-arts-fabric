package dev.metallicarts.network;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.domain.Spiritweb;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Sent only to its owner. Persistence and network schema share bounded immutable codecs. */
public record SpiritwebSnapshot(Spiritweb state) implements CustomPacketPayload {
    public static final Type<SpiritwebSnapshot> TYPE = new Type<>(MetallicArts.id("spiritweb_snapshot_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpiritwebSnapshot> CODEC = StreamCodec.composite(
        ByteBufCodecs.fromCodec(Spiritweb.CODEC), SpiritwebSnapshot::state, SpiritwebSnapshot::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
