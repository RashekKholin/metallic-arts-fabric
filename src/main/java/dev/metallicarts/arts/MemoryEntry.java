package dev.metallicarts.arts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.domain.DomainCodecs;
import net.minecraft.core.UUIDUtil;
import java.util.UUID;

/** Actual observed game event. No invented biography or claim to predict arbitrary futures. */
public record MemoryEntry(UUID id,String kind,String label,String dimension,int x,int y,int z,long time){
    public static final Codec<MemoryEntry> CODEC=RecordCodecBuilder.create(i->i.group(
        UUIDUtil.CODEC.fieldOf("id").forGetter(MemoryEntry::id),Codec.string(1,32).fieldOf("kind").forGetter(MemoryEntry::kind),
        Codec.string(1,128).fieldOf("label").forGetter(MemoryEntry::label),Codec.string(1,128).fieldOf("dimension").forGetter(MemoryEntry::dimension),
        Codec.intRange(-30000000,30000000).fieldOf("x").forGetter(MemoryEntry::x),Codec.intRange(-2048,2048).fieldOf("y").forGetter(MemoryEntry::y),
        Codec.intRange(-30000000,30000000).fieldOf("z").forGetter(MemoryEntry::z),DomainCodecs.longRange(0,Long.MAX_VALUE).fieldOf("time").forGetter(MemoryEntry::time)
    ).apply(i,MemoryEntry::new));
}
