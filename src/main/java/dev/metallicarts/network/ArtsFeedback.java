package dev.metallicarts.network;

import dev.metallicarts.MetallicArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Owner-only bounded sensory/mental feedback and readable journal; never other players' raw contracts. */
public record ArtsFeedback(String cue,String journal) implements CustomPacketPayload {
    public static final Type<ArtsFeedback> TYPE=new Type<>(MetallicArts.id("arts_feedback_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ArtsFeedback> CODEC=StreamCodec.composite(ByteBufCodecs.stringUtf8(2048),ArtsFeedback::cue,ByteBufCodecs.stringUtf8(24000),ArtsFeedback::journal,ArtsFeedback::new);
    public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
