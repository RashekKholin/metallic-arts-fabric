package dev.metallicarts.api;

import net.fabricmc.fabric.api.event.*;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;

/** Server-side adapter point for claim/protection mods. Any denial wins; exceptions deny the action. */
public final class ArtsProtection {
 public enum Action { EMOTION, LEECH, AMPLIFY, EXTRACTION, CONTROL, CONSTRUCT, COMPOUND, MOVE_BLOCK }
 @FunctionalInterface public interface Guard{boolean allow(ServerPlayer actor,LivingEntity target,Action action);}
 public static final Event<Guard> EVENT=EventFactory.createArrayBacked(Guard.class,guards->(actor,target,action)->{for(var guard:guards)try{if(!guard.allow(actor,target,action))return false;}catch(RuntimeException e){return false;}return true;});
 public static boolean allows(ServerPlayer actor,LivingEntity target,Action action){
  if(!actor.isAlive()||actor.isSpectator()||target==null||!target.isAlive()||target.level()!=actor.level())return false;
  if(target!=actor&&target instanceof ServerPlayer other&&(!actor.canHarmPlayer(other)||other.isCreative()))return false;
  if(!EVENT.invoker().allow(actor,target,action))return false;
  return target==actor||AttackEntityCallback.EVENT.invoker().interact(actor,actor.level(),InteractionHand.MAIN_HAND,target,null)==InteractionResult.PASS;
 }
 @FunctionalInterface public interface MobGuard{boolean allow(LivingEntity actor,LivingEntity target,Action action);}
 public static final Event<MobGuard> NPC_EVENT=EventFactory.createArrayBacked(MobGuard.class,guards->(actor,target,action)->{for(var guard:guards)try{if(!guard.allow(actor,target,action))return false;}catch(RuntimeException e){return false;}return true;});
 public static boolean npcAllows(LivingEntity actor,LivingEntity target,Action action){return actor.isAlive()&&target.isAlive()&&actor.level()==target.level()&&!(target instanceof ServerPlayer p&&(p.isCreative()||p.isSpectator()))&&NPC_EVENT.invoker().allow(actor,target,action);}
 @FunctionalInterface public interface BlockGuard{boolean allow(ServerPlayer actor,net.minecraft.core.BlockPos pos,Action action);}
 public static final Event<BlockGuard> BLOCK_EVENT=EventFactory.createArrayBacked(BlockGuard.class,guards->(actor,pos,action)->{for(var guard:guards)try{if(!guard.allow(actor,pos,action))return false;}catch(RuntimeException e){return false;}return true;});
 private ArtsProtection(){}
}
