package dev.metallicarts.hemalurgy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.domain.*;
import dev.metallicarts.equipment.*;
import net.minecraft.core.UUIDUtil;
import java.util.*;

/** Canonical charge, donor provenance and anatomical ownership; never reconstructed from a tooltip. */
public record SpikeAsset(AssetToken token,Metal metal,StolenPower power,UUID donor,UUID donorIdentity,UUID donorSource,
                         double originalStrength,double extractedStrength,long exposure,long lastTime,
                         boolean blood,Optional<AssetLease> lease,boolean drained) {
    public static final long MAX_EXPOSURE=1_000_000_000_000_000L;
    public static final Codec<SpikeAsset> CODEC=RecordCodecBuilder.create(i->i.group(
        AssetToken.CODEC.fieldOf("token").forGetter(SpikeAsset::token),Metal.CODEC.fieldOf("metal").forGetter(SpikeAsset::metal),
        StolenPower.CODEC.fieldOf("power").forGetter(SpikeAsset::power),UUIDUtil.CODEC.fieldOf("donor").forGetter(SpikeAsset::donor),
        UUIDUtil.CODEC.fieldOf("donor_identity").forGetter(SpikeAsset::donorIdentity),UUIDUtil.CODEC.fieldOf("donor_source").forGetter(SpikeAsset::donorSource),
        Codec.doubleRange(.000001,1000).fieldOf("original_strength").forGetter(SpikeAsset::originalStrength),
        Codec.doubleRange(.0000001,1000).fieldOf("extracted_strength").forGetter(SpikeAsset::extractedStrength),
        DomainCodecs.longRange(0,MAX_EXPOSURE).fieldOf("exposure").forGetter(SpikeAsset::exposure),
        DomainCodecs.longRange(0,Long.MAX_VALUE).fieldOf("last_time").forGetter(SpikeAsset::lastTime),
        Codec.BOOL.fieldOf("blood").forGetter(SpikeAsset::blood),AssetLease.CODEC.optionalFieldOf("lease").forGetter(SpikeAsset::lease),
        Codec.BOOL.fieldOf("drained").forGetter(SpikeAsset::drained)
    ).apply(i,SpikeAsset::new));
    public SpikeAsset{
        Objects.requireNonNull(token);Objects.requireNonNull(metal);Objects.requireNonNull(power);Objects.requireNonNull(donor);
        Objects.requireNonNull(donorIdentity);Objects.requireNonNull(donorSource);Objects.requireNonNull(lease);
        if(!power.compatible(metal)||!Double.isFinite(originalStrength)||originalStrength<.000001||originalStrength>1000||
            !Double.isFinite(extractedStrength)||extractedStrength<.0000001||extractedStrength>originalStrength||exposure<0||exposure>MAX_EXPOSURE||lastTime<0)
            throw new IllegalArgumentException("Invalid canonical spike");
    }
    public double potency(){return drained?0:extractedStrength*(.1+.9/(1+Math.log1p(exposure/480000.0)));}
    public SpikeAsset advance(long now){
        long elapsed=Math.max(0,now-lastTime),rate=lease.isPresent()?0:blood?1:20;
        long room=MAX_EXPOSURE-exposure;
        long next=exposure+(rate==0?0:elapsed>room/rate?room:elapsed*rate);
        return new SpikeAsset(token,metal,power,donor,donorIdentity,donorSource,originalStrength,extractedStrength,next,Math.max(lastTime,now),blood,lease,drained);
    }
    public SpikeAsset transfer(Optional<AssetLease> destination){return new SpikeAsset(new AssetToken(token.instance(),token.seal(),Math.addExact(token.revision(),1)),
        metal,power,donor,donorIdentity,donorSource,originalStrength,extractedStrength,exposure,lastTime,blood,destination,drained);}
    public SpikeAsset preserved(boolean value){return new SpikeAsset(token,metal,power,donor,donorIdentity,donorSource,originalStrength,extractedStrength,exposure,lastTime,value,lease,drained);}
    public SpikeAsset drain(){return new SpikeAsset(token,metal,power,donor,donorIdentity,donorSource,originalStrength,extractedStrength,exposure,lastTime,blood,lease,true);}
}
