package dev.metallicarts.hemalurgy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.equipment.AssetToken;

/** Public item preview; private donor Identity and extraction provenance stay in SavedData. */
public record SpikeData(AssetToken token,StolenPower power,double potency,boolean blood,boolean drained){
    public static final Codec<SpikeData> CODEC=RecordCodecBuilder.create(i->i.group(
        AssetToken.CODEC.fieldOf("token").forGetter(SpikeData::token),StolenPower.CODEC.fieldOf("power").forGetter(SpikeData::power),
        Codec.doubleRange(0,1000).fieldOf("potency").forGetter(SpikeData::potency),Codec.BOOL.fieldOf("blood").forGetter(SpikeData::blood),
        Codec.BOOL.fieldOf("drained").forGetter(SpikeData::drained)
    ).apply(i,SpikeData::new));
    public static SpikeData of(SpikeAsset a){return new SpikeData(a.token(),a.power(),a.potency(),a.blood(),a.drained());}
}
