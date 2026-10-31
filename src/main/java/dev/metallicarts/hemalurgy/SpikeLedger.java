package dev.metallicarts.hemalurgy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.domain.*;
import dev.metallicarts.equipment.*;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.*;
import java.util.*;

/** World-bound, revisioned charge and single anatomical leases. Donor-source tombstones prevent repeated theft. */
public final class SpikeLedger extends SavedData {
    private static final int LIMIT=100000;
    public static final Codec<SpikeLedger> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.intRange(1,1).fieldOf("schema").forGetter(x->1),
        SpikeAsset.CODEC.listOf(0,LIMIT).fieldOf("assets").forGetter(SpikeLedger::all),
        UUIDUtil.CODEC.listOf(0,LIMIT).fieldOf("extractions").forGetter(x->x.extractions.stream().sorted().toList())
    ).apply(i,(schema,assets,extractions)->new SpikeLedger(assets,extractions)));
    public static final SavedDataType<SpikeLedger> TYPE=new SavedDataType<>(MetallicArts.id("spikes"),SpikeLedger::new,CODEC,null);
    private final Map<UUID,SpikeAsset> assets=new HashMap<>();
    private final Map<AssetLease,UUID> leases=new HashMap<>();
    private final Set<UUID> extractions=new HashSet<>();
    public SpikeLedger(){}
    private SpikeLedger(List<SpikeAsset> entries,List<UUID> extracted){
        extractions.addAll(extracted);
        for(var a:entries){
            if(assets.putIfAbsent(a.token().instance(),a)!=null)throw new IllegalArgumentException("Duplicate canonical spike");
            a.lease().ifPresent(l->{if(leases.putIfAbsent(l,a.token().instance())!=null)throw new IllegalArgumentException("Duplicate anatomical lease");});
        }
    }
    public static SpikeLedger get(MinecraftServer server){return server.overworld().getDataStorage().computeIfAbsent(TYPE);}
    public List<SpikeAsset> all(){return assets.values().stream().sorted(Comparator.comparing(a->a.token().instance())).toList();}
    public boolean extracted(UUID source){return extractions.contains(source);}
    public boolean known(UUID source){return assets.containsKey(source);}
    public boolean canCreate(UUID source){return assets.size()<LIMIT&&extractions.size()<LIMIT&&!extracted(source);}
    public SpikeAsset create(Metal metal,StolenPower power,UUID donor,UUID identity,UUID source,double strength,long now){
        if(!canCreate(source))throw new IllegalArgumentException("Donor source already extracted or world limit reached");
        var asset=new SpikeAsset(new AssetToken(UUID.randomUUID(),UUID.randomUUID(),0),metal,power,donor,identity,source,strength,strength*.9,0,Math.max(0,now),false,Optional.empty(),false);
        extractions.add(source);return replace(asset);
    }
    public SpikeAsset resolve(AssetToken token){
        var a=assets.get(token.instance());if(a==null||!a.token().equals(token))throw new IllegalArgumentException("Unknown, copied or stale spike reference");return a;
    }
    public SpikeAsset advance(AssetToken token,long now){return replace(resolve(token).advance(now));}
    public SpikeAsset claim(AssetToken token,AssetLease lease,long now){
        var a=resolve(token);if(a.drained()||a.lease().isPresent()||leases.containsKey(lease))throw new IllegalArgumentException("Spike or binding point unavailable");
        return replace(a.advance(now).transfer(Optional.of(lease)));
    }
    public SpikeAsset owned(AssetToken token,AssetLease lease){
        var a=resolve(token);if(!a.lease().filter(lease::equals).isPresent())throw new IllegalArgumentException("No matching anatomical ownership");return a;
    }
    public SpikeAsset release(AssetToken token,AssetLease lease,long now){return replace(owned(token,lease).advance(now).transfer(Optional.empty()));}
    public SpikeAsset preserve(AssetToken token,boolean blood,long now){
        var a=resolve(token);if(a.lease().isPresent())throw new IllegalArgumentException("Remove the spike first");
        return replace(a.advance(now).preserved(blood).transfer(Optional.empty()));
    }
    public void drainSource(UUID source){var a=assets.get(source);if(a!=null)replace(a.drain());}
    /** Roll back a private provisional extraction before any physical charged item is published. */
    void cancelUnpublished(AssetToken token){
        var a=resolve(token);if(a.lease().isPresent())throw new IllegalArgumentException("Cannot roll back an implanted spike");
        assets.remove(token.instance());extractions.remove(a.donorSource());setDirty();
    }
    private SpikeAsset replace(SpikeAsset a){
        var old=assets.put(a.token().instance(),a);if(old!=null)old.lease().ifPresent(leases::remove);
        a.lease().ifPresent(l->leases.put(l,a.token().instance()));setDirty();return a;
    }
}
