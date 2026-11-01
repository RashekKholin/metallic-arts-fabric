package dev.metallicarts.network;

import dev.metallicarts.MetallicArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Explicit action/selection, not client-supplied effects, coordinates, targets or stored memories. */
public record ArtAction(int action,int value) implements CustomPacketPayload {
    public static final Type<ArtAction> TYPE=new Type<>(MetallicArts.id("art_action_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ArtAction> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,ArtAction::action,ByteBufCodecs.VAR_INT,ArtAction::value,ArtAction::new);
    public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
