package dev.metallicarts.arts;
import dev.metallicarts.allomancy.*;
import dev.metallicarts.config.AdvancedConfig;
import dev.metallicarts.domain.*;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.phys.Vec3;
import java.util.*;
/** Paid NPC burners shared by natural survival profiles and explicit operator provisioning. */
public final class NpcArts {
 public static final Set<Metal> SUPPORTED=Set.of(Metal.IRON,Metal.STEEL,Metal.PEWTER,Metal.TIN,Metal.COPPER,Metal.ALUMINUM,Metal.DURALUMIN);
 private static final class Session{final Mob mob;final BurnEngine engine=new BurnEngine(SUPPORTED);Map<Metal,Double> paid=Map.of();boolean burst;Session(Mob mob){this.mob=mob;}}
 private static final Map<UUID,Session> SESSIONS=new HashMap<>();
 public static boolean request(Mob mob,Metal metal,BurnMode mode){if(!SUPPORTED.contains(metal)||SESSIONS.size()>=256&&!SESSIONS.containsKey(mob.getUUID()))return false;var session=SESSIONS.get(mob.getUUID());if(session==null||session.mob!=mob){session=new Session(mob);SESSIONS.put(mob.getUUID(),session);}return session.engine.request(mob.getAttachedOrCreate(ModRegistries.SPIRITWEB),metal,mode);}
 public static boolean active(Mob mob){var s=SESSIONS.get(mob.getUUID());return s!=null&&s.mob==mob&&!mob.isRemoved();}
 public static List<Mob> actors(){return SESSIONS.values().stream().map(s->s.mob).toList();}
 public static Map<Metal,Double> powers(Mob mob){var s=SESSIONS.get(mob.getUUID());return s!=null&&s.mob==mob?s.paid:Map.of();}
 public static void leech(Mob mob){var s=SESSIONS.get(mob.getUUID());if(s==null)return;mob.setAttached(ModRegistries.SPIRITWEB,AdvancedAllomancy.drain(mob.getAttachedOrCreate(ModRegistries.SPIRITWEB),EnumSet.allOf(Metal.class)));s.engine.stopAll();s.paid=Map.of();s.burst=false;}
 public static void boost(Mob mob){var s=SESSIONS.get(mob.getUUID());if(s!=null)s.burst=true;}
 public static void prepare(MinecraftServer server){for(var iterator=SESSIONS.values().iterator();iterator.hasNext();){var s=iterator.next();if(!s.mob.isAlive()||s.mob.isRemoved()||s.mob.level().getServer()!=server){clearModifiers(s.mob);iterator.remove();continue;}
  var step=s.engine.step(s.mob.getAttachedOrCreate(ModRegistries.SPIRITWEB),AllomancyRuntime.config);s.mob.setAttached(ModRegistries.SPIRITWEB,step.state());var paid=new EnumMap<Metal,Double>(Metal.class);paid.putAll(step.strengths());
  if(paid.containsKey(Metal.ALUMINUM)){leech(s.mob);continue;}
  if(paid.containsKey(Metal.DURALUMIN)||s.burst){s.burst=false;var used=EnumSet.noneOf(Metal.class);for(var metal:paid.keySet())if(metal!=Metal.DURALUMIN){long mass=ReserveLedger.ordinary(s.mob.getAttachedOrCreate(ModRegistries.SPIRITWEB),metal);double factor=Math.min(AdvancedConfig.get().enhancement().maximumBoost(),1+Math.sqrt(mass/(double)AdvancedConfig.get().enhancement().massScale()));paid.put(metal,Math.min(AdvancedConfig.get().enhancement().maximumPower(),paid.get(metal)*factor));used.add(metal);s.engine.exhaust(metal);}if(paid.containsKey(Metal.DURALUMIN)){used.add(Metal.DURALUMIN);s.engine.exhaust(Metal.DURALUMIN);}s.mob.setAttached(ModRegistries.SPIRITWEB,AdvancedAllomancy.drain(s.mob.getAttachedOrCreate(ModRegistries.SPIRITWEB),used));}
  s.paid=Map.copyOf(paid);
 }}
 public static void apply(){for(var s:SESSIONS.values()){
  modifier(s.mob,Attributes.ATTACK_DAMAGE,"npc_pewter",3*s.paid.getOrDefault(Metal.PEWTER,0.0));modifier(s.mob,Attributes.FOLLOW_RANGE,"npc_tin",8*s.paid.getOrDefault(Metal.TIN,0.0));
  var target=s.mob.getTarget();double force=s.paid.getOrDefault(Metal.STEEL,0.0)-s.paid.getOrDefault(Metal.IRON,0.0);if(target==null||force==0||s.mob.distanceToSqr(target)>144||!s.mob.hasLineOfSight(target)||!dev.metallicarts.api.ArtsProtection.npcAllows(s.mob,target,dev.metallicarts.api.ArtsProtection.Action.AMPLIFY))continue;
  boolean metal=false;for(var slot:EquipmentSlot.values())if(MetalPhysics.external(target.getItemBySlot(slot))){metal=true;break;}if(!metal)continue;
  Vec3 direction=target.getBoundingBox().getCenter().subtract(s.mob.getBoundingBox().getCenter()).normalize();double impulse=Math.min(.5,Math.abs(force)*.07)*(force>0?1:-1);double targetMass=target instanceof net.minecraft.server.level.ServerPlayer p?dev.metallicarts.feruchemy.FeruchemyRuntime.mass(p):80;
  MetalPhysics.externalMotion(target,direction.scale(impulse*80/Math.max(1,targetMass)),target instanceof net.minecraft.server.level.ServerPlayer?AllomancyRuntime.config.maximumPlayerSpeed():AllomancyRuntime.config.maximumObjectSpeed());MetalPhysics.externalMotion(s.mob,direction.scale(-impulse),AllomancyRuntime.config.maximumObjectSpeed());
 }}
 public static double cloudAt(LivingEntity target){double strength=0;for(var s:SESSIONS.values()){double power=s.paid.getOrDefault(Metal.COPPER,0.0);if(power>0&&s.mob.level()==target.level()&&s.mob.distanceToSqr(target)<=Math.pow(Math.min(18,6+3*power),2))strength=Math.max(strength,power);}return strength;}
 private static void modifier(Mob mob,net.minecraft.core.Holder<Attribute> attribute,String name,double amount){var a=mob.getAttribute(attribute);if(a==null)return;var id=dev.metallicarts.MetallicArts.id(name);if(amount==0)a.removeModifier(id);else if(a.getModifier(id)==null||a.getModifier(id).amount()!=amount)a.addOrUpdateTransientModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_VALUE));}
 private static void clearModifiers(Mob mob){modifier(mob,Attributes.ATTACK_DAMAGE,"npc_pewter",0);modifier(mob,Attributes.FOLLOW_RANGE,"npc_tin",0);}
 public static void clear(){SESSIONS.values().forEach(s->clearModifiers(s.mob));SESSIONS.clear();}
 private NpcArts(){}
}

