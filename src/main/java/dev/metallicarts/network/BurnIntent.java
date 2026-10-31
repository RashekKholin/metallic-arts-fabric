package dev.metallicarts.network;
import dev.metallicarts.MetallicArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
/** Fixed-size intent: metal ordinal and OFF/BURN/FLARE. No ownership or quantities. */
public record BurnIntent(int metal,int mode) implements CustomPacketPayload {
    public static final Type<BurnIntent> TYPE=new Type<>(MetallicArts.id("burn_intent_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf,BurnIntent> CODEC=StreamCodec.composite(
        ByteBufCodecs.VAR_INT,BurnIntent::metal,ByteBufCodecs.VAR_INT,BurnIntent::mode,BurnIntent::new);
    public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
