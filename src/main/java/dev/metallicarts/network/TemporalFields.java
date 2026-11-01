package dev.metallicarts.network;
import dev.metallicarts.MetallicArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import java.util.*;
/** Public field surfaces only. No owner UUID, inventory, grant or private character data. */
public record TemporalFields(List<Field> fields) implements CustomPacketPayload {
 public record Field(Vec3 center,float radius,boolean fast){public static final StreamCodec<RegistryFriendlyByteBuf,Field> CODEC=StreamCodec.composite(Vec3.STREAM_CODEC,Field::center,ByteBufCodecs.FLOAT,Field::radius,ByteBufCodecs.BOOL,Field::fast,Field::new);public Field{if(!Float.isFinite(radius)||radius<=0||radius>24)throw new IllegalArgumentException("Invalid field radius");}}
 public static final Type<TemporalFields> TYPE=new Type<>(MetallicArts.id("temporal_fields_v1"));
 public static final StreamCodec<RegistryFriendlyByteBuf,TemporalFields> CODEC=Field.CODEC.apply(ByteBufCodecs.list(32)).map(TemporalFields::new,TemporalFields::fields);
 public TemporalFields{fields=List.copyOf(fields);if(fields.size()>32)throw new IllegalArgumentException("Too many field surfaces");}
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
