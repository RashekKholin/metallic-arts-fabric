package dev.metallicarts.network;

import dev.metallicarts.MetallicArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Actions: equip held item, unequip, change mode/rate, request own equipment. No supplied item/charge. */
public record ContactIntent(int action,int slot,int sense,int mode,int rate) implements CustomPacketPayload {
    public static final Type<ContactIntent> TYPE=new Type<>(MetallicArts.id("contact_intent_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ContactIntent> CODEC=StreamCodec.composite(
        ByteBufCodecs.VAR_INT,ContactIntent::action,ByteBufCodecs.VAR_INT,ContactIntent::slot,
        ByteBufCodecs.VAR_INT,ContactIntent::sense,ByteBufCodecs.VAR_INT,ContactIntent::mode,
        ByteBufCodecs.VAR_INT,ContactIntent::rate,ContactIntent::new);
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
