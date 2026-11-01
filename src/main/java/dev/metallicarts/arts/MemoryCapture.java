package dev.metallicarts.arts;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
public final class MemoryCapture {
 public static void observe(ServerPlayer p,String kind,String label,String key){if(label.length()>128)label=label.substring(0,128);if(key.length()>192)key=key.substring(0,192);var journal=p.getAttachedOrCreate(ModRegistries.JOURNAL);p.setAttached(ModRegistries.JOURNAL,journal.observe(p,kind,label,key));}
 public static void initialize(){
  net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server->{if(server.getTickCount()%100==0)for(var p:server.getPlayerList().getPlayers())if(p.isSleeping())observe(p,"rest","Rested at a real bed","rest:"+p.level().dimension().identifier()+":"+p.blockPosition().asLong());});
  PlayerBlockBreakEvents.AFTER.register((world,player,pos,state,blockEntity)->{if(player instanceof ServerPlayer p&&state.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,dev.metallicarts.MetallicArts.id("fortune_opportunities")))){String id=BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();observe(p,"discovery","Mined "+id,"mined:"+id);}});
  ServerLivingEntityEvents.AFTER_DEATH.register((entity,source)->{if(source.getEntity() instanceof ServerPlayer p)observe(p,"encounter","Defeated "+entity.getType().getDescription().getString(),"defeat:"+entity.getUUID());});
  ServerPlayerEvents.AFTER_RESPAWN.register((old,p,alive)->{if(!alive)observe(p,"return","Returned after death in "+old.level().dimension().identifier(),"return:"+p.level().getServer().overworld().getGameTime());});
 }
 private MemoryCapture(){}
}
