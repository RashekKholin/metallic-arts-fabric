package dev.metallicarts.arts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;

/** Actual bounded emotional tendencies; Rioting amplifies an existing feeling rather than issuing commands. */
public record EmotionProfile(Map<Emotion,Double> levels,double awareness,long updated){
    public enum Emotion { ANGER, FEAR, TRUST, DETERMINATION;
        public static final Codec<Emotion> CODEC=dev.metallicarts.domain.DomainCodecs.enumeration(values(),Enum::name);
        public double baseline(){return this==DETERMINATION?.5:.25;}
    }
    public static final Codec<EmotionProfile> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.unboundedMap(Emotion.CODEC,Codec.doubleRange(0,1)).fieldOf("levels").forGetter(EmotionProfile::levels),
        Codec.doubleRange(0,1).fieldOf("awareness").forGetter(EmotionProfile::awareness),
        dev.metallicarts.domain.DomainCodecs.longRange(0,Long.MAX_VALUE).fieldOf("updated").forGetter(EmotionProfile::updated)
    ).apply(i,EmotionProfile::new));
    public EmotionProfile{levels=Map.copyOf(levels);if(!Double.isFinite(awareness)||awareness<0||awareness>1||updated<0)throw new IllegalArgumentException("Invalid emotions");}
    public static EmotionProfile baseline(){return new EmotionProfile(Map.of(),0,0);}
    public double value(Emotion emotion){return levels.getOrDefault(emotion,emotion.baseline());}
    public EmotionProfile settle(long now){
        long elapsed=Math.max(0,now-updated);double recovery=Math.min(1,elapsed*.0002);
        var next=new EnumMap<Emotion,Double>(Emotion.class);
        for(var emotion:Emotion.values()){double v=value(emotion),base=emotion.baseline();next.put(emotion,v+Math.copySign(Math.min(Math.abs(base-v),recovery),base-v));}
        return new EmotionProfile(next,Math.max(0,awareness-recovery),Math.max(now,updated));
    }
    public EmotionProfile influence(Map<Emotion,Double> changes,long now){
        var settled=settle(now);var next=new EnumMap<Emotion,Double>(Emotion.class);next.putAll(settled.levels());double intrusion=0;
        for(var e:changes.entrySet()){double value=settled.value(e.getKey());next.put(e.getKey(),Math.clamp(value+e.getValue(),0,1));intrusion+=Math.abs(e.getValue());}
        return new EmotionProfile(next,Math.min(1,settled.awareness()+intrusion*.2),now);
    }
}
