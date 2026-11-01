package dev.metallicarts.network;

import dev.metallicarts.MetallicArts;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;

/** Selects an existing owner-authorized memory by ID; never supplies its label, contents or charge. */
public record MemoryAction(int action,int slot,int page,UUID memory) implements CustomPacketPayload {
    public static final Type<MemoryAction> TYPE=new Type<>(MetallicArts.id("memory_action_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf,MemoryAction> CODEC=StreamCodec.composite(
        ByteBufCodecs.VAR_INT,MemoryAction::action,ByteBufCodecs.VAR_INT,MemoryAction::slot,ByteBufCodecs.VAR_INT,MemoryAction::page,UUIDUtil.STREAM_CODEC,MemoryAction::memory,MemoryAction::new);
    public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
