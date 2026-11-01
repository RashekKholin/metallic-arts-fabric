package dev.metallicarts.arts;

import com.mojang.serialization.Codec;
import java.util.*;

/** Familiarity earned by actual time in regions. Temporary Connection does not rewrite this history. */
public record ConnectionState(Map<String,Integer> regions){
    public static final Codec<ConnectionState> CODEC=Codec.unboundedMap(Codec.string(1,192),Codec.intRange(0,24000)).xmap(ConnectionState::new,ConnectionState::regions);
    public ConnectionState{regions=Map.copyOf(regions);if(regions.size()>128)throw new IllegalArgumentException("Too many remembered regions");}
    public static ConnectionState empty(){return new ConnectionState(Map.of());}
    public ConnectionState visit(String key){var next=new TreeMap<>(regions);if(!next.containsKey(key)&&next.size()>=128)next.pollFirstEntry();next.put(key,Math.min(24000,next.getOrDefault(key,0)+20));return new ConnectionState(next);}
    public double familiarity(String key){return Math.min(1,regions.getOrDefault(key,0)/6000.0);}
}
