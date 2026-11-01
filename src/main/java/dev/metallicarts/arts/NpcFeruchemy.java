package dev.metallicarts.arts;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.EconomyContent;
import dev.metallicarts.equipment.*;
import dev.metallicarts.feruchemy.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** NPCs wear actual world-owned minds, surrender attributes while resting, and pay to tap in combat.
 * No preview balance, renewable spawn grant or free emergency charge is used. */
public final class NpcFeruchemy {
 public static final Set<Metal> SUPPORTED=Collections.unmodifiableSet(EnumSet.allOf(Metal.class));
 private static final class Session {final Mob mob;final FeruchemicalEngine engine=new FeruchemicalEngine();Map<FeruchemicalAttribute,Double> effects=Map.of();Optional<StoredPower> storedPower=Optional.empty();Session(Mob mob){this.mob=mob;}}
 private static final Map<UUID,Session> SESSIONS=new HashMap<>();
 public static double effect(LivingEntity entity,FeruchemicalAttribute attribute){var s=SESSIONS.get(entity.getUUID());return s!=null&&s.mob==entity?s.effects.getOrDefault(attribute,0.0):0;}
 public static double access(Mob mob,Spiritweb state,Art art,Metal metal){double base=state.strength(art,metal);var s=SESSIONS.get(mob.getUUID());if(s==null||s.mob!=mob)return base;var power=s.storedPower;if(power.isEmpty()||power.get().art()!=art||power.get().metal()!=metal)return base;double effect=s.effects.getOrDefault(FeruchemicalAttribute.INVESTITURE,0.0);return Math.clamp(effect<0?base*(1+effect):base+power.get().strength()*effect,0,1000);}
 public static void initialize(){
  net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_UNLOAD.register((entity,level)->{if(entity instanceof Mob mob&&entity.getRemovalReason()==Entity.RemovalReason.DISCARDED){MetalmindLedger.get(level.getServer()).discardOwned(mob.getUUID());SESSIONS.remove(mob.getUUID());}});
  ServerLivingEntityEvents.AFTER_DEATH.register((entity,damage)->{if(entity instanceof Mob mob)release(mob);});
  ServerLifecycleEvents.SERVER_STOPPING.register(server->{SESSIONS.values().forEach(s->clearModifiers(s.mob));SESSIONS.clear();});
 }
 public static boolean provision(Mob mob){
  var gear=mob.getAttachedOrCreate(ModRegistries.CONTACT_EQUIPMENT);if(!gear.item(0).isEmpty())return true;
  var state=mob.getAttachedOrCreate(ModRegistries.SPIRITWEB);var grant=state.grants().stream().filter(g->g.art()==Art.FERUCHEMY&&SUPPORTED.contains(g.metal())).findFirst();if(grant.isEmpty())return false;
  var metal=grant.get().metal();var attr=metal==Metal.TIN?FeruchemicalAttribute.SIGHT:Arrays.stream(FeruchemicalAttribute.values()).filter(a->a.metal()==metal).findFirst().orElseThrow();
  var ledger=MetalmindLedger.get(mob.level().getServer());var lease=new AssetLease(mob.getUUID(),0);if(!ledger.room()||ledger.occupied(lease))return false;
  var content=MetalmindContent.EMPTY;if(attr==FeruchemicalAttribute.INVESTITURE){var source=state.grants().stream().filter(g->g.art()==Art.ALLOMANCY).findFirst();if(source.isEmpty())return false;var g=source.get();content=new MetalmindContent(Optional.of(new StoredPower(g.art(),g.metal(),g.strength())),List.of(),false);}
  var a=ledger.claim(ledger.create(attr,9_000_000,1,false,content).token(),lease);var ring=new ItemStack(EconomyContent.item(metal.id(),"ring"));ring.set(ModRegistries.METALMIND,MetalmindData.of(a));mob.setAttached(ModRegistries.CONTACT_EQUIPMENT,gear.with(0,ring));return true;
 }
 public static void tick(Mob mob){
  if(!mob.isAlive()||mob.isRemoved())return;
  if(SESSIONS.size()>=dev.metallicarts.config.WorldConfig.get().maximumNpcSessions()&&!SESSIONS.containsKey(mob.getUUID()))return;
  if(!provision(mob))return;
  var s=SESSIONS.get(mob.getUUID());if(s==null||s.mob!=mob){s=new Session(mob);SESSIONS.put(mob.getUUID(),s);}
  var ledger=MetalmindLedger.get(mob.level().getServer());var gear=mob.getAttachedOrCreate(ModRegistries.CONTACT_EQUIPMENT);var ref=gear.item(0).get(ModRegistries.METALMIND);if(ref==null)return;
  MetalmindAsset asset;try{asset=ledger.owned(ref.token(),new AssetLease(mob.getUUID(),0));}catch(IllegalArgumentException e){s.engine.stopAll();s.effects=Map.of();clearModifiers(mob);return;}
  boolean fighting=mob.getTarget()!=null||mob.getLastHurtByMob()!=null&&mob.tickCount-mob.getLastHurtByMobTimestamp()<100;
  s.storedPower=asset.content().power();
  if(asset.attribute()==FeruchemicalAttribute.MEMORY){
   if(mob.tickCount%100==0){var lease=new AssetLease(mob.getUUID(),0);var identity=mob.getAttachedOrCreate(ModRegistries.SPIRITWEB).identity();
    if(!fighting&&asset.content().memories().size()<256&&asset.content().memories().stream().noneMatch(m->m.x()>>4==mob.getBlockX()>>4&&m.z()>>4==mob.getBlockZ()>>4)){
     var pos=mob.blockPosition();var memory=new MemoryEntry(UUID.randomUUID(),"npc_route","Observed patrol refuge",mob.level().dimension().identifier().toString(),pos.getX(),pos.getY(),pos.getZ(),mob.level().getGameTime());asset=ledger.storeMemory(asset.token(),lease,identity,memory,false);
    }else if(fighting&&mob.getHealth()<mob.getMaxHealth()*.5&&!asset.content().memories().isEmpty()){
     var take=ledger.takeMemory(asset.token(),lease,identity,asset.content().memories().getFirst().id());asset=take.asset();var memory=take.memory();mob.setAttached(ModRegistries.JOURNAL,mob.getAttachedOrCreate(ModRegistries.JOURNAL).remember(memory));if(memory.dimension().equals(mob.level().dimension().identifier().toString()))mob.getNavigation().moveTo(memory.x()+.5,memory.y(),memory.z()+.5,1.1);
    }
    var ring=gear.item(0);ring.set(ModRegistries.METALMIND,MetalmindData.of(asset));mob.setAttached(ModRegistries.CONTACT_EQUIPMENT,gear.with(0,ring));
   }return;
  }
  var mode=fighting?FeruchemicalEngine.Mode.TAP:FeruchemicalEngine.Mode.STORE;
  if(asset.attribute()==FeruchemicalAttribute.HEALTH&&fighting&&mob.getHealth()>=mob.getMaxHealth())mode=FeruchemicalEngine.Mode.OFF;
  var tokens=ContactService.tokens(gear);var state=mob.getAttachedOrCreate(ModRegistries.SPIRITWEB);
  var context=new FeruchemicalEngine.Context(){
   public int storeLimit(MetalmindAsset a,int units){return switch(a.attribute()){
    case HEALTH->Math.min(units,Math.max(0,(int)((mob.getHealth()-6)*1000)));
    case NUTRITION->(int)Math.min(units,mob.getAttachedOrCreate(ModRegistries.BODY).nutrition());
    case BREATH->Math.min(units,Math.max(0,(mob.getAirSupply()-40)*1000));
    case WARMTH->(int)Math.min(units,Math.max(0,mob.getAttachedOrCreate(ModRegistries.BODY).temperature()-BodyState.MIN_TEMP));
    case WAKEFULNESS->(int)Math.min(units,BodyState.MAX_FATIGUE-mob.getAttachedOrCreate(ModRegistries.BODY).fatigue());default->units;};}
   public int payStore(MetalmindAsset a,int units){switch(a.attribute()){
    case HEALTH->mob.setHealth(mob.getHealth()-units/1000f);
    case NUTRITION->{var b=mob.getAttachedOrCreate(ModRegistries.BODY);mob.setAttached(ModRegistries.BODY,b.nutrition(b.nutrition()-units));}
    case BREATH->mob.setAirSupply(mob.getAirSupply()-(units+999)/1000);
    case WARMTH->{var b=mob.getAttachedOrCreate(ModRegistries.BODY);mob.setAttached(ModRegistries.BODY,b.temperature(b.temperature()-units));}
    case WAKEFULNESS->{var b=mob.getAttachedOrCreate(ModRegistries.BODY);mob.setAttached(ModRegistries.BODY,b.fatigue(b.fatigue()+units));}
    default->{}
   }return units;}
   public int tapLimit(MetalmindAsset a,int units){return switch(a.attribute()){
    case HEALTH->Math.min(units,Math.max(0,(int)((mob.getMaxHealth()-mob.getHealth())*1000)));
    case NUTRITION->(int)Math.min(units,BodyState.MAX_NUTRITION-mob.getAttachedOrCreate(ModRegistries.BODY).nutrition());
    case BREATH->Math.min(units,Math.max(0,(mob.getMaxAirSupply()-mob.getAirSupply())*1000));
    case WARMTH->(int)Math.min(units,BodyState.MAX_TEMP-mob.getAttachedOrCreate(ModRegistries.BODY).temperature());
    case WAKEFULNESS->(int)Math.min(units,mob.getAttachedOrCreate(ModRegistries.BODY).fatigue());default->units;};}
   public void paid(MetalmindAsset a,int slot,int units){if(units<=0)return;switch(a.attribute()){
    case HEALTH->mob.heal(units/1000f);
    case NUTRITION->{var b=mob.getAttachedOrCreate(ModRegistries.BODY);mob.setAttached(ModRegistries.BODY,b.nutrition(b.nutrition()+units));}
    case BREATH->mob.setAirSupply(Math.min(mob.getMaxAirSupply(),mob.getAirSupply()+units/1000));
    case WARMTH->{var b=mob.getAttachedOrCreate(ModRegistries.BODY);mob.setAttached(ModRegistries.BODY,b.temperature(b.temperature()+units));}
    case WAKEFULNESS->{var b=mob.getAttachedOrCreate(ModRegistries.BODY);mob.setAttached(ModRegistries.BODY,b.fatigue(b.fatigue()-units));}
    default->{}
   }}
  };
  s.engine.request(0,mode,fighting?2:1,tokens,ledger,mob.getUUID(),state,context);s.effects=s.engine.step(tokens,ledger,mob.getUUID(),state,context);
  var item=gear.item(0);item.set(ModRegistries.METALMIND,MetalmindData.of(ledger.owned(ref.token(),new AssetLease(mob.getUUID(),0))));mob.setAttached(ModRegistries.CONTACT_EQUIPMENT,gear.with(0,item));
  modifier(mob,Attributes.MOVEMENT_SPEED,"f_speed",.08*s.effects.getOrDefault(FeruchemicalAttribute.SPEED,0.0));
  modifier(mob,Attributes.ATTACK_DAMAGE,"f_strength",4*s.effects.getOrDefault(FeruchemicalAttribute.STRENGTH,0.0));
  modifier(mob,Attributes.FOLLOW_RANGE,"f_senses",8*(s.effects.getOrDefault(FeruchemicalAttribute.SIGHT,0.0)+s.effects.getOrDefault(FeruchemicalAttribute.MENTAL_SPEED,0.0)));
  modifier(mob,Attributes.KNOCKBACK_RESISTANCE,"f_weight",Math.clamp(s.effects.getOrDefault(FeruchemicalAttribute.WEIGHT,0.0)*.25+s.effects.getOrDefault(FeruchemicalAttribute.DETERMINATION,0.0)*.2,-.5,.8));
  modifier(mob,Attributes.LUCK,"f_fortune",s.effects.getOrDefault(FeruchemicalAttribute.FORTUNE,0.0));
  var body=mob.getAttachedOrCreate(ModRegistries.BODY);modifier(mob,Attributes.MOVEMENT_SPEED,"f_body",(body.nutrition()==0?-.03:0)+(body.fatigue()>BodyState.MAX_FATIGUE*.8?-.03:0)+Math.min(0,s.effects.getOrDefault(FeruchemicalAttribute.WAKEFULNESS,0.0))*.05);if(fighting&&body.nutrition()>0)mob.setAttached(ModRegistries.BODY,body.nutrition(Math.max(0,body.nutrition()-1000)));if(body.temperature()<-200000)mob.setTicksFrozen(Math.min(mob.getTicksRequiredToFreeze()+1,mob.getTicksFrozen()+2));if(!fighting&&mob.tickCount%100==0&&mob.getHealth()<mob.getMaxHealth()&&body.nutrition()>=400000){mob.heal(1);mob.setAttached(ModRegistries.BODY,body.nutrition(body.nutrition()-400000));}
 }
 public static void frame(MinecraftServer server){SESSIONS.values().removeIf(s->{if(s.mob.isRemoved()||!s.mob.isAlive()){clearModifiers(s.mob);return true;}return false;});for(var level:server.getAllLevels()){int checked=0;for(var entity:level.getAllEntities())if(entity instanceof Mob mob){if(++checked>dev.metallicarts.config.WorldConfig.get().scanBudget())break;if(mob.getAttachedOrCreate(ModRegistries.SPIRITWEB).grants().stream().anyMatch(g->g.art()==Art.FERUCHEMY&&SUPPORTED.contains(g.metal())))tick(mob);}}}
 public static void release(Mob mob){var s=SESSIONS.remove(mob.getUUID());if(s!=null)clearModifiers(mob);var gear=mob.getAttachedOrCreate(ModRegistries.CONTACT_EQUIPMENT);var ledger=MetalmindLedger.get(mob.level().getServer());for(int i=0;i<12;i++){var item=gear.item(i);var ref=item.get(ModRegistries.METALMIND);if(ref==null)continue;try{var a=ledger.release(ref.token(),new AssetLease(mob.getUUID(),i));item.set(ModRegistries.METALMIND,MetalmindData.of(a));mob.spawnAtLocation((net.minecraft.server.level.ServerLevel)mob.level(),item);}catch(IllegalArgumentException ignored){}}mob.setAttached(ModRegistries.CONTACT_EQUIPMENT,ContactEquipment.empty());}
 private static void modifier(Mob mob,net.minecraft.core.Holder<Attribute> attribute,String name,double amount){var a=mob.getAttribute(attribute);if(a==null)return;var id=MetallicArts.id(name);if(amount==0)a.removeModifier(id);else a.addOrUpdateTransientModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_VALUE));}
 private static void clearModifiers(Mob mob){modifier(mob,Attributes.MOVEMENT_SPEED,"f_speed",0);modifier(mob,Attributes.ATTACK_DAMAGE,"f_strength",0);modifier(mob,Attributes.FOLLOW_RANGE,"f_senses",0);modifier(mob,Attributes.KNOCKBACK_RESISTANCE,"f_weight",0);modifier(mob,Attributes.LUCK,"f_fortune",0);modifier(mob,Attributes.MOVEMENT_SPEED,"f_body",0);}
 private NpcFeruchemy(){}
}
