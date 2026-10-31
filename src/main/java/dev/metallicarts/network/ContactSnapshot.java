package dev.metallicarts.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.equipment.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.*;

/** Owner-only item previews and controls. Identity keys remain in the server ledger. */
public record ContactSnapshot(ContactEquipment equipment,List<Integer> modes,List<Integer> rates,String message) implements CustomPacketPayload {
    private static final Codec<ContactSnapshot> DATA=RecordCodecBuilder.create(i->i.group(
        ContactEquipment.CODEC.fieldOf("equipment").forGetter(ContactSnapshot::equipment),
        Codec.intRange(0,2).listOf(12,12).fieldOf("modes").forGetter(ContactSnapshot::modes),
        Codec.intRange(1,5).listOf(12,12).fieldOf("rates").forGetter(ContactSnapshot::rates),
        Codec.string(0,256).fieldOf("message").forGetter(ContactSnapshot::message)
    ).apply(i,ContactSnapshot::new));
    public static final Type<ContactSnapshot> TYPE=new Type<>(MetallicArts.id("contact_snapshot_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ContactSnapshot> CODEC=ByteBufCodecs.fromCodecWithRegistries(DATA);
    public ContactSnapshot{modes=List.copyOf(modes);rates=List.copyOf(rates);}
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
