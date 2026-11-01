package dev.metallicarts.network;
import dev.metallicarts.MetallicArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import java.util.*;
/** Owner-only nearby physical endpoints; contains no inventories, grants, Identity or charge. */
public record MetalSight(List<Dot> sources) implements CustomPacketPayload {
 public record Dot(int entityId,Vec3 position,boolean selected){public static final StreamCodec<RegistryFriendlyByteBuf,Dot> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Dot::entityId,Vec3.STREAM_CODEC,Dot::position,ByteBufCodecs.BOOL,Dot::selected,Dot::new);}
 public static final Type<MetalSight> TYPE=new Type<>(MetallicArts.id("metal_sight_v2"));
 public static final StreamCodec<RegistryFriendlyByteBuf,MetalSight> CODEC=Dot.CODEC.apply(ByteBufCodecs.list(64)).map(MetalSight::new,MetalSight::sources);
 public MetalSight{sources=List.copyOf(sources);if(sources.size()>64)throw new IllegalArgumentException("Too many metal endpoints");}
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
