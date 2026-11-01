package dev.metallicarts.test;

import dev.metallicarts.MetallicArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Test mod only; excluded from installable jars. Never registered in ordinary runs. */
public final class MultiplayerProbe {
    public record Instruction(int sequence,String action) implements CustomPacketPayload {
        public static final Type<Instruction> TYPE=new Type<>(MetallicArts.id("test_instruction"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Instruction> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Instruction::sequence,ByteBufCodecs.stringUtf8(128),Instruction::action,Instruction::new);
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record Reply(int sequence,String data) implements CustomPacketPayload {
        public static final Type<Reply> TYPE=new Type<>(MetallicArts.id("test_reply"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Reply> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Reply::sequence,ByteBufCodecs.stringUtf8(4096),Reply::data,Reply::new);
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    private MultiplayerProbe(){}
}
