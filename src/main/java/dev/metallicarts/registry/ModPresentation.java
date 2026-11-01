package dev.metallicarts.registry;
import dev.metallicarts.MetallicArts;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.sounds.SoundEvent;
/** Dedicated presentation IDs let local preferences affect this mod without muting vanilla effects. */
public final class ModPresentation {
 public record Dust(int color,float scale) implements ParticleOptions {
  public static final MapCodec<Dust> CODEC=RecordCodecBuilder.mapCodec(i->i.group(Codec.INT.fieldOf("color").forGetter(Dust::color),Codec.floatRange(.01f,4).fieldOf("scale").forGetter(Dust::scale)).apply(i,Dust::new));
  public static final StreamCodec<RegistryFriendlyByteBuf,Dust> STREAM=StreamCodec.composite(ByteBufCodecs.INT,Dust::color,ByteBufCodecs.FLOAT,Dust::scale,Dust::new);
  @Override public ParticleType<Dust> getType(){return ART_DUST;}
 }
 public static final ParticleType<Dust> ART_DUST=Registry.register(BuiltInRegistries.PARTICLE_TYPE,MetallicArts.id("art_dust"),new ParticleType<Dust>(false){public MapCodec<Dust> codec(){return Dust.CODEC;}public StreamCodec<? super RegistryFriendlyByteBuf,Dust> streamCodec(){return Dust.STREAM;}});
 public static final SoundEvent BRONZE_PULSE=Registry.register(BuiltInRegistries.SOUND_EVENT,MetallicArts.id("bronze_pulse"),SoundEvent.createVariableRangeEvent(MetallicArts.id("bronze_pulse")));
 public static void initialize(){}
 private ModPresentation(){}
}
