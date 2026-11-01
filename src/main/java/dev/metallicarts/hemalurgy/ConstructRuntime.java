package dev.metallicarts.hemalurgy;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.api.ArtsProtection;
import dev.metallicarts.arts.*;
import dev.metallicarts.allomancy.*;
import dev.metallicarts.config.AdvancedConfig;
import dev.metallicarts.domain.*;
import dev.metallicarts.equipment.AssetLease;
import dev.metallicarts.feruchemy.*;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import java.util.*;

/** Distinct body dependencies and behavior. Exact anatomy/preparation costs are labelled game interpretations. */
public final class ConstructRuntime {
 public record Analysis(List<SpikeAsset> spikes,boolean koloss,boolean inquisitor,int blessings,int blessingMembers){}
 private record Compulsion(ServerPlayer caster,LivingEntity target,int until){}
 private static final Map<UUID,Integer> CLOCK=new HashMap<>();
 private static final Map<UUID,Compulsion> CONTROL=new HashMap<>();
 public static Analysis inspect(ServerPlayer p){
  var spikes=new ArrayList<SpikeAsset>();var bySlot=new HashMap<Integer,SpikeAsset>();var prep=ConstructLedger.get(p.level().getServer());var pairs=new HashMap<UUID,List<SpikeAsset>>();int members=0;
  for(int i=0;i<12;i++){var data=HemalurgyRuntime.equipment(p).item(i).get(ModRegistries.SPIKE);if(data==null)continue;try{var a=SpikeLedger.get(p.level().getServer()).owned(data.token(),new AssetLease(p.getUUID(),i));if(a.drained()||!accepts(p,i,a))continue;spikes.add(a);bySlot.put(i,a);var entry=prep.find(a.token().instance());if(entry.isPresent()&&entry.get().purpose()==ConstructLedger.Purpose.KANDRA_BLESSING){members++;pairs.computeIfAbsent(entry.get().pair().orElseThrow(),u->new ArrayList<>()).add(a);}}catch(IllegalArgumentException ignored){}}
  boolean koloss=true;for(int slot:new int[]{0,1,4,5}){var a=bySlot.get(slot);if(a==null||prep.find(a.token().instance()).filter(v->v.purpose()==ConstructLedger.Purpose.KOLOSS_STAPLE).isEmpty())koloss=false;}
  boolean eyes=true;for(int slot:new int[]{2,3}){var a=bySlot.get(slot);if(a==null||prep.find(a.token().instance()).filter(v->v.purpose()==ConstructLedger.Purpose.INQUISITOR_EYE).isEmpty())eyes=false;}
  var pin=bySlot.get(11);boolean inquisitor=eyes&&pin!=null&&prep.find(pin.token().instance()).filter(v->v.purpose()==ConstructLedger.Purpose.INQUISITOR_PIN).isPresent()&&spikes.size()>=11&&spikes.stream().anyMatch(a->a.power().kind()==StolenPower.Kind.FERUCHEMY&&a.power().metal()==Metal.GOLD);
  int blessings=(int)pairs.values().stream().filter(v->v.size()==2&&v.get(0).power().kind()==v.get(1).power().kind()&&v.get(0).metal()==v.get(1).metal()).count();
  return new Analysis(List.copyOf(spikes),koloss,inquisitor,blessings,members);
 }
 public static boolean accepts(ServerPlayer p,int point,SpikeAsset a){
  var prep=ConstructLedger.get(p.level().getServer()).find(a.token().instance());
  if(prep.isPresent()&&prep.get().purpose()==ConstructLedger.Purpose.INQUISITOR_EYE)return (point==2||point==3)&&a.metal()==Metal.STEEL;
  if(prep.isPresent()&&prep.get().purpose()==ConstructLedger.Purpose.KANDRA_BLESSING)return p.getAttachedOrCreate(ModRegistries.CONSTRUCT).lineage()==ConstructState.Lineage.MISTWRAITH&&Set.of(0,1,4,5).contains(point);
  return BindingPoints.ALL.get(point).accepts(a.metal());
 }
 public static void transform(ServerPlayer p,ConstructState.Kind kind){
  if(!AdvancedConfig.get().constructs().enabled()||!ArtsProtection.allows(p,p,ArtsProtection.Action.CONSTRUCT))throw new IllegalArgumentException("Construct transformations disabled or protected");
  var state=p.getAttachedOrCreate(ModRegistries.CONSTRUCT);if(state.kind()!=ConstructState.Kind.HUMAN||kind==ConstructState.Kind.HUMAN)throw new IllegalArgumentException("Only an untransformed body can enter a template; operator recovery is separate");
  var a=inspect(p);boolean valid=switch(kind){case KOLOSS->state.lineage()==ConstructState.Lineage.HUMAN&&a.koloss();case INQUISITOR->state.lineage()==ConstructState.Lineage.HUMAN&&a.inquisitor();case KANDRA->state.lineage()==ConstructState.Lineage.MISTWRAITH&&a.blessings()>0;default->false;};
  if(!valid)throw new IllegalArgumentException("Missing lineage, prepared spikes, Gold access or anatomical dependencies");
  p.setAttached(ModRegistries.CONSTRUCT,new ConstructState(state.lineage(),kind,ConstructState.Shape.HUMAN,0,0,1));tick(p,p.level().getServer().getTickCount());
 }
 public static void prepareHeld(ServerPlayer p,ConstructLedger.Purpose purpose){
  if(!AdvancedConfig.get().constructs().enabled()||!ArtsProtection.allows(p,p,ArtsProtection.Action.CONSTRUCT))throw new IllegalArgumentException("Preparation disabled/protected");
  var held=p.getMainHandItem();var data=held.get(ModRegistries.SPIKE);if(held.getCount()!=1||data==null)throw new IllegalArgumentException("Hold one charged free spike");
  var a=SpikeLedger.get(p.level().getServer()).resolve(data.token());var preparation=ConstructLedger.get(p.level().getServer());
  // Cost preflight and canonical validation happen before inventory changes. Preparation creates no charge.
  if(count(p,Items.BONE)<AdvancedConfig.get().constructs().preparationBones())throw new IllegalArgumentException("Preparation consumes two ordinary bones");
  if(purpose==ConstructLedger.Purpose.KANDRA_BLESSING){var other=p.getOffhandItem();var b=other.get(ModRegistries.SPIKE);if(other.getCount()!=1||b==null)throw new IllegalArgumentException("Hold the second charged compatible spike in offhand");preparation.bless(a,SpikeLedger.get(p.level().getServer()).resolve(b.token()));}else preparation.prepare(a,purpose);
  consume(p,Items.BONE,AdvancedConfig.get().constructs().preparationBones());
 }
 public static void shape(ServerPlayer p,ConstructState.Shape shape){
  var state=p.getAttachedOrCreate(ModRegistries.CONSTRUCT);if(state.kind()!=ConstructState.Kind.KANDRA||inspect(p).blessings()==0||!canUsePowers(p))throw new IllegalArgumentException("A stable kandra Blessing is required");
  if(!ArtsProtection.allows(p,p,ArtsProtection.Action.CONSTRUCT))throw new IllegalArgumentException("Shaping protected");int mask=1<<shape.ordinal();
  if((state.knownShapes()&mask)==0){int bones=shape==ConstructState.Shape.WOLF?AdvancedConfig.get().constructs().wolfBones():AdvancedConfig.get().constructs().slimeBones();var extra=shape==ConstructState.Shape.WOLF?Items.LEATHER:Items.SLIME_BALL;if(count(p,Items.BONE)<bones||count(p,extra)<AdvancedConfig.get().constructs().shapeMaterials())throw new IllegalArgumentException("New body template needs "+bones+" bones and two "+new ItemStack(extra).getHoverName().getString());consume(p,Items.BONE,bones);consume(p,extra,AdvancedConfig.get().constructs().shapeMaterials());}
  p.setAttached(ModRegistries.CONSTRUCT,new ConstructState(state.lineage(),state.kind(),shape,state.growthTicks(),0,state.knownShapes()|mask));tick(p,p.level().getServer().getTickCount());
 }
 private static int count(ServerPlayer p,Item item){int count=0;for(int i=0;i<36;i++)if(p.getInventory().getItem(i).is(item))count+=p.getInventory().getItem(i).getCount();return count;}
 private static void consume(ServerPlayer p,Item item,int n){for(int i=0;i<36&&n>0;i++){var stack=p.getInventory().getItem(i);if(stack.is(item)){int amount=Math.min(n,stack.getCount());stack.shrink(amount);n-=amount;}}}
 public static boolean canUsePowers(ServerPlayer p){var s=p.getAttachedOrCreate(ModRegistries.CONSTRUCT);return !AdvancedConfig.get().constructs().enabled()||!(s.lineage()==ConstructState.Lineage.MISTWRAITH&&s.kind()==ConstructState.Kind.HUMAN)&&(s.kind()!=ConstructState.Kind.KANDRA||s.degradedTicks()<AdvancedConfig.get().constructs().kandraDegradeTicks()&&inspect(p).blessingMembers()>0);}
 public static void tick(ServerPlayer p,int tick){
  int actual=p.level().getServer().getTickCount();boolean advances=!Objects.equals(CLOCK.put(p.getUUID(),actual),actual);
  var s=p.getAttachedOrCreate(ModRegistries.CONSTRUCT);if(!AdvancedConfig.get().constructs().enabled()||!p.isAlive()){clearModifiers(p);return;}
  if(s.kind()==ConstructState.Kind.HUMAN){clearModifiers(p);if(s.lineage()==ConstructState.Lineage.MISTWRAITH){modifier(p,Attributes.SCALE,"construct_body",-.7,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);modifier(p,Attributes.MOVEMENT_SPEED,"construct_stride",-.07,AttributeModifier.Operation.ADD_VALUE);}return;}
  var a=inspect(p);boolean active=switch(s.kind()){case KOLOSS->a.koloss();case INQUISITOR->a.inquisitor();case KANDRA->a.blessings()>0;default->false;};
  int degraded=active?0:Math.min(24000,s.degradedTicks()+(advances?1:0));if(s.kind()==ConstructState.Kind.KANDRA&&a.blessingMembers()==0)degraded=Math.max(degraded,AdvancedConfig.get().constructs().kandraDegradeTicks());
  long age=Math.min(1000000000000L,s.growthTicks()+(advances?1:0));var next=new ConstructState(s.lineage(),s.kind(),s.shape(),age,degraded,s.knownShapes());if(!next.equals(s))p.setAttached(ModRegistries.CONSTRUCT,next);
  double scale=1,health=0,speed=0,attack=0;
  if(s.kind()==ConstructState.Kind.KOLOSS){scale=active?1.2+(AdvancedConfig.get().constructs().kolossMaximumScale()-1.2)*Math.min(1,age/(double)AdvancedConfig.get().constructs().kolossGrowthTicks()):1.1;health=active?20:0;attack=active?4:0;speed=active?.01:-.04;}
  if(s.kind()==ConstructState.Kind.INQUISITOR){health=active?10:0;speed=active?0:-.04;if(tick%20==0){p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS,40,0,false,false,false));metalSight(p);}}
  if(s.kind()==ConstructState.Kind.KANDRA){boolean thinking=degraded<AdvancedConfig.get().constructs().kandraDegradeTicks();scale=!thinking?.3:s.shape()==ConstructState.Shape.WOLF?.65:s.shape()==ConstructState.Shape.SLIME?.35:1;speed=!thinking?-.07:s.shape()==ConstructState.Shape.WOLF?.035:s.shape()==ConstructState.Shape.SLIME?-.03:0;if(active)p.setAirSupply(p.getMaxAirSupply());}
  modifier(p,Attributes.SCALE,"construct_body",scale-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);modifier(p,Attributes.MAX_HEALTH,"construct_health",health,AttributeModifier.Operation.ADD_VALUE);modifier(p,Attributes.MOVEMENT_SPEED,"construct_stride",speed,AttributeModifier.Operation.ADD_VALUE);modifier(p,Attributes.ATTACK_DAMAGE,"construct_force",attack,AttributeModifier.Operation.ADD_VALUE);if(p.getHealth()>p.getMaxHealth())p.setHealth(p.getMaxHealth());
  if(!active&&tick%40==0&&p.getHealth()>2&&s.kind()==ConstructState.Kind.KOLOSS)p.hurtServer(p.level(),p.damageSources().magic(),.5f);
 }
 private static void metalSight(ServerPlayer p){int checked=0,shown=0;var tag=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,MetallicArts.id("metal_anchors"));for(int x=-8;x<=8;x+=2)for(int y=-4;y<=4;y+=2)for(int z=-8;z<=8;z+=2){if(checked++>=512||shown>=32)return;var pos=p.blockPosition().offset(x,y,z);if(p.level().hasChunkAt(pos)&&p.level().getBlockState(pos).is(tag)){shown++;p.level().sendParticles(p,new net.minecraft.core.particles.DustParticleOptions(0x55CCFF,1),true,false,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,2,0,0,0,0);}}}
 public static void compel(ServerPlayer caster){
  var target=AdvancedAllomancy.aimed(caster,12).orElseThrow(()->new IllegalArgumentException("Aim at a spiked living target"));if(!(target instanceof ServerPlayer other))throw new IllegalArgumentException("Only supported player construct bodies can currently be compelled");
  var a=inspect(other);var state=other.getAttachedOrCreate(ModRegistries.CONSTRUCT);double power=Math.max(AllomancyRuntime.power(caster,Metal.ZINC),AllomancyRuntime.power(caster,Metal.BRASS));var emotions=other.getAttachedOrCreate(ModRegistries.EMOTIONS);
  double resistance=state.kind()==ConstructState.Kind.KANDRA?3:1;resistance+=HemalurgyRuntime.passive(other,StolenPower.Kind.EMOTIONAL_FORTITUDE)+Math.max(0,AdvancedFeruchemy.effect(other,FeruchemicalAttribute.DETERMINATION));
  if(a.spikes().size()<2||power<=0||power*a.spikes().size()<resistance*2||Math.max(emotions.value(EmotionProfile.Emotion.TRUST),emotions.value(EmotionProfile.Emotion.FEAR))<.8||AllomancyRuntime.power(other,Metal.COPPER)>0||!ArtsProtection.allows(caster,target,ArtsProtection.Action.CONTROL))throw new IllegalArgumentException("Need sufficient paid emotional power, susceptible spikes and existing extreme fear/trust");
  if(CONTROL.size()>=256&&!CONTROL.containsKey(target.getUUID()))throw new IllegalArgumentException("Compulsion limit reached");CONTROL.put(target.getUUID(),new Compulsion(caster,target,caster.level().getServer().getTickCount()+AdvancedConfig.get().constructs().controlTicks()));
 }
 public static void controlFrame(int tick){CONTROL.values().removeIf(c->{if(tick>=c.until()||c.caster().isRemoved()||c.target().isRemoved()||!c.target().isAlive()||c.target().level()!=c.caster().level()||c.caster().distanceToSqr(c.target())>144||!ArtsProtection.allows(c.caster(),c.target(),ArtsProtection.Action.CONTROL)||Math.max(AllomancyRuntime.power(c.caster(),Metal.ZINC),AllomancyRuntime.power(c.caster(),Metal.BRASS))<=0)return true;var destination=c.caster().position().add(c.caster().getLookAngle().multiply(1,0,1).normalize().scale(4));var motion=destination.subtract(c.target().position()).multiply(1,0,1);if(motion.length()>.2)c.target().setDeltaMovement(c.target().getDeltaMovement().add(motion.normalize().scale(.06)));c.target().hurtMarked=true;return false;});}
 public static void clear(){CONTROL.clear();CLOCK.clear();}
 public static void stop(ServerPlayer p){CLOCK.remove(p.getUUID());CONTROL.remove(p.getUUID());CONTROL.values().removeIf(c->c.caster()==p);clearModifiers(p);}
 public static String status(ServerPlayer p){var s=p.getAttachedOrCreate(ModRegistries.CONSTRUCT);var a=inspect(p);return s.kind()+" / "+s.shape()+" • "+a.spikes().size()+" valid spikes • "+a.blessings()+" Blessings • degradation "+s.degradedTicks();}
 public static void lineage(ServerPlayer p,ConstructState.Lineage lineage){if(p.getAttachedOrCreate(ModRegistries.CONSTRUCT).kind()!=ConstructState.Kind.HUMAN)throw new IllegalArgumentException("Reset body before changing base lineage");p.setAttached(ModRegistries.CONSTRUCT,new ConstructState(lineage,ConstructState.Kind.HUMAN,ConstructState.Shape.HUMAN,0,0,1));}
 public static void reset(ServerPlayer p){p.setAttached(ModRegistries.CONSTRUCT,ConstructState.HUMAN);clearModifiers(p);CONTROL.remove(p.getUUID());}
 private static void modifier(ServerPlayer p,net.minecraft.core.Holder<Attribute> attr,String name,double amount,AttributeModifier.Operation op){var value=p.getAttribute(attr);if(value==null)return;var id=MetallicArts.id(name);if(amount==0)value.removeModifier(id);else if(value.getModifier(id)==null||value.getModifier(id).amount()!=amount)value.addOrUpdateTransientModifier(new AttributeModifier(id,amount,op));}
 private static void clearModifiers(ServerPlayer p){modifier(p,Attributes.SCALE,"construct_body",0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);modifier(p,Attributes.MAX_HEALTH,"construct_health",0,AttributeModifier.Operation.ADD_VALUE);modifier(p,Attributes.MOVEMENT_SPEED,"construct_stride",0,AttributeModifier.Operation.ADD_VALUE);modifier(p,Attributes.ATTACK_DAMAGE,"construct_force",0,AttributeModifier.Operation.ADD_VALUE);}
 private ConstructRuntime(){}
}
