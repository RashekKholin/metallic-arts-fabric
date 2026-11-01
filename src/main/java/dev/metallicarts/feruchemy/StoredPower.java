package dev.metallicarts.feruchemy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.domain.*;

/** A single typed ability and its baseline strength, never a fungible grant of every Art. */
public record StoredPower(Art art,Metal metal,double strength){
    public static final Codec<StoredPower> CODEC=RecordCodecBuilder.create(i->i.group(
        Art.CODEC.fieldOf("art").forGetter(StoredPower::art),Metal.CODEC.fieldOf("metal").forGetter(StoredPower::metal),
        Codec.doubleRange(.000000001,1000).fieldOf("strength").forGetter(StoredPower::strength)
    ).apply(i,StoredPower::new));
    public StoredPower{if(art==null||metal==null||!Double.isFinite(strength)||strength<=0||strength>1000)throw new IllegalArgumentException("Invalid stored ability");}
    public String label(){return art.name().toLowerCase(java.util.Locale.ROOT)+" "+metal.id();}
    public dev.metallicarts.arts.EffectivePower.Key key(){return new dev.metallicarts.arts.EffectivePower.Key(art,metal);}
}
