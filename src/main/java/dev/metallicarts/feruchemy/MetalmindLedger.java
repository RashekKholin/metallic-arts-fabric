package dev.metallicarts.feruchemy;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.equipment.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.*;
import java.util.*;

/** One persistent server-world authority across dimensions. Never imports charge from an item token. */
public final class MetalmindLedger extends SavedData {
    private static final int LIMIT=100000;
    public static final Codec<MetalmindLedger> CODEC=RecordCodecBuilder.<MetalmindLedger>create(i->i.group(
        Codec.intRange(1,1).fieldOf("schema").forGetter(x->1),
        MetalmindAsset.CODEC.listOf(0,LIMIT).fieldOf("assets").forGetter(MetalmindLedger::all)
    ).apply(i,(schema,assets)->new MetalmindLedger(assets)));
    public static final SavedDataType<MetalmindLedger> TYPE=new SavedDataType<>(MetallicArts.id("metalminds"),
        MetalmindLedger::new,CODEC,null);
    private final Map<UUID,MetalmindAsset> assets=new HashMap<>();
    private final Map<AssetLease,UUID> leases=new HashMap<>();
    public MetalmindLedger(){}
    private MetalmindLedger(List<MetalmindAsset> entries){
        for(var asset:entries)if(assets.putIfAbsent(asset.token().instance(),asset)!=null)
            throw new IllegalArgumentException("Duplicate canonical metalmind");
        for(var asset:entries)asset.lease().ifPresent(lease->{
            if(leases.putIfAbsent(lease,asset.token().instance())!=null)throw new IllegalArgumentException("Duplicate leased contact slot");
        });
    }
    public static MetalmindLedger get(MinecraftServer server){return server.overworld().getDataStorage().computeIfAbsent(TYPE);}
    public List<MetalmindAsset> all(){return assets.values().stream().sorted(Comparator.comparing(a->a.token().instance())).toList();}
    public MetalmindAsset resolve(AssetToken token){
        var asset=assets.get(token.instance());
        if(asset==null||!asset.token().equals(token))throw new IllegalArgumentException("Unknown, copied or stale metalmind reference");
        return asset;
    }
    public MetalmindAsset create(FeruchemicalAttribute attribute,long mass,double purity,boolean unkeyed){
        if(assets.size()>=LIMIT)throw new IllegalArgumentException("World metalmind limit reached");
        var token=new AssetToken(UUID.randomUUID(),UUID.randomUUID(),0);
        var asset=new MetalmindAsset(token,attribute.metal(),mass,purity,attribute,0,
            Math.max(1,(long)Math.floor(mass*purity)),Optional.empty(),unkeyed,Optional.empty());
        assets.put(token.instance(),asset);setDirty();return asset;
    }
    public MetalmindAsset claim(AssetToken token,AssetLease destination){
        var asset=resolve(token);
        if(asset.lease().isPresent()||occupied(destination))
            throw new IllegalArgumentException("Metalmind or slot already equipped");
        return replace(asset.transfer(Optional.of(destination)));
    }
    public boolean occupied(AssetLease destination){return leases.containsKey(destination);}
    /** Administrative setup only; normal Identity-free filling is a later aluminum mechanic. */
    public MetalmindAsset unkey(AssetToken token){
        var asset=resolve(token);
        if(asset.lease().isPresent())throw new IllegalArgumentException("Unequip the metalmind before rekeying");
        var next=asset.transfer(Optional.empty());
        return replace(new MetalmindAsset(next.token(),next.metal(),next.massMicrograms(),next.purity(),next.attribute(),
            next.charge(),next.capacity(),Optional.empty(),true,Optional.empty()));
    }
    public MetalmindAsset release(AssetToken token,AssetLease expected){
        var asset=owned(token,expected);
        return replace(asset.transfer(Optional.empty()));
    }
    public MetalmindAsset owned(AssetToken token,AssetLease expected){
        var asset=resolve(token);
        if(!asset.lease().filter(expected::equals).isPresent())throw new IllegalArgumentException("No matching physical contact");
        return asset;
    }
    /** Runtime-only transaction. Positive amount stores surrendered attributes; negative taps already-owned charge. */
    public MetalmindAsset change(AssetToken token,AssetLease expected,UUID identity,long amount){
        var asset=owned(token,expected);
        if(!asset.accessible(identity))throw new IllegalArgumentException("Foreign Identity");
        return replace(asset.withCharge(Math.addExact(asset.charge(),amount),identity));
    }
    private MetalmindAsset replace(MetalmindAsset asset){
        var old=assets.get(asset.token().instance());
        if(old!=null)old.lease().ifPresent(leases::remove);
        asset.lease().ifPresent(lease->leases.put(lease,asset.token().instance()));
        assets.put(asset.token().instance(),asset);setDirty();return asset;
    }
}
