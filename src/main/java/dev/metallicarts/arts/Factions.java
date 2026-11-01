package dev.metallicarts.arts;
import dev.metallicarts.hemalurgy.*;
import dev.metallicarts.registry.ModRegistries;
import dev.metallicarts.economy.EconomyContent;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Items;
import java.util.*;
public final class Factions {
 public static String of(LivingEntity entity){if(entity instanceof ConstructMob mob)return NpcConstructs.kind(mob)==ConstructState.Kind.KANDRA?"independent":"empire";return entity.getAttachedOrCreate(ModRegistries.RESEARCHER)?"terris":"village";}
 public static void trade(Villager merchant,ServerPlayer player){player.setAttached(ModRegistries.FACTIONS,player.getAttachedOrCreate(ModRegistries.FACTIONS).change(of(merchant),2));}
 public static void tribute(ServerPlayer p){var target=AdvancedAllomancy.aimed(p,3).orElseThrow(()->new IllegalArgumentException("Aim at a faction member within three blocks"));if(!(target instanceof ConstructMob||target instanceof Villager))throw new IllegalArgumentException("Choose a supported faction member");String faction=of(target);var standing=p.getAttachedOrCreate(ModRegistries.FACTIONS);long tick=p.level().getGameTime();if(tick-standing.lastTribute()<400||SurvivalCommands.count(p,Items.GOLD_INGOT)<1||SurvivalCommands.count(p,EconomyContent.item("steel","ingot"))<4)throw new IllegalArgumentException("Tribute costs one Gold and four Steel ingots; wait twenty seconds between gifts");if(!dev.metallicarts.api.ArtsProtection.ENTITY_EVENT.invoker().allow(p,target,dev.metallicarts.api.ArtsProtection.Action.MEMORY_TRANSFER))throw new IllegalArgumentException("Faction interaction protected");SurvivalCommands.take(p,Items.GOLD_INGOT,1);SurvivalCommands.take(p,EconomyContent.item("steel","ingot"),4);p.setAttached(ModRegistries.FACTIONS,standing.change(faction,5).tribute(tick));}
 public static void initialize(){
  ServerLivingEntityEvents.AFTER_DAMAGE.register((entity,source,base,taken,blocked)->{if(!blocked&&taken>0&&source.getEntity() instanceof ServerPlayer p&&(entity instanceof Villager||entity instanceof ConstructMob))p.setAttached(ModRegistries.FACTIONS,p.getAttachedOrCreate(ModRegistries.FACTIONS).change(of(entity),-5));});
  ServerLivingEntityEvents.AFTER_DEATH.register((entity,source)->{if(source.getEntity() instanceof ServerPlayer p&&entity instanceof ConstructMob mob&&NpcConstructs.kind(mob)!=ConstructState.Kind.KANDRA)p.setAttached(ModRegistries.FACTIONS,p.getAttachedOrCreate(ModRegistries.FACTIONS).change("terris",2).change("village",1));});
  UseEntityCallback.EVENT.register((player,level,hand,entity,hit)->{if(!(entity instanceof ConstructMob mob)||NpcConstructs.kind(mob)!=ConstructState.Kind.KANDRA||!player.isShiftKeyDown())return InteractionResult.PASS;if(!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;
   if(mob.collapsed()||!dev.metallicarts.api.ArtsProtection.ENTITY_EVENT.invoker().allow(p,mob,dev.metallicarts.api.ArtsProtection.Action.CONSTRUCT))return InteractionResult.FAIL;var s=mob.getAttachedOrCreate(ModRegistries.CONSTRUCT);ConstructState.Shape shape;
   if(player.getItemInHand(hand).is(Items.BONE))shape=ConstructState.Shape.WOLF;else if(player.getItemInHand(hand).is(Items.SLIME_BALL))shape=ConstructState.Shape.SLIME;else if(player.getItemInHand(hand).isEmpty())shape=ConstructState.Shape.HUMAN;else return InteractionResult.PASS;
   int bit=1<<shape.ordinal();if((s.knownShapes()&bit)==0){var policy=dev.metallicarts.config.AdvancedConfig.get().constructs();int bones=shape==ConstructState.Shape.WOLF?policy.wolfBones():policy.slimeBones();var material=shape==ConstructState.Shape.WOLF?Items.LEATHER:Items.SLIME_BALL;if(SurvivalCommands.count(p,Items.BONE)<bones||SurvivalCommands.count(p,material)<policy.shapeMaterials())return InteractionResult.FAIL;SurvivalCommands.take(p,Items.BONE,bones);SurvivalCommands.take(p,material,policy.shapeMaterials());}
   mob.setAttached(ModRegistries.CONSTRUCT,new ConstructState(s.lineage(),s.kind(),shape,s.growthTicks(),s.degradedTicks(),s.knownShapes()|bit));p.setAttached(ModRegistries.FACTIONS,p.getAttachedOrCreate(ModRegistries.FACTIONS).change("independent",2));NpcConstructs.tick(mob);return InteractionResult.SUCCESS;
  });
 }
 private Factions(){}
}
