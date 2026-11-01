package dev.metallicarts.arts;

import dev.metallicarts.domain.*;
import dev.metallicarts.hemalurgy.*;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

/** Transient paid access/suppression; original persistent grants are never overwritten by storing a power. */
public final class EffectivePower {
    public record Key(Art art,Metal metal){}
    private record Frame(Object player,Object level,java.util.function.Supplier<Map<Key,Double>> additions,java.util.function.Supplier<Map<Key,Double>> suppression){}
    private static final Map<UUID,Frame> FRAMES=new HashMap<>();
    public static double base(ServerPlayer p,Art art,Metal metal){return AccountPowers.strength(p.getUUID(),dev.metallicarts.MetallicArts.config.enableAccountPowers(),p.getAttachedOrCreate(ModRegistries.SPIRITWEB),art,metal);}
    public static double strength(ServerPlayer p,Art art,Metal metal){
        var frame=FRAMES.get(p.getUUID());var key=new Key(art,metal);double base=base(p,art,metal);
        if(p.isAlive()&&frame!=null&&frame.player()==p&&frame.level()==p.level())base=base*(1-Math.clamp(frame.suppression().get().getOrDefault(key,0.0),0,1))+frame.additions().get().getOrDefault(key,0.0);
        return Math.min(1000,base*(1+.1*HemalurgyRuntime.passive(p,StolenPower.Kind.INVESTITURE)));
    }
    public static void publish(ServerPlayer p,java.util.function.Supplier<Map<Key,Double>> additions,java.util.function.Supplier<Map<Key,Double>> suppression){FRAMES.put(p.getUUID(),new Frame(p,p.level(),additions,suppression));}
    public static void stop(ServerPlayer p){FRAMES.remove(p.getUUID());}
    public static void clear(){FRAMES.clear();}
    private EffectivePower(){}
}
