package dev.metallicarts.arts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
/** Reputation from actual trade, gifts and violence; never an authentication or claim-permission credential. */
public record FactionStanding(Map<String,Integer> values,long lastTribute){
 public static final Set<String> FACTIONS=Set.of("terris","empire","village","independent");
 public static final Codec<FactionStanding> CODEC=RecordCodecBuilder.create(i->i.group(Codec.unboundedMap(Codec.STRING,Codec.intRange(-100,100)).fieldOf("values").forGetter(FactionStanding::values),dev.metallicarts.domain.DomainCodecs.longRange(0,Long.MAX_VALUE).fieldOf("last_tribute").forGetter(FactionStanding::lastTribute)).apply(i,FactionStanding::new));
 public FactionStanding{values=Map.copyOf(values);if(lastTribute<0||values.size()>4||values.values().stream().anyMatch(v->v< -100||v>100)||values.keySet().stream().anyMatch(k->!FACTIONS.contains(k)))throw new IllegalArgumentException("Invalid faction standing");}
 public static FactionStanding empty(){return new FactionStanding(Map.of(),0);}
 public int get(String faction){return values.getOrDefault(faction,0);}
 public FactionStanding change(String faction,int amount){if(!FACTIONS.contains(faction))throw new IllegalArgumentException("Unknown faction");var next=new HashMap<>(values);next.put(faction,Math.clamp(get(faction)+amount,-100,100));return new FactionStanding(next,lastTribute);}
 public FactionStanding tribute(long tick){return new FactionStanding(values,tick);}
}
