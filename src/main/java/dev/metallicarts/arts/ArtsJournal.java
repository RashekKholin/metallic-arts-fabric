package dev.metallicarts.arts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

/** Bounded persistent mod knowledge and actual route history. Copper removes accessible entries. */
public record ArtsJournal(List<MemoryEntry> memories,List<MemoryEntry> history,List<String> discoveries){
    public static final Codec<ArtsJournal> CODEC=RecordCodecBuilder.create(i->i.group(
        MemoryEntry.CODEC.listOf(0,256).fieldOf("memories").forGetter(ArtsJournal::memories),
        MemoryEntry.CODEC.listOf(0,128).fieldOf("history").forGetter(ArtsJournal::history),
        Codec.string(1,192).listOf(0,512).fieldOf("discoveries").forGetter(ArtsJournal::discoveries)
    ).apply(i,ArtsJournal::new));
    public ArtsJournal{memories=List.copyOf(memories);history=List.copyOf(history);discoveries=List.copyOf(discoveries);}
    public static ArtsJournal empty(){return new ArtsJournal(List.of(),List.of(),List.of());}
    public ArtsJournal observe(ServerPlayer p,String kind,String label,String key){
        if(discoveries.contains(key)||discoveries.size()>=512)return this;
        var pos=p.blockPosition();var entry=new MemoryEntry(UUID.randomUUID(),kind,label,p.level().dimension().identifier().toString(),pos.getX(),pos.getY(),pos.getZ(),p.level().getGameTime());
        var m=new ArrayList<>(memories);if(m.size()<256)m.add(entry);var h=new ArrayList<>(history);h.add(entry);if(h.size()>128)h.removeFirst();
        var d=new ArrayList<>(discoveries);d.add(key);return new ArtsJournal(m,h,d);
    }
    public ArtsJournal forget(UUID id){return new ArtsJournal(memories.stream().filter(e->!e.id().equals(id)).toList(),history.stream().filter(e->!e.id().equals(id)).toList(),discoveries);}
    public ArtsJournal remember(MemoryEntry entry){
        if(memories.size()>=256||memories.stream().anyMatch(m->m.id().equals(entry.id())))throw new IllegalArgumentException("Memory collection full or entry already accessible");
        var m=new ArrayList<>(memories);m.add(entry);var h=new ArrayList<>(history);if(h.stream().noneMatch(e->e.id().equals(entry.id()))){h.add(entry);if(h.size()>128)h.removeFirst();}return new ArtsJournal(m,h,discoveries);
    }
}
