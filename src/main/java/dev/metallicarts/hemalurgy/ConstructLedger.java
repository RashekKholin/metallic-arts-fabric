package dev.metallicarts.hemalurgy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.domain.*;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.*;
import java.util.*;
/** Preparation is canonical provenance, not a forgeable item tooltip. Blessing pairs remain distinct from staples. */
public final class ConstructLedger extends SavedData {
 public enum Purpose{KOLOSS_STAPLE,KANDRA_BLESSING,INQUISITOR_EYE,INQUISITOR_PIN}
 public record Preparation(UUID spike,Purpose purpose,Optional<UUID> pair){
  public static final Codec<Preparation> CODEC=RecordCodecBuilder.create(i->i.group(UUIDUtil.CODEC.fieldOf("spike").forGetter(Preparation::spike),DomainCodecs.enumeration(Purpose.values(),Enum::name).fieldOf("purpose").forGetter(Preparation::purpose),UUIDUtil.CODEC.optionalFieldOf("pair").forGetter(Preparation::pair)).apply(i,Preparation::new));
  public Preparation{Objects.requireNonNull(spike);Objects.requireNonNull(purpose);Objects.requireNonNull(pair);if((purpose==Purpose.KANDRA_BLESSING)!=pair.isPresent())throw new IllegalArgumentException("Invalid Blessing pair");}
 }
 public static final Codec<ConstructLedger> CODEC=Preparation.CODEC.listOf(0,100000).xmap(ConstructLedger::new,ConstructLedger::all);
 public static final SavedDataType<ConstructLedger> TYPE=new SavedDataType<>(MetallicArts.id("construct_preparations"),ConstructLedger::new,CODEC,null);
 private final Map<UUID,Preparation> entries=new HashMap<>();public ConstructLedger(){}
 private ConstructLedger(List<Preparation> list){for(var v:list)if(entries.putIfAbsent(v.spike(),v)!=null)throw new IllegalArgumentException("Duplicate spike preparation");var pairs=new HashMap<UUID,Integer>();list.forEach(v->v.pair().ifPresent(id->pairs.merge(id,1,Integer::sum)));if(pairs.values().stream().anyMatch(n->n!=2))throw new IllegalArgumentException("Incomplete or duplicated Blessing pair");}
 public static ConstructLedger get(MinecraftServer server){return server.overworld().getDataStorage().computeIfAbsent(TYPE);}
 public List<Preparation> all(){return entries.values().stream().sorted(Comparator.comparing(Preparation::spike)).toList();}
 public Optional<Preparation> find(UUID spike){return Optional.ofNullable(entries.get(spike));}
 public void prepare(SpikeAsset source,Purpose purpose){
  free(source);if(purpose==Purpose.KANDRA_BLESSING)throw new IllegalArgumentException("Blessings require a linked pair");
  if(purpose==Purpose.KOLOSS_STAPLE&&(source.metal()!=Metal.IRON||source.power().kind()!=StolenPower.Kind.STRENGTH)||purpose==Purpose.INQUISITOR_EYE&&(source.metal()!=Metal.STEEL||source.power().kind()!=StolenPower.Kind.ALLOMANCY)||purpose==Purpose.INQUISITOR_PIN&&source.power().art().isEmpty())throw new IllegalArgumentException("Wrong charged material/capability for this preparation");
  entries.put(source.token().instance(),new Preparation(source.token().instance(),purpose,Optional.empty()));setDirty();
 }
 public void bless(SpikeAsset a,SpikeAsset b){
  free(a);free(b);if(a.token().instance().equals(b.token().instance())||a.donor().equals(b.donor())||a.power().kind()!=b.power().kind()||a.metal()!=b.metal()||!Set.of(StolenPower.Kind.STRENGTH,StolenPower.Kind.SENSES,StolenPower.Kind.COGNITION,StolenPower.Kind.EMOTIONAL_FORTITUDE).contains(a.power().kind()))throw new IllegalArgumentException("Need two distinct compatible innate donors, not ordinary art spikes");
  var pair=UUID.randomUUID();entries.put(a.token().instance(),new Preparation(a.token().instance(),Purpose.KANDRA_BLESSING,Optional.of(pair)));entries.put(b.token().instance(),new Preparation(b.token().instance(),Purpose.KANDRA_BLESSING,Optional.of(pair)));setDirty();
 }
 private void free(SpikeAsset a){if(a.drained()||a.lease().isPresent()||entries.containsKey(a.token().instance())||entries.size()>=99998)throw new IllegalArgumentException("Use free, unprepared, live canonical spikes");}
}
