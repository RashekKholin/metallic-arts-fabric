package dev.metallicarts.network;

import dev.metallicarts.MetallicArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Small bounded intent. Donor, held item, charge and grant source are resolved by the server. */
public record SpikeIntent(int action,int point,int power) implements CustomPacketPayload {
    public static final Type<SpikeIntent> TYPE=new Type<>(MetallicArts.id("spike_intent_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SpikeIntent> CODEC=StreamCodec.composite(
        ByteBufCodecs.VAR_INT,SpikeIntent::action,ByteBufCodecs.VAR_INT,SpikeIntent::point,ByteBufCodecs.VAR_INT,SpikeIntent::power,SpikeIntent::new);
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
