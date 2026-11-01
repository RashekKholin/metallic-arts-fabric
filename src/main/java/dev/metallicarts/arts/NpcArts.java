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
 public static final Set<Metal> SUPPORTED=Collections.unmodifiableSet(EnumSet.allOf(Metal.class));
 private static final class Session{final Mob mob;final BurnEngine engine=new BurnEngine(SUPPORTED);Map<Metal,Double> paid=Map.of();boolean burst,selfBurst;int burstUntil;final Map<Metal,Double> burstPowers=new EnumMap<>(Metal.class);int dodge=-1000,contact=-1000;Vec3 rest;Session(Mob mob){this.mob=mob;rest=mob.position();}}
 private static final Map<UUID,Session> SESSIONS=new HashMap<>();
 public static boolean request(Mob mob,Metal metal,BurnMode mode){if(mode!=BurnMode.OFF&&((metal==Metal.CADMIUM&&powers(mob).containsKey(Metal.BENDALLOY))||(metal==Metal.BENDALLOY&&powers(mob).containsKey(Metal.CADMIUM))))return false;if(!SUPPORTED.contains(metal)||SESSIONS.size()>=dev.metallicarts.config.WorldConfig.get().maximumNpcSessions()&&!SESSIONS.containsKey(mob.getUUID()))return false;var session=SESSIONS.get(mob.getUUID());if(session==null||session.mob!=mob){session=new Session(mob);SESSIONS.put(mob.getUUID(),session);}if(mode==BurnMode.OFF){if(metal==Metal.DURALUMIN){session.burstPowers.clear();session.burstUntil=0;}else session.burstPowers.remove(metal);}var state=mob.getAttachedOrCreate(ModRegistries.SPIRITWEB);return session.engine.request(state,metal,mode,m->NpcFeruchemy.access(mob,state,Art.ALLOMANCY,m));}
 public static boolean active(Mob mob){var s=SESSIONS.get(mob.getUUID());return s!=null&&s.mob==mob&&!mob.isRemoved();}
 public static List<Mob> actors(){return SESSIONS.values().stream().map(s->s.mob).toList();}
 public static Map<Metal,Double> powers(Mob mob){var s=SESSIONS.get(mob.getUUID());return s!=null&&s.mob==mob?s.paid:Map.of();}
 public static void leech(Mob mob){var s=SESSIONS.get(mob.getUUID());if(s==null)return;mob.setAttached(ModRegistries.SPIRITWEB,AdvancedAllomancy.drain(mob.getAttachedOrCreate(ModRegistries.SPIRITWEB),EnumSet.allOf(Metal.class)));s.engine.stopAll();s.paid=Map.of();s.burst=false;s.burstUntil=0;s.burstPowers.clear();}
 public static void boost(Mob mob){var s=SESSIONS.get(mob.getUUID());if(s!=null)s.burst=true;}
 public static void prepare(MinecraftServer server){for(var iterator=SESSIONS.values().iterator();iterator.hasNext();){var s=iterator.next();if(!s.mob.isAlive()||s.mob.isRemoved()||s.mob.level().getServer()!=server){clearModifiers(s.mob);iterator.remove();continue;}
  int now=server.getTickCount();if(s.selfBurst&&s.burstUntil>0&&now>=s.burstUntil)s.engine.stop(Metal.DURALUMIN);var before=s.mob.getAttachedOrCreate(ModRegistries.SPIRITWEB);var step=s.engine.step(before,AllomancyRuntime.config,m->NpcFeruchemy.access(s.mob,before,Art.ALLOMANCY,m));s.mob.setAttached(ModRegistries.SPIRITWEB,step.state());var paid=new EnumMap<Metal,Double>(Metal.class);paid.putAll(step.strengths());
  if(paid.containsKey(Metal.ALUMINUM)){leech(s.mob);continue;}
  if(s.burstUntil>0&&now>=s.burstUntil){s.burstPowers.clear();s.burstUntil=0;s.burst=false;}
  if(s.selfBurst&&s.burstUntil>now&&!paid.containsKey(Metal.DURALUMIN)){s.burstPowers.clear();s.burstUntil=0;}
  if((s.burstUntil==0||s.selfBurst)&&(paid.containsKey(Metal.DURALUMIN)||s.burst)){s.selfBurst=paid.containsKey(Metal.DURALUMIN);s.burst=false;var used=EnumSet.noneOf(Metal.class);for(var metal:paid.keySet())if(!s.burstPowers.containsKey(metal)&&!Set.of(Metal.DURALUMIN,Metal.ALUMINUM,Metal.CHROMIUM,Metal.NICROSIL).contains(metal)){long mass=ReserveLedger.ordinary(s.mob.getAttachedOrCreate(ModRegistries.SPIRITWEB),metal);double factor=Math.min(AdvancedConfig.get().enhancement().maximumBoost(),1+Math.sqrt(mass/(double)AdvancedConfig.get().enhancement().massScale()));s.burstPowers.put(metal,Math.min(AdvancedConfig.get().enhancement().maximumPower(),paid.get(metal)*factor));used.add(metal);s.engine.exhaust(metal);}if(!used.isEmpty()||s.selfBurst){if(s.burstUntil==0)s.burstUntil=now+100;s.mob.setAttached(ModRegistries.SPIRITWEB,AdvancedAllomancy.drain(s.mob.getAttachedOrCreate(ModRegistries.SPIRITWEB),used));}}
  if(now<s.burstUntil)for(var entry:s.burstPowers.entrySet())if(NpcFeruchemy.access(s.mob,s.mob.getAttachedOrCreate(ModRegistries.SPIRITWEB),Art.ALLOMANCY,entry.getKey())>0)paid.put(entry.getKey(),entry.getValue());
  s.paid=Map.copyOf(paid);TemporalEngine.updateNpc(s.mob,s.paid);
 }}
 public static void apply(){for(var s:SESSIONS.values()){advanced(s);
  modifier(s.mob,Attributes.ATTACK_DAMAGE,"npc_pewter",3*s.paid.getOrDefault(Metal.PEWTER,0.0));modifier(s.mob,Attributes.FOLLOW_RANGE,"npc_tin",8*s.paid.getOrDefault(Metal.TIN,0.0));
  var target=s.mob.getTarget();double force=s.paid.getOrDefault(Metal.STEEL,0.0)-s.paid.getOrDefault(Metal.IRON,0.0);if(target==null||force==0||s.mob.distanceToSqr(target)>144||!s.mob.hasLineOfSight(target)||!dev.metallicarts.api.ArtsProtection.npcAllows(s.mob,target,dev.metallicarts.api.ArtsProtection.Action.AMPLIFY))continue;
  boolean metal=false;for(var slot:EquipmentSlot.values())if(MetalPhysics.external(target.getItemBySlot(slot))){metal=true;break;}if(!metal)continue;
  Vec3 direction=target.getBoundingBox().getCenter().subtract(s.mob.getBoundingBox().getCenter()).normalize();double impulse=Math.min(.5,Math.abs(force)*.07)*MetalPhysics.distanceFactor(s.mob.distanceTo(target))*(force>0?1:-1);double targetMass=target instanceof net.minecraft.server.level.ServerPlayer p?dev.metallicarts.feruchemy.FeruchemyRuntime.mass(p):80;
  MetalPhysics.externalMotion(target,direction.scale(impulse*80/Math.max(1,targetMass)),target instanceof net.minecraft.server.level.ServerPlayer?AllomancyRuntime.config.maximumPlayerSpeed():AllomancyRuntime.config.maximumObjectSpeed());MetalPhysics.externalMotion(s.mob,direction.scale(-impulse),AllomancyRuntime.config.maximumObjectSpeed());
 }}
 private static void advanced(Session s){
  var mob=s.mob;int tick=mob.level().getServer().getTickCount();var target=mob.getTarget();
  if(target==null&&mob.onGround())s.rest=mob.position();
  double bronze=s.paid.getOrDefault(Metal.BRONZE,0.0);
  if(bronze>0&&target==null&&mob instanceof net.minecraft.world.entity.monster.Monster&&tick%10==0)for(var p:mob.level().getServer().getPlayerList().getPlayers())if(p.level()==mob.level()&&!p.isCreative()&&!p.isSpectator()&&mob.distanceToSqr(p)<=Math.pow(Math.min(32,12+3*bronze),2)&&!AllomancyRuntime.powers(p).isEmpty()&&AdvancedAllomancy.concealment(p)<bronze&&dev.metallicarts.api.ArtsProtection.npcAllows(mob,p,dev.metallicarts.api.ArtsProtection.Action.EMOTION)){mob.setTarget(p);break;}
  double zinc=s.paid.getOrDefault(Metal.ZINC,0.0),brass=s.paid.getOrDefault(Metal.BRASS,0.0);
  if(target!=null&&(zinc>0||brass>0))EmotionService.influenceMob(mob,target,zinc>0?EmotionProfile.Emotion.FEAR:EmotionProfile.Emotion.DETERMINATION,(zinc-brass)*dev.metallicarts.config.AdvancedConfig.get().emotions().rate());
  if(s.paid.containsKey(Metal.GOLD)&&mob.getHealth()<mob.getMaxHealth()*.5&&tick%20==0)mob.getNavigation().moveTo(s.rest.x,s.rest.y,s.rest.z,1.1);
  if(s.paid.containsKey(Metal.ELECTRUM)&&tick-s.dodge>=dev.metallicarts.config.AdvancedConfig.get().enhancement().dodgeCooldown()){
   int checked=0;for(var projectile:mob.level().getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class,mob.getBoundingBox().inflate(10))){if(++checked>32)break;if(projectile.getOwner()==mob||mob.getBoundingBox().inflate(.3).clip(projectile.position(),projectile.position().add(projectile.getDeltaMovement().scale(5))).isEmpty())continue;
    var v=projectile.getDeltaMovement();var side=new Vec3(-v.z,0,v.x).normalize().scale(.6);if(!mob.level().noCollision(mob,mob.getBoundingBox().move(side)))side=side.scale(-1);var state=mob.getAttachedOrCreate(ModRegistries.SPIRITWEB);long cost=dev.metallicarts.config.AdvancedConfig.get().enhancement().dodgeCost();if(mob.level().noCollision(mob,mob.getBoundingBox().move(side))&&ReserveLedger.ordinary(state,Metal.ELECTRUM)>=cost){mob.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.debit(state,Metal.ELECTRUM,cost));MetalPhysics.externalMotion(mob,side,AllomancyRuntime.config.maximumObjectSpeed());s.dodge=tick;}break;
   }
  }
  if(target==null||tick-s.contact<dev.metallicarts.config.AdvancedConfig.get().enhancement().contactCooldown()||mob.distanceToSqr(target)>6.25||!mob.hasLineOfSight(target))return;
  double chrome=s.paid.getOrDefault(Metal.CHROMIUM,0.0),nicro=s.paid.getOrDefault(Metal.NICROSIL,0.0);if(chrome<=0&&nicro<=0||!dev.metallicarts.api.ArtsProtection.npcAllows(mob,target,chrome>0?dev.metallicarts.api.ArtsProtection.Action.LEECH:dev.metallicarts.api.ArtsProtection.Action.AMPLIFY))return;
  s.contact=tick;if(target instanceof net.minecraft.server.level.ServerPlayer p){if(chrome>0){p.setAttached(ModRegistries.SPIRITWEB,AdvancedAllomancy.drain(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),EnumSet.allOf(Metal.class)));AllomancyRuntime.stop(p);dev.metallicarts.network.ModNetworking.send(p);}else AdvancedAllomancy.amplify(p);}else if(target instanceof Mob m){if(chrome>0)leech(m);else boost(m);}
 }
 public static double cloudAt(LivingEntity target){double strength=0;for(var s:SESSIONS.values()){double power=s.paid.getOrDefault(Metal.COPPER,0.0);if(power>0&&s.mob.level()==target.level()&&s.mob.distanceToSqr(target)<=Math.pow(Math.min(18,6+3*power),2))strength=Math.max(strength,power);}return strength;}
 private static void modifier(Mob mob,net.minecraft.core.Holder<Attribute> attribute,String name,double amount){var a=mob.getAttribute(attribute);if(a==null)return;var id=dev.metallicarts.MetallicArts.id(name);if(amount==0)a.removeModifier(id);else if(a.getModifier(id)==null||a.getModifier(id).amount()!=amount)a.addOrUpdateTransientModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_VALUE));}
 private static void clearModifiers(Mob mob){modifier(mob,Attributes.ATTACK_DAMAGE,"npc_pewter",0);modifier(mob,Attributes.FOLLOW_RANGE,"npc_tin",0);}
 public static void clear(){SESSIONS.values().forEach(s->clearModifiers(s.mob));SESSIONS.clear();}
 private NpcArts(){}
}
