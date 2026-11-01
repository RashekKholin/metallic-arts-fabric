package dev.metallicarts.network;
import dev.metallicarts.MetallicArts;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;
/** Only public appearance. Never includes spirit Identity, stolen provenance, charge or private memories. */
public record PublicConstruct(UUID player,int kind,int shape,boolean collapsed) implements CustomPacketPayload {
 public static final Type<PublicConstruct> TYPE=new Type<>(MetallicArts.id("public_construct_v1"));
 public static final StreamCodec<RegistryFriendlyByteBuf,PublicConstruct> CODEC=StreamCodec.composite(UUIDUtil.STREAM_CODEC,PublicConstruct::player,ByteBufCodecs.VAR_INT,PublicConstruct::kind,ByteBufCodecs.VAR_INT,PublicConstruct::shape,ByteBufCodecs.BOOL,PublicConstruct::collapsed,PublicConstruct::new);
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
