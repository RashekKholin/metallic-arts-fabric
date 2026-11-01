package dev.metallicarts.compat;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.api.ArtsProtection;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import java.lang.reflect.Method;

/** Optional dynamic API link; no OpenPAC implementation or dependency is bundled.
 * Signatures verified against the official 26.1.2 branch, IChunkProtectionAPI v0.33.0.
 * An installed but incompatible API denies effects instead of silently bypassing claims. */
public final class OpenPacAdapter {
 private static Method api,protection,entity,block,access;
 private static boolean failed;
 public static void initialize(){
  if(!FabricLoader.getInstance().isModLoaded("openpartiesandclaims"))return;
  try{
   var entry=Class.forName("xaero.pac.common.server.api.OpenPACServerAPI");
   var contract=Class.forName("xaero.pac.common.server.claims.protection.api.IChunkProtectionAPI");
   api=entry.getMethod("get",MinecraftServer.class);protection=entry.getMethod("getChunkProtection");
   entity=contract.getMethod("onEntityInteraction",Entity.class,Entity.class,Entity.class,ItemStack.class,InteractionHand.class,boolean.class,boolean.class,boolean.class);
   block=contract.getMethod("onBlockInteraction",Entity.class,InteractionHand.class,ItemStack.class,ServerLevel.class,BlockPos.class,Direction.class,boolean.class,boolean.class,boolean.class);
   access=contract.getMethod("hasChunkAccess",Entity.class,net.minecraft.resources.Identifier.class,int.class,int.class);
  }catch(ReflectiveOperationException|LinkageError e){fail(e);}
  ArtsProtection.ENTITY_EVENT.register((actor,target,action)->entityAllowed(actor,target,action));
  ArtsProtection.NPC_EVENT.register((actor,target,action)->entityAllowed(actor,target,action));
  ArtsProtection.BLOCK_EVENT.register((actor,pos,action)->blockAllowed(actor,pos));
  MetallicArts.LOGGER.info("Open Parties and Claims adapter installed; API failures deny affected actions");
 }
 private static Object protection(Entity actor)throws ReflectiveOperationException{return protection.invoke(api.invoke(null,actor.level().getServer()));}
 private static boolean entityAllowed(Entity actor,Entity target,ArtsProtection.Action action){if(failed||!(target.level() instanceof ServerLevel level)||!temporalAllowed(actor,level,target.blockPosition()))return false;try{return !(boolean)entity.invoke(protection(actor),actor,actor,target,ItemStack.EMPTY,InteractionHand.MAIN_HAND,action!=ArtsProtection.Action.MEMORY_TRANSFER,false,false);}catch(ReflectiveOperationException|RuntimeException e){fail(e);return false;}}
 private static boolean blockAllowed(ServerPlayer actor,BlockPos pos){if(failed)return false;try{return !(boolean)block.invoke(protection(actor),actor,InteractionHand.MAIN_HAND,ItemStack.EMPTY,actor.level(),pos,Direction.UP,true,false,false);}catch(ReflectiveOperationException|RuntimeException e){fail(e);return false;}}
 public static boolean temporalAllowed(Entity actor,ServerLevel level,BlockPos pos){if(!FabricLoader.getInstance().isModLoaded("openpartiesandclaims"))return true;if(failed)return false;try{return (boolean)access.invoke(protection(actor),actor,level.dimension().identifier(),pos.getX()>>4,pos.getZ()>>4);}catch(ReflectiveOperationException|RuntimeException e){fail(e);return false;}}
 private static void fail(Throwable e){if(!failed)MetallicArts.LOGGER.error("OpenPAC API unavailable; protected mod actions fail closed",e);failed=true;}
 private OpenPacAdapter(){}
}
