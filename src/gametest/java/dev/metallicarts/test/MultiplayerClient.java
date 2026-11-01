package dev.metallicarts.test;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import dev.metallicarts.client.*;
import dev.metallicarts.domain.*;
import dev.metallicarts.hemalurgy.*;
import dev.metallicarts.network.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.world.item.*;
import org.lwjgl.glfw.GLFW;
import java.nio.file.*;
import java.util.*;

/** Two independent normal client JVMs, without the single-client gametest network synchronizer. */
public final class MultiplayerClient implements ClientModInitializer {
    private final Set<String> identities=new HashSet<>();
    private MultiplayerProbe.Instruction pending;private int delay,connections;private long nextConnect;
    private boolean push,look;private float yaw,pitch;
    @Override public void onInitializeClient(){
        String role=System.getProperty("metallicarts.multiplayer");if(!Set.of("A","B").contains(role==null?"":role))return;
        ClientPlayNetworking.registerGlobalReceiver(MultiplayerProbe.Instruction.TYPE,(p,c)->{pending=p;delay=-1;});
        ClientPlayConnectionEvents.JOIN.register((handler,sender,client)->connections++);
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            try{
                Path result=Path.of("../multiplayerServer/multiplayer-result.json");
                if(Files.exists(result)){Files.writeString(Path.of("client-result.json"),"{\"connections\":"+connections+",\"identitiesObserved\":"+identities.size()+"}");client.stop();return;}
                if(client.player==null){
                    if(System.currentTimeMillis()>nextConnect&&!(client.screen instanceof ConnectScreen)&&client.getOverlay()==null){
                        nextConnect=System.currentTimeMillis()+5000;
                        ConnectScreen.startConnecting(new TitleScreen(),client,ServerAddress.parseString("127.0.0.1:25570"),new ServerData("Metallic Arts local verification","127.0.0.1:25570",ServerData.Type.OTHER),false,null);
                    }return;
                }
                if(MetallicArtsClient.snapshot()!=null)identities.add(MetallicArtsClient.snapshot().identity().toString());
                KeyMapping.set(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_R),push);
                if(look){client.player.setYRot(yaw);client.player.setXRot(pitch);}
                if(pending==null)return;
                if(delay<0){execute(client,pending.action(),role);delay=0;}
                if(++delay>=15&&client.getConnection()!=null){
                    ClientPlayNetworking.send(new MultiplayerProbe.Reply(pending.sequence(),report(client).toString()));pending=null;
                }
            }catch(Throwable failure){try{Files.writeString(Path.of("client-failure.txt"),failure.toString());}catch(Exception ignored){}client.stop();}
        });
    }
    private void execute(Minecraft c,String action,String role){
        switch(action){
            case "M" -> c.setScreen(new SpiritwebScreen());
            case "F" -> FeruchemyClient.open();case "H" -> HemalurgyClient.open();
            case "EQUIP" -> ClientPlayNetworking.send(new ContactIntent(0,0,0,0,1));
            case "UNEQUIP" -> ClientPlayNetworking.send(new ContactIntent(1,0,0,0,1));
            case "STORE" -> ClientPlayNetworking.send(new ContactIntent(2,0,0,1,1));
            case "TAP" -> ClientPlayNetworking.send(new ContactIntent(2,0,0,2,1));
            case "OFF" -> ClientPlayNetworking.send(new ContactIntent(2,0,0,0,1));
            case "DROP" -> {c.setScreen(null);c.player.drop(false);}
            case "BURN_STEEL" -> ClientPlayNetworking.send(new BurnIntent(0,1));
            case "BURN_IRON" -> ClientPlayNetworking.send(new BurnIntent(1,1));
            case "STOP" -> {push=false;for(int i=0;i<16;i++)ClientPlayNetworking.send(new BurnIntent(i,0));ClientPlayNetworking.send(new ForceIntent(0));}
            case "PUSH" -> {c.setScreen(null);look=true;yaw=role.equals("A")?-90:90;pitch=0;push=true;}
            case "CONSENT" -> ClientPlayNetworking.send(new SpikeIntent(5,0,0));
            case "EXTRACT" -> {look=true;yaw=0;pitch=20;ClientPlayNetworking.send(new SpikeIntent(3,4,StolenPower.choices().indexOf(new StolenPower(StolenPower.Kind.ALLOMANCY,Metal.TIN))));}
            case "AIM_Z" -> {look=true;yaw=0;pitch=20;}
            case "INSERT" -> ClientPlayNetworking.send(new SpikeIntent(1,4,0));
            case "REMOVE" -> ClientPlayNetworking.send(new SpikeIntent(2,4,0));
            case "RESPAWN" -> c.getConnection().send(new net.minecraft.network.protocol.game.ServerboundClientCommandPacket(net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
            case "RECONNECT" -> {push=false;nextConnect=System.currentTimeMillis()+2500;c.disconnect(new TitleScreen(),false);}
            case "SELECT_COPY" -> {c.player.getInventory().setSelectedSlot(1);c.getConnection().send(new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(1));}
            case "PEEK" -> {}
            default -> {if(action.startsWith("BURN:")){int index=Integer.parseInt(action.substring(5));if(index==4||index==10||index==11){look=true;yaw=0;pitch=20;}ClientPlayNetworking.send(new BurnIntent(index,1));}else if(action.equals("FEAR"))ClientPlayNetworking.send(new ArtAction(0,1));else throw new IllegalArgumentException(action);}
        }
    }
    private JsonObject report(Minecraft c){
        var j=new JsonObject();j.addProperty("name",c.player.getGameProfile().name());j.addProperty("screen",c.screen==null?"none":c.screen.getClass().getSimpleName());
        var seen=new JsonArray();identities.forEach(seen::add);j.add("identities",seen);j.addProperty("connections",connections);
        var spirit=MetallicArtsClient.snapshot();if(spirit!=null){j.addProperty("identity",spirit.identity().toString());j.addProperty("steel",spirit.reserves().getOrDefault(Metal.STEEL,0L));j.addProperty("tinAccess",spirit.strength(Art.ALLOMANCY,Metal.TIN));}
        var f=FeruchemyClient.snapshot;j.addProperty("fMode",f==null?-1:f.modes().get(0));j.addProperty("contact",f!=null&&!f.equipment().item(0).isEmpty());
        j.addProperty("burnModes",AllomancyClient.status.modes());j.addProperty("targets",AllomancyClient.status.flags()>>8);j.addProperty("dx",c.player.getDeltaMovement().x);
        j.addProperty("alive",c.player.isAlive());j.addProperty("heldSpike",c.player.getMainHandItem().has(ModRegistries.SPIKE));
        j.addProperty("cue",AdvancedArtsClient.cue);j.addProperty("journal",AdvancedArtsClient.journal.toString());
        j.addProperty("message",HemalurgyClient.snapshot==null?"":HemalurgyClient.snapshot.message());return j;
    }
}
