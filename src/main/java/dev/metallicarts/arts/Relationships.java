package dev.metallicarts.arts;
import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import java.util.*;
/** Familiarity from actual completed trades with a specific NPC, independent of its temporary emotion profile. */
public record Relationships(Map<UUID,Integer> merchants){
 public static final Codec<Relationships> CODEC=Codec.unboundedMap(UUIDUtil.STRING_CODEC,Codec.intRange(0,100)).xmap(Relationships::new,Relationships::merchants);
 public Relationships{merchants=Map.copyOf(merchants);if(merchants.size()>256)throw new IllegalArgumentException("Too many social records");}
 public static Relationships empty(){return new Relationships(Map.of());}
 public Relationships trade(UUID merchant){var next=new TreeMap<>(merchants);if(!next.containsKey(merchant)&&next.size()>=256)next.pollFirstEntry();next.put(merchant,Math.min(100,next.getOrDefault(merchant,0)+2));return new Relationships(next);}
 public double familiarity(UUID merchant){return merchants.getOrDefault(merchant,0)/100.0;}
}
