package dev.metallicarts.arts;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;
/** Explicit recipient opt-in; moves one actual accessible record, with no copied private journal payload. */
public final class MemorySharing {
 public static void transfer(ServerPlayer from,ServerPlayer to,UUID memory){
  if(from==to||!from.isAlive()||!to.isAlive()||from.level()!=to.level()||from.distanceToSqr(to)>9||!from.hasLineOfSight(to)||!to.getAttachedOrCreate(ModRegistries.MEMORY_SHARING))throw new IllegalArgumentException("Aim within three blocks at a player who enabled memory sharing");
  if(!dev.metallicarts.api.ArtsProtection.allows(from,to,dev.metallicarts.api.ArtsProtection.Action.MEMORY_TRANSFER))throw new IllegalArgumentException("Memory transfer protected");
  var a=from.getAttachedOrCreate(ModRegistries.JOURNAL);var b=to.getAttachedOrCreate(ModRegistries.JOURNAL);var entry=a.memories().stream().filter(m->m.id().equals(memory)).findFirst().orElseThrow(()->new IllegalArgumentException("Memory is not accessible"));
  var nextB=b.remember(entry);var nextA=a.forget(memory);from.setAttached(ModRegistries.JOURNAL,nextA);to.setAttached(ModRegistries.JOURNAL,nextB);
 }
 private MemorySharing(){}
}
