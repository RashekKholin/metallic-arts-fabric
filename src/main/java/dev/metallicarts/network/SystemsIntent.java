package dev.metallicarts.network;
import dev.metallicarts.MetallicArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
/** Intent only. Held source, contact, anatomy, donor and all charge are independently resolved on server. */
public record SystemsIntent(int action,int selection,int slot,boolean intent) implements CustomPacketPayload {
 public static final Type<SystemsIntent> TYPE=new Type<>(MetallicArts.id("advanced_systems_intent_v1"));
 public static final StreamCodec<RegistryFriendlyByteBuf,SystemsIntent> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,SystemsIntent::action,ByteBufCodecs.VAR_INT,SystemsIntent::selection,ByteBufCodecs.VAR_INT,SystemsIntent::slot,ByteBufCodecs.BOOL,SystemsIntent::intent,SystemsIntent::new);
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
