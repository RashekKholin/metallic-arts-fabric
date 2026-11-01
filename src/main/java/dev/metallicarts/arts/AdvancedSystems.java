package dev.metallicarts.arts;
import dev.metallicarts.hemalurgy.*;
import dev.metallicarts.network.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
public final class AdvancedSystems {
 private static final Map<UUID,int[]> REQUESTS=new HashMap<>();
 public static void initialize(){
  PayloadTypeRegistry.serverboundPlay().register(SystemsIntent.TYPE,SystemsIntent.CODEC);PayloadTypeRegistry.clientboundPlay().register(SystemsSnapshot.TYPE,SystemsSnapshot.CODEC);PayloadTypeRegistry.clientboundPlay().register(PublicConstruct.TYPE,PublicConstruct.CODEC);
  ServerPlayNetworking.registerGlobalReceiver(SystemsIntent.TYPE,(payload,context)->act(context.player(),payload));
  ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->{REQUESTS.remove(handler.player.getUUID());ConstructRuntime.stop(handler.player);});
  ServerLifecycleEvents.SERVER_STOPPING.register(server->REQUESTS.clear());
  ServerTickEvents.END_SERVER_TICK.register(server->{if(server.getTickCount()%20==0)for(var p:server.getPlayerList().getPlayers())appearance(p);});
  SystemsCommands.initialize();NpcCommands.initialize();
 }
 public static boolean act(ServerPlayer p,SystemsIntent request){
  int tick=p.level().getServer().getTickCount();var budget=REQUESTS.computeIfAbsent(p.getUUID(),u->new int[]{tick,0});if(tick-budget[0]>=20){budget[0]=tick;budget[1]=0;}
  if(++budget[1]>20||request.action()<0||request.action()>6||request.selection()<0||request.selection()>=StolenPower.choices().size()||request.slot()<0||request.slot()>=12||!p.isAlive()||p.isSpectator())return false;
  try{
   if(request.action()!=0&&!request.intent())throw new IllegalArgumentException("Turn explicit Intent ON before consuming material or changing a body");
   switch(request.action()){
    case 0->{}case 1->Compounding.burn(p,request.slot());
    case 2->{if(request.selection()>=ConstructLedger.Purpose.values().length)throw new IllegalArgumentException("Unknown preparation");ConstructRuntime.prepareHeld(p,ConstructLedger.Purpose.values()[request.selection()]);}
    case 3->{if(request.selection()>=ConstructState.Kind.values().length)throw new IllegalArgumentException("Unknown body");ConstructRuntime.transform(p,ConstructState.Kind.values()[request.selection()]);}
    case 4->{if(request.selection()>=ConstructState.Shape.values().length)throw new IllegalArgumentException("Unknown shape");ConstructRuntime.shape(p,ConstructState.Shape.values()[request.selection()]);}
    case 5->ConstructRuntime.compel(p);
    case 6->HemalurgyRuntime.extract(p,HemalurgyRuntime.aimedDonor(p).orElseThrow(()->new IllegalArgumentException("Aim at a donor within three blocks")),StolenPower.choices().get(request.selection()),true);
   }
   send(p,"Action completed");appearance(p);return true;
  }catch(IllegalArgumentException|ArithmeticException e){send(p,e.getMessage());return false;}
 }
 private static void appearance(ServerPlayer p){var s=p.getAttachedOrCreate(ModRegistries.CONSTRUCT);boolean enabled=dev.metallicarts.config.AdvancedConfig.get().constructs().enabled();boolean mist=s.lineage()==ConstructState.Lineage.MISTWRAITH&&s.kind()==ConstructState.Kind.HUMAN;var packet=new PublicConstruct(p.getUUID(),enabled?(mist?3:s.kind().ordinal()):0,mist?2:s.shape().ordinal(),enabled&&!ConstructRuntime.canUsePowers(p));var audience=new HashSet<>(PlayerLookup.tracking(p));audience.add(p);for(var watcher:audience)if(ServerPlayNetworking.canSend(watcher,PublicConstruct.TYPE))ServerPlayNetworking.send(watcher,packet);}
 private static void send(ServerPlayer p,String message){if(ServerPlayNetworking.canSend(p,SystemsSnapshot.TYPE))ServerPlayNetworking.send(p,new SystemsSnapshot(message+" • "+ConstructRuntime.status(p)));}
 private AdvancedSystems(){}
}
