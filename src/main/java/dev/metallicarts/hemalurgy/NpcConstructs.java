package dev.metallicarts.hemalurgy;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.allomancy.*;
import dev.metallicarts.arts.*;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.EconomyContent;
import dev.metallicarts.equipment.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.biome.v1.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.*;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.*;

/** Authored rare world populations. Initial spikes represent generated NPC backstory sources,
 * not an extraction from an unseen real player. All later charge/transfer uses canonical ledgers. */
public final class NpcConstructs {
 public static EntityType<ConstructMob> KOLOSS,INQUISITOR,KANDRA;
 public static ConstructState.Kind kind(ConstructMob mob){return mob.getType()==KOLOSS?ConstructState.Kind.KOLOSS:mob.getType()==INQUISITOR?ConstructState.Kind.INQUISITOR:ConstructState.Kind.KANDRA;}
 public static void initialize(){
  KOLOSS=type("koloss");INQUISITOR=type("inquisitor");KANDRA=type("kandra");
  for(var type:List.of(KOLOSS,INQUISITOR,KANDRA)){FabricDefaultAttributeRegistry.register(type,Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,30).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.MOVEMENT_SPEED,.23).add(Attributes.FOLLOW_RANGE,24));
   SpawnPlacements.register(type,SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(entity,level,reason,pos,random)->dev.metallicarts.config.AdvancedConfig.get().constructs().enabled()&&random.nextDouble()<dev.metallicarts.config.WorldConfig.get().constructChance()&&Monster.checkMonsterSpawnRules(entity,level,reason,pos,random));
   BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(),MobCategory.MONSTER,type,1,1,1);
  }
  ServerEntityEvents.ENTITY_LOAD.register((entity,level)->{if(entity instanceof ConstructMob mob)provision(mob);});
  ServerEntityEvents.ENTITY_UNLOAD.register((entity,level)->{if(entity instanceof ConstructMob mob&&entity.getRemovalReason()==Entity.RemovalReason.DISCARDED){var removed=SpikeLedger.get(level.getServer()).discardOwned(mob.getUUID());ConstructLedger.get(level.getServer()).discard(removed);}});
  ServerLivingEntityEvents.AFTER_DEATH.register((entity,damage)->{if(entity instanceof ConstructMob mob)release(mob);});
 }
 private static EntityType<ConstructMob> type(String name){var key=ResourceKey.create(Registries.ENTITY_TYPE,MetallicArts.id(name));return Registry.register(BuiltInRegistries.ENTITY_TYPE,key,EntityType.Builder.<ConstructMob>of(ConstructMob::new,MobCategory.MONSTER).sized(.6f,1.95f).clientTrackingRange(8).build(key));}
 private static SpikeAsset create(ConstructMob mob,Metal metal,StolenPower power){return SpikeLedger.get(mob.level().getServer()).create(metal,power,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1,mob.level().getGameTime());}
 private static void put(ConstructMob mob,int slot,SpikeAsset free){var asset=SpikeLedger.get(mob.level().getServer()).claim(free.token(),new AssetLease(mob.getUUID(),slot),mob.level().getGameTime());var item=new ItemStack(EconomyContent.item(asset.metal().id(),"spike"));item.set(ModRegistries.SPIKE,SpikeData.of(asset));mob.setAttached(ModRegistries.SPIKES,mob.getAttachedOrCreate(ModRegistries.SPIKES).with(slot,item));if(asset.power().art().isPresent()){UUID id=UUID.randomUUID();var state=mob.getAttachedOrCreate(ModRegistries.SPIRITWEB);mob.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.grant(state,new PowerGrant(id,asset.power().art().get(),asset.power().metal(),PowerGrant.Origin.HEMALURGIC,asset.token().instance(),asset.potency())));}}
 public static void provision(ConstructMob mob){if(mob.getAttachedOrCreate(ModRegistries.NPC_CONSTRUCT_READY))return;
  if(mob.getAttachedOrCreate(ModRegistries.SPIKES).items().stream().anyMatch(s->!s.isEmpty()))throw new IllegalStateException("Uninitialized construct contains existing implants; refusing duplicate provisioning");
  var kind=kind(mob);var prep=ConstructLedger.get(mob.level().getServer());int needed=kind==ConstructState.Kind.KOLOSS?4:kind==ConstructState.Kind.KANDRA?2:11;if(!SpikeLedger.get(mob.level().getServer()).room(needed)||!prep.room(needed)){mob.discard();return;}mob.setAttached(ModRegistries.NPC_CONSTRUCT_READY,true);
  if(kind==ConstructState.Kind.KOLOSS){for(int slot:new int[]{0,1,4,5}){var a=create(mob,Metal.IRON,new StolenPower(StolenPower.Kind.STRENGTH,Metal.IRON));prep.prepare(a,ConstructLedger.Purpose.KOLOSS_STAPLE);put(mob,slot,a);}}
  else if(kind==ConstructState.Kind.KANDRA){var a=create(mob,Metal.IRON,new StolenPower(StolenPower.Kind.STRENGTH,Metal.IRON));var b=create(mob,Metal.IRON,new StolenPower(StolenPower.Kind.STRENGTH,Metal.IRON));prep.bless(a,b);put(mob,0,a);put(mob,1,b);}
  else{Metal[] materials={Metal.STEEL,Metal.STEEL,Metal.STEEL,Metal.STEEL,Metal.BRONZE,Metal.BRONZE,Metal.GOLD,Metal.BRASS,Metal.PEWTER,Metal.PEWTER,Metal.ELECTRUM};Metal[] powers={Metal.PEWTER,Metal.IRON,Metal.STEEL,Metal.TIN,Metal.BRONZE,Metal.COPPER,Metal.GOLD,Metal.ZINC,Metal.STEEL,Metal.PEWTER,Metal.DURALUMIN};for(int n=0;n<11;n++){int slot=n==10?11:n;var power=new StolenPower(n>=6&&n<=9?StolenPower.Kind.FERUCHEMY:StolenPower.Kind.ALLOMANCY,powers[n]);var a=create(mob,materials[n],power);if(slot==2||slot==3)prep.prepare(a,ConstructLedger.Purpose.INQUISITOR_EYE);if(slot==11)prep.prepare(a,ConstructLedger.Purpose.INQUISITOR_PIN);put(mob,slot,a);}}
  mob.setAttached(ModRegistries.CONSTRUCT,new ConstructState(kind==ConstructState.Kind.KANDRA?ConstructState.Lineage.MISTWRAITH:ConstructState.Lineage.HUMAN,kind,ConstructState.Shape.HUMAN,0,0,1));
  for(var g:mob.getAttachedOrCreate(ModRegistries.SPIRITWEB).grants())if(g.art()==Art.ALLOMANCY)mob.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.credit(mob.getAttachedOrCreate(ModRegistries.SPIRITWEB),g.metal(),100000));
 }
 public static Map<Integer,SpikeAsset> implants(ConstructMob mob){var result=new HashMap<Integer,SpikeAsset>();var ledger=SpikeLedger.get(mob.level().getServer());for(int i=0;i<12;i++){var ref=mob.getAttachedOrCreate(ModRegistries.SPIKES).item(i).get(ModRegistries.SPIKE);if(ref!=null)try{var a=ledger.owned(ref.token(),new AssetLease(mob.getUUID(),i));if(!a.drained())result.put(i,a);}catch(IllegalArgumentException ignored){}}return result;}
 public static void tick(ConstructMob mob){if(mob.getTarget() instanceof ServerPlayer player&&player.getAttachedOrCreate(ModRegistries.FACTIONS).get("empire")>=60&&kind(mob)!=ConstructState.Kind.KANDRA&&mob.getLastHurtByMob()!=player)mob.setTarget(null);if(!mob.isAlive()||!mob.getAttachedOrCreate(ModRegistries.NPC_CONSTRUCT_READY))return;var state=mob.getAttachedOrCreate(ModRegistries.CONSTRUCT);var assets=implants(mob);var prep=ConstructLedger.get(mob.level().getServer());
  boolean valid=switch(kind(mob)){case KOLOSS->Arrays.stream(new int[]{0,1,4,5}).allMatch(slot->assets.containsKey(slot)&&prep.find(assets.get(slot).token().instance()).filter(p->p.purpose()==ConstructLedger.Purpose.KOLOSS_STAPLE).isPresent());case INQUISITOR->assets.size()>=11&&assets.containsKey(11)&&prep.find(assets.get(11).token().instance()).filter(p->p.purpose()==ConstructLedger.Purpose.INQUISITOR_PIN).isPresent();case KANDRA->assets.containsKey(0)&&assets.containsKey(1)&&prep.find(assets.get(0).token().instance()).flatMap(ConstructLedger.Preparation::pair).isPresent()&&prep.find(assets.get(0).token().instance()).flatMap(ConstructLedger.Preparation::pair).equals(prep.find(assets.get(1).token().instance()).flatMap(ConstructLedger.Preparation::pair));default->false;};
  if(kind(mob)==ConstructState.Kind.INQUISITOR&&!assets.containsKey(11)){mob.hurtServer((ServerLevel)mob.level(),mob.damageSources().genericKill(),Float.MAX_VALUE);return;}
  long growth=Math.min(dev.metallicarts.config.AdvancedConfig.get().constructs().kolossGrowthTicks(),state.growthTicks()+1);int degraded=valid?0:Math.min(24000,state.degradedTicks()+1);var shape=state.shape();if(kind(mob)==ConstructState.Kind.KANDRA&&!valid&&degraded>=dev.metallicarts.config.AdvancedConfig.get().constructs().kandraDegradeTicks())shape=ConstructState.Shape.SLIME;
  mob.setAttached(ModRegistries.CONSTRUCT,new ConstructState(state.lineage(),state.kind(),shape,growth,degraded,state.knownShapes()));mob.publicBody(shape.ordinal(),!valid);
  double scale=kind(mob)==ConstructState.Kind.KOLOSS?1+(dev.metallicarts.config.AdvancedConfig.get().constructs().kolossMaximumScale()-1)*growth/dev.metallicarts.config.AdvancedConfig.get().constructs().kolossGrowthTicks():kind(mob)==ConstructState.Kind.KANDRA&&shape!=ConstructState.Shape.HUMAN?.5:1;
  modifier(mob,Attributes.SCALE,"npc_construct_scale",scale-1);modifier(mob,Attributes.ATTACK_DAMAGE,"npc_construct_force",valid?kind(mob)==ConstructState.Kind.KOLOSS?6:kind(mob)==ConstructState.Kind.INQUISITOR?2:0:-3);modifier(mob,Attributes.MOVEMENT_SPEED,"npc_construct_collapse",valid?0:-.18);
  if(mob.tickCount%20==0){var stateWeb=mob.getAttachedOrCreate(ModRegistries.SPIRITWEB);var activeSources=assets.values().stream().map(a->a.token().instance()).collect(java.util.stream.Collectors.toSet());for(var g:stateWeb.grants())if(g.origin()==PowerGrant.Origin.HEMALURGIC&&!activeSources.contains(g.source()))mob.setAttached(ModRegistries.SPIRITWEB,mob.getAttachedOrCreate(ModRegistries.SPIRITWEB).withoutSource(g.source()));for(var g:mob.getAttachedOrCreate(ModRegistries.SPIRITWEB).grants())if(g.art()==Art.ALLOMANCY&&!Set.of(Metal.ALUMINUM,Metal.DURALUMIN).contains(g.metal()))NpcArts.request(mob,g.metal(),valid&&mob.getTarget()!=null?BurnMode.BURN:BurnMode.OFF);}
 }
 public static void remove(ServerPlayer actor,int slot){var target=AdvancedAllomancy.aimed(actor,3).orElseThrow(()->new IllegalArgumentException("Aim at a construct within three blocks"));if(!(target instanceof ConstructMob mob)||slot<0||slot>=12||!actor.getMainHandItem().isEmpty()||actor.getInventory().getFreeSlot()<0||mob.getHealth()<=6||!dev.metallicarts.api.ArtsProtection.allows(actor,mob,dev.metallicarts.api.ArtsProtection.Action.EXTRACTION))throw new IllegalArgumentException("Need empty hand, inventory space and an eligible unprotected construct");var a=implants(mob).get(slot);if(a==null)throw new IllegalArgumentException("No valid implant at this point");if(!mob.hurtServer((ServerLevel)mob.level(),actor.damageSources().playerAttack(actor),6))throw new IllegalArgumentException("Protected injury; implant unchanged");if(!mob.isAlive())return;var free=SpikeLedger.get(mob.level().getServer()).release(a.token(),new AssetLease(mob.getUUID(),slot),mob.level().getGameTime());var item=mob.getAttachedOrCreate(ModRegistries.SPIKES).item(slot);item.set(ModRegistries.SPIKE,SpikeData.of(free));mob.setAttached(ModRegistries.SPIKES,mob.getAttachedOrCreate(ModRegistries.SPIKES).with(slot,ItemStack.EMPTY));mob.setAttached(ModRegistries.SPIRITWEB,mob.getAttachedOrCreate(ModRegistries.SPIRITWEB).withoutSource(a.token().instance()));actor.getInventory().add(item);tick(mob);}
 public static void release(ConstructMob mob){var ledger=SpikeLedger.get(mob.level().getServer());for(var entry:implants(mob).entrySet()){var free=ledger.release(entry.getValue().token(),new AssetLease(mob.getUUID(),entry.getKey()),mob.level().getGameTime());var item=mob.getAttachedOrCreate(ModRegistries.SPIKES).item(entry.getKey());item.set(ModRegistries.SPIKE,SpikeData.of(free));mob.spawnAtLocation((ServerLevel)mob.level(),item);}mob.setAttached(ModRegistries.SPIKES,ContactEquipment.empty());}
 private static void modifier(ConstructMob mob,net.minecraft.core.Holder<Attribute> type,String name,double amount){var a=mob.getAttribute(type);if(a==null)return;var id=MetallicArts.id(name);if(amount==0)a.removeModifier(id);else a.addOrUpdateTransientModifier(new AttributeModifier(id,amount,name.equals("npc_construct_scale")?AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL:AttributeModifier.Operation.ADD_VALUE));}
 private NpcConstructs(){}
}
