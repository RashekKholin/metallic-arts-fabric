package dev.metallicarts.feruchemy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.arts.MemoryEntry;
import java.util.*;

public record MetalmindContent(Optional<StoredPower> power,List<MemoryEntry> memories,boolean unsealed){
    public static final MetalmindContent EMPTY=new MetalmindContent(Optional.empty(),List.of(),false);
    public static final Codec<MetalmindContent> CODEC=RecordCodecBuilder.create(i->i.group(
        StoredPower.CODEC.optionalFieldOf("power").forGetter(MetalmindContent::power),MemoryEntry.CODEC.listOf(0,256).fieldOf("memories").forGetter(MetalmindContent::memories),
        Codec.BOOL.fieldOf("unsealed").forGetter(MetalmindContent::unsealed)
    ).apply(i,MetalmindContent::new));
    public MetalmindContent{Objects.requireNonNull(power);memories=List.copyOf(memories);if(memories.stream().map(MemoryEntry::id).distinct().count()!=memories.size())throw new IllegalArgumentException("Duplicate stored memory");}
}
