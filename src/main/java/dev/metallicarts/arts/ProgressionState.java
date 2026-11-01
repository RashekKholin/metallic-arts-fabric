package dev.metallicarts.arts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import dev.metallicarts.domain.Metal;
/** Once-only latent allocation persists through death. Awakened flags prevent extraction/relogin farming. */
public record ProgressionState(boolean initialized,Optional<Metal> allomancy,Optional<Metal> feruchemy,boolean snapped,boolean discovered){
 public static final ProgressionState EMPTY=new ProgressionState(false,Optional.empty(),Optional.empty(),false,false);
 public static final Codec<ProgressionState> CODEC=RecordCodecBuilder.create(i->i.group(Codec.BOOL.fieldOf("initialized").forGetter(ProgressionState::initialized),Metal.CODEC.optionalFieldOf("allomancy").forGetter(ProgressionState::allomancy),Metal.CODEC.optionalFieldOf("feruchemy").forGetter(ProgressionState::feruchemy),Codec.BOOL.fieldOf("snapped").forGetter(ProgressionState::snapped),Codec.BOOL.fieldOf("discovered").forGetter(ProgressionState::discovered)).apply(i,ProgressionState::new));
}
