package dev.metallicarts.network;

import dev.metallicarts.MetallicArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Toggle the server-resolved aimed target (0), or clear selection (1). No target IDs or coordinates. */
public record SelectionIntent(int action) implements CustomPacketPayload {
    public static final Type<SelectionIntent> TYPE=new Type<>(MetallicArts.id("selection_intent_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SelectionIntent> CODEC=StreamCodec.composite(
        ByteBufCodecs.VAR_INT,SelectionIntent::action,SelectionIntent::new);
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
