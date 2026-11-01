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
    private final Map<UUID,UUID> memoryOwners=new HashMap<>();
    private final Map<AssetLease,UUID> leases=new HashMap<>();
    public MetalmindLedger(){}
    private MetalmindLedger(List<MetalmindAsset> entries){
        for(var asset:entries)if(assets.putIfAbsent(asset.token().instance(),asset)!=null)
            throw new IllegalArgumentException("Duplicate canonical metalmind");
        for(var asset:entries)for(var memory:asset.content().memories())if(memoryOwners.putIfAbsent(memory.id(),asset.token().instance())!=null)throw new IllegalArgumentException("Duplicate canonical memory");
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
        return create(attribute,mass,purity,unkeyed,MetalmindContent.EMPTY);
    }
    public MetalmindAsset create(FeruchemicalAttribute attribute,long mass,double purity,boolean unkeyed,MetalmindContent content){
        if(assets.size()>=LIMIT)throw new IllegalArgumentException("World metalmind limit reached");
        var token=new AssetToken(UUID.randomUUID(),UUID.randomUUID(),0);
        var asset=new MetalmindAsset(token,attribute.metal(),mass,purity,attribute,0,
            Math.max(1,(long)Math.floor(mass*purity)),Optional.empty(),unkeyed,Optional.empty(),content);
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
            next.charge(),next.capacity(),Optional.empty(),true,Optional.empty(),next.content()));
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
    /** Runtime filling: an empty pool may be keyed anew; charged foreign pools cannot be erased. */
    public MetalmindAsset change(AssetToken token,AssetLease expected,UUID identity,long amount,boolean blank){
        var asset=owned(token,expected);
        if(amount>0&&asset.charge()==0){
            boolean unkeyed=blank||asset.content().unsealed();
            asset=new MetalmindAsset(asset.token(),asset.metal(),asset.massMicrograms(),asset.purity(),asset.attribute(),0,asset.capacity(),Optional.empty(),unkeyed,asset.lease(),asset.content());
        }
        if(amount>0&&asset.unkeyed()&&!blank)throw new IllegalArgumentException("Suppress Identity before adding unkeyed charge");
        if(!asset.accessible(identity))throw new IllegalArgumentException("Foreign Identity");
        return replace(asset.withCharge(Math.addExact(asset.charge(),amount),identity));
    }
    public MetalmindAsset unseal(AssetToken token){
        var a=resolve(token);
        if(a.lease().isPresent()||!a.unkeyed()||a.attribute()!=FeruchemicalAttribute.INVESTITURE||a.content().power().isEmpty()||a.charge()==0)
            throw new IllegalArgumentException("Need a free charged unkeyed typed nicrosilmind");
        var next=a.transfer(Optional.empty());return replace(new MetalmindAsset(next.token(),a.metal(),a.massMicrograms(),a.purity(),a.attribute(),a.charge(),a.capacity(),Optional.empty(),true,Optional.empty(),new MetalmindContent(a.content().power(),List.of(),true)));
    }
    public MetalmindAsset storeMemory(AssetToken token,AssetLease lease,UUID key,dev.metallicarts.arts.MemoryEntry memory,boolean blank){
        var a=owned(token,lease);
        if(a.attribute()!=FeruchemicalAttribute.MEMORY||a.charge()+1000>a.capacity()||a.content().memories().size()>=256)throw new IllegalArgumentException("Memory pool full");
        if(memoryOwners.containsKey(memory.id()))throw new IllegalArgumentException("Memory already stored");
        if(a.charge()>0&&!a.accessible(key))throw new IllegalArgumentException("Foreign Identity");
        boolean unkeyed=a.charge()==0?blank:a.unkeyed();if(unkeyed&&!blank)throw new IllegalArgumentException("Suppress Identity before adding unkeyed memories");
        var entries=new ArrayList<>(a.content().memories());entries.add(memory);
        return replace(new MetalmindAsset(a.token(),a.metal(),a.massMicrograms(),a.purity(),a.attribute(),a.charge()+1000,a.capacity(),unkeyed?Optional.empty():Optional.of(key),unkeyed,a.lease(),new MetalmindContent(Optional.empty(),entries,false)));
    }
    public record MemoryTake(MetalmindAsset asset,dev.metallicarts.arts.MemoryEntry memory){}
    public MemoryTake takeMemory(AssetToken token,AssetLease lease,UUID key,UUID memoryId){
        var a=owned(token,lease);if(a.attribute()!=FeruchemicalAttribute.MEMORY||!a.accessible(key))throw new IllegalArgumentException("No authorized memory access");
        var memory=a.content().memories().stream().filter(m->m.id().equals(memoryId)).findFirst().orElseThrow(()->new IllegalArgumentException("Memory no longer stored here"));
        var next=replace(new MetalmindAsset(a.token(),a.metal(),a.massMicrograms(),a.purity(),a.attribute(),a.charge()-1000,a.capacity(),a.identity(),a.unkeyed(),a.lease(),new MetalmindContent(Optional.empty(),a.content().memories().stream().filter(m->!m.id().equals(memoryId)).toList(),false)));
        return new MemoryTake(next,memory);
    }
    private MetalmindAsset replace(MetalmindAsset asset){
        var old=assets.get(asset.token().instance());
        for(var memory:asset.content().memories()){var owner=memoryOwners.get(memory.id());if(owner!=null&&!owner.equals(asset.token().instance()))throw new IllegalArgumentException("Duplicate canonical memory");}
        if(old!=null){old.lease().ifPresent(leases::remove);old.content().memories().forEach(memory->memoryOwners.remove(memory.id()));}
        asset.content().memories().forEach(memory->memoryOwners.put(memory.id(),asset.token().instance()));
        asset.lease().ifPresent(lease->leases.put(lease,asset.token().instance()));
        assets.put(asset.token().instance(),asset);setDirty();return asset;
    }
}
