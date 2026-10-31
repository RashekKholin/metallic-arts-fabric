package dev.metallicarts.network;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.MetallicArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Paid Feruchemical sensory gains plus nearby/consumed sensory cues, sent only to their owner. */
public record SenseStatus(double sight,double hearing,double touch,double smell,double taste,String cue) implements CustomPacketPayload {
    private static final Codec<SenseStatus> DATA=RecordCodecBuilder.create(i->i.group(
        Codec.doubleRange(.25,5).fieldOf("sight").forGetter(SenseStatus::sight),
        Codec.doubleRange(.25,5).fieldOf("hearing").forGetter(SenseStatus::hearing),
        Codec.doubleRange(.25,5).fieldOf("touch").forGetter(SenseStatus::touch),
        Codec.doubleRange(.25,5).fieldOf("smell").forGetter(SenseStatus::smell),
        Codec.doubleRange(.25,5).fieldOf("taste").forGetter(SenseStatus::taste),
        Codec.string(0,256).fieldOf("cue").forGetter(SenseStatus::cue)
    ).apply(i,SenseStatus::new));
    public static final SenseStatus NORMAL=new SenseStatus(1,1,1,1,1,"");
    public static final Type<SenseStatus> TYPE=new Type<>(MetallicArts.id("sense_status_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SenseStatus> CODEC=ByteBufCodecs.fromCodecWithRegistries(DATA);
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
