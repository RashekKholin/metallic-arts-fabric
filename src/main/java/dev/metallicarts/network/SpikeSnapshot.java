package dev.metallicarts.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.equipment.ContactEquipment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Owner-only implantation previews; never sends donor Spirit Identity. */
public record SpikeSnapshot(ContactEquipment equipment,String message,boolean consent,int vulnerability) implements CustomPacketPayload {
    private static final Codec<SpikeSnapshot> DATA=RecordCodecBuilder.create(i->i.group(
        ContactEquipment.CODEC.fieldOf("equipment").forGetter(SpikeSnapshot::equipment),
        Codec.string(0,256).fieldOf("message").forGetter(SpikeSnapshot::message),
        Codec.BOOL.fieldOf("consent").forGetter(SpikeSnapshot::consent),
        Codec.intRange(0,12).fieldOf("vulnerability").forGetter(SpikeSnapshot::vulnerability)
    ).apply(i,SpikeSnapshot::new));
    public static final Type<SpikeSnapshot> TYPE=new Type<>(MetallicArts.id("spike_snapshot_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SpikeSnapshot> CODEC=ByteBufCodecs.fromCodecWithRegistries(DATA);
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
