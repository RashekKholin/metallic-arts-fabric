package dev.metallicarts.network;
import dev.metallicarts.MetallicArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public record TossCoin() implements CustomPacketPayload {
    public static final Type<TossCoin> TYPE=new Type<>(MetallicArts.id("toss_coin_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf,TossCoin> CODEC=StreamCodec.unit(new TossCoin());
    public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
