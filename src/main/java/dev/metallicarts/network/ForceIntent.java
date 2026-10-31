package dev.metallicarts.network;
import dev.metallicarts.MetallicArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
/** Held push/pull flags; aim and force are resolved by the server. Expires without heartbeat. */
public record ForceIntent(int flags) implements CustomPacketPayload {
    public static final Type<ForceIntent> TYPE=new Type<>(MetallicArts.id("force_intent_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ForceIntent> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,ForceIntent::flags,ForceIntent::new);
    public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
