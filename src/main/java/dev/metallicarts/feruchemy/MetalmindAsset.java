package dev.metallicarts.feruchemy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.domain.*;
import dev.metallicarts.equipment.*;
import net.minecraft.core.UUIDUtil;
import java.util.*;

/** Canonical world-owned charge. Item components merely reference this record. */
public record MetalmindAsset(AssetToken token,Metal metal,long massMicrograms,double purity,
    FeruchemicalAttribute attribute,long charge,long capacity,Optional<UUID> identity,boolean unkeyed,Optional<AssetLease> lease) {
    public static final long MAX_CAPACITY=1_000_000_000_000L;
    public static final Codec<MetalmindAsset> CODEC=RecordCodecBuilder.create(i->i.group(
        AssetToken.CODEC.fieldOf("token").forGetter(MetalmindAsset::token),
        Metal.CODEC.fieldOf("metal").forGetter(MetalmindAsset::metal),
        DomainCodecs.longRange(1,MAX_CAPACITY).fieldOf("mass_micrograms").forGetter(MetalmindAsset::massMicrograms),
        Codec.doubleRange(.000001,1).fieldOf("purity").forGetter(MetalmindAsset::purity),
        FeruchemicalAttribute.CODEC.fieldOf("attribute").forGetter(MetalmindAsset::attribute),
        DomainCodecs.longRange(0,MAX_CAPACITY).fieldOf("charge").forGetter(MetalmindAsset::charge),
        DomainCodecs.longRange(1,MAX_CAPACITY).fieldOf("capacity").forGetter(MetalmindAsset::capacity),
        UUIDUtil.CODEC.optionalFieldOf("identity").forGetter(MetalmindAsset::identity),
        Codec.BOOL.fieldOf("unkeyed").forGetter(MetalmindAsset::unkeyed),
        AssetLease.CODEC.optionalFieldOf("lease").forGetter(MetalmindAsset::lease)
    ).apply(i,MetalmindAsset::new));
    public MetalmindAsset{
        Objects.requireNonNull(token);Objects.requireNonNull(metal);Objects.requireNonNull(attribute);
        Objects.requireNonNull(identity);Objects.requireNonNull(lease);
        if(attribute.metal()!=metal||massMicrograms<1||massMicrograms>MAX_CAPACITY||
            !Double.isFinite(purity)||purity<.000001||purity>1||capacity<1||
            capacity>Math.max(1,(long)Math.floor(massMicrograms*purity))||charge<0||charge>capacity||
            unkeyed&&identity.isPresent()||charge>0&&!unkeyed&&identity.isEmpty())
            throw new IllegalArgumentException("Invalid typed metalmind");
    }
    public boolean accessible(UUID userIdentity){return unkeyed||identity.isEmpty()||identity.get().equals(userIdentity);}
    public MetalmindAsset withCharge(long next,UUID filler){
        if(!accessible(filler))throw new IllegalArgumentException("Foreign Identity");
        return new MetalmindAsset(token,metal,massMicrograms,purity,attribute,next,capacity,
            unkeyed?Optional.empty():identity.isPresent()?identity:Optional.of(filler),unkeyed,lease);
    }
    public MetalmindAsset transfer(Optional<AssetLease> nextLease){
        var next=new AssetToken(token.instance(),token.seal(),Math.incrementExact(token.revision()));
        return new MetalmindAsset(next,metal,massMicrograms,purity,attribute,charge,capacity,identity,unkeyed,nextLease);
    }
}
