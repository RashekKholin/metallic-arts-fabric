package dev.metallicarts.client;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.network.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.minecraft.client.*;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import java.util.*;
public final class SystemsClient {
 public static String status="Waiting for server";public static final Map<UUID,PublicConstruct> APPEARANCE=new HashMap<>();
 public static void initialize(){
  ClientPlayNetworking.registerGlobalReceiver(SystemsSnapshot.TYPE,(payload,context)->status=payload.status());
  ClientPlayNetworking.registerGlobalReceiver(PublicConstruct.TYPE,(payload,context)->{if(payload.kind()<0||payload.kind()>3||payload.shape()<0||payload.shape()>2)return;if(APPEARANCE.size()>2048)APPEARANCE.clear();APPEARANCE.put(payload.player(),payload);});
  ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->{status="Waiting for server";APPEARANCE.clear();});
  var category=KeyMapping.Category.register(MetallicArts.id("systems"));var key=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.metallicarts.systems",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_P,category));
  ClientTickEvents.END_CLIENT_TICK.register(client->{while(key.consumeClick())if(client.player!=null&&client.screen==null)open();});
 }
 public static void open(){send(0,0,0,false);Minecraft.getInstance().setScreen(new SystemsScreen());}
 public static void send(int action,int selection,int slot,boolean intent){if(ClientPlayNetworking.canSend(SystemsIntent.TYPE))ClientPlayNetworking.send(new SystemsIntent(action,selection,slot,intent));}
 private SystemsClient(){}
}
