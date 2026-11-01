package dev.metallicarts.feruchemy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.equipment.AssetToken;
import dev.metallicarts.domain.DomainCodecs;

/** Client tooltip preview. Charge is always resolved from the world's ledger when used. */
public record MetalmindData(int schema,AssetToken token,FeruchemicalAttribute attribute,long charge,long capacity,boolean keyed,boolean unkeyed,String contentLabel,boolean unsealed,int memories) {
    public static final Codec<MetalmindData> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.intRange(1,1).fieldOf("schema").forGetter(MetalmindData::schema),
        AssetToken.CODEC.fieldOf("token").forGetter(MetalmindData::token),
        FeruchemicalAttribute.CODEC.fieldOf("attribute").forGetter(MetalmindData::attribute),
        DomainCodecs.longRange(0,MetalmindAsset.MAX_CAPACITY).fieldOf("charge").forGetter(MetalmindData::charge),
        DomainCodecs.longRange(1,MetalmindAsset.MAX_CAPACITY).fieldOf("capacity").forGetter(MetalmindData::capacity),
        Codec.BOOL.fieldOf("keyed").forGetter(MetalmindData::keyed),
        Codec.BOOL.optionalFieldOf("unkeyed",false).forGetter(MetalmindData::unkeyed),
        Codec.string(0,96).optionalFieldOf("content_label","").forGetter(MetalmindData::contentLabel),
        Codec.BOOL.optionalFieldOf("unsealed",false).forGetter(MetalmindData::unsealed),Codec.intRange(0,256).optionalFieldOf("memories",0).forGetter(MetalmindData::memories)
    ).apply(i,MetalmindData::new));
    public MetalmindData{
        java.util.Objects.requireNonNull(token);java.util.Objects.requireNonNull(attribute);
        if(schema!=1||charge<0||capacity<1||capacity>MetalmindAsset.MAX_CAPACITY||charge>capacity)throw new IllegalArgumentException("Invalid preview");
    }
    public static MetalmindData of(MetalmindAsset asset){
        return new MetalmindData(1,asset.token(),asset.attribute(),asset.charge(),asset.capacity(),asset.identity().isPresent(),asset.unkeyed(),asset.content().power().map(StoredPower::label).orElse(""),asset.content().unsealed(),asset.content().memories().size());
    }
}
