package dev.metallicarts.network;
import dev.metallicarts.MetallicArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
/** Toggle a nearby server-picked physical load; never supplies a block or inventory stack. */
public record CargoIntent() implements CustomPacketPayload {
    public static final Type<CargoIntent> TYPE=new Type<>(MetallicArts.id("cargo_intent_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf,CargoIntent> CODEC=StreamCodec.unit(new CargoIntent());
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
