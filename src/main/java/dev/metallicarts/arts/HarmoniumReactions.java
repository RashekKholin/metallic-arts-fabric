package dev.metallicarts.arts;
import dev.metallicarts.api.ArtsProtection;
import dev.metallicarts.economy.EconomyContent;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import java.util.*;
/** Bounded water reaction, no block destruction. Ownerless reactions are cosmetic, not an anonymous damage bypass. */
public final class HarmoniumReactions {
 private static final Set<ItemEntity> TRACKED=Collections.newSetFromMap(new WeakHashMap<>());
 public static boolean react(ItemEntity item){if(!dev.metallicarts.config.WorldConfig.get().harmoniumReactions()||item.isRemoved()||!item.isInWater()||!item.getItem().is(EconomyContent.ITEMS.get("harmonium_sample")))return false;
  var level=(ServerLevel)item.level();var owner=item.getOwner();item.getItem().shrink(1);level.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,item.getX(),item.getY(),item.getZ(),12,.3,.3,.3,.02);level.playSound(null,item.blockPosition(),net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,net.minecraft.sounds.SoundSource.BLOCKS,.5f,.7f);
  if(owner instanceof LivingEntity actor)for(var target:level.getEntitiesOfClass(LivingEntity.class,item.getBoundingBox().inflate(2))){boolean allowed=actor instanceof ServerPlayer p?ArtsProtection.allows(p,target,ArtsProtection.Action.AMPLIFY):ArtsProtection.npcAllows(actor,target,ArtsProtection.Action.AMPLIFY);if(allowed)target.hurtServer(level,actor instanceof ServerPlayer p?actor.damageSources().playerAttack(p):actor.damageSources().mobAttack(actor),4);}
  if(item.getItem().isEmpty())item.discard();return true;
 }
 public static void initialize(){ServerEntityEvents.ENTITY_LOAD.register((entity,level)->{if(entity instanceof ItemEntity item&&item.getItem().is(EconomyContent.ITEMS.get("harmonium_sample"))&&TRACKED.size()<1024)TRACKED.add(item);});ServerTickEvents.END_SERVER_TICK.register(server->{if(server.getTickCount()%10!=0)return;int checked=0;for(var iterator=TRACKED.iterator();iterator.hasNext();){var item=iterator.next();if(item.isRemoved()||!item.getItem().is(EconomyContent.ITEMS.get("harmonium_sample"))){iterator.remove();continue;}if(++checked<=128)react(item);}});ServerLifecycleEvents.SERVER_STOPPING.register(server->TRACKED.clear());}
 private HarmoniumReactions(){}
}
