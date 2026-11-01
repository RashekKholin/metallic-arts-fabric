package dev.metallicarts.network;
import dev.metallicarts.MetallicArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public record SystemsSnapshot(String status) implements CustomPacketPayload {
 public static final Type<SystemsSnapshot> TYPE=new Type<>(MetallicArts.id("advanced_systems_snapshot_v1"));
 public static final StreamCodec<RegistryFriendlyByteBuf,SystemsSnapshot> CODEC=StreamCodec.composite(ByteBufCodecs.stringUtf8(4096),SystemsSnapshot::status,SystemsSnapshot::new);
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
