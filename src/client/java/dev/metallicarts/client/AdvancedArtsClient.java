package dev.metallicarts.client;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.network.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.*;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class AdvancedArtsClient {
    public static String cue="";public static JsonObject journal=new JsonObject(),memoryJournal=new JsonObject();
    public static void initialize(){
        ClientPlayNetworking.registerGlobalReceiver(ArtsFeedback.TYPE,(payload,context)->{cue=payload.cue();if(!payload.journal().isEmpty()){var data=JsonParser.parseString(payload.journal()).getAsJsonObject();if(data.has("memorySlot"))memoryJournal=data;else journal=data;}});
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->{cue="";journal=new JsonObject();memoryJournal=new JsonObject();});
        var category=KeyMapping.Category.register(MetallicArts.id("advanced_arts"));
        var opening=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.metallicarts.opening",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_J,category));
        var knowledge=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.metallicarts.journal",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_K,category));
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            while(opening.consumeClick())if(client.player!=null&&client.screen==null)action(1,0);
            while(knowledge.consumeClick())if(client.player!=null)openJournal();
        });
        HudElementRegistry.addLast(MetallicArts.id("advanced_feedback"),(g,delta)->{
            var client=Minecraft.getInstance();if(client.player==null||client.screen!=null||client.options.hideGui||cue.isEmpty())return;
            int y=46,count=0;for(var line:client.font.split(Component.literal(cue),client.getWindow().getGuiScaledWidth()-16)){if(count++>=3)break;g.text(client.font,line,8,y,0xFFEEDDCC);y+=10;}
        });
    }
    public static void action(int action,int value){if(ClientPlayNetworking.canSend(ArtAction.TYPE))ClientPlayNetworking.send(new ArtAction(action,value));}
    public static void openJournal(){action(3,0);Minecraft.getInstance().setScreen(new JournalScreen());}
    public static double access(dev.metallicarts.domain.Spiritweb state,dev.metallicarts.domain.Art art,dev.metallicarts.domain.Metal metal){var access=journal.getAsJsonObject("access");String key=art.name()+":"+metal.name();return access!=null&&access.has(key)?access.get(key).getAsDouble():state.strength(art,metal);}
    public static double ownedAccess(dev.metallicarts.domain.Spiritweb state,dev.metallicarts.domain.Art art,dev.metallicarts.domain.Metal metal){var access=journal.getAsJsonObject("baseAccess");String key=art.name()+":"+metal.name();return access!=null&&access.has(key)?access.get(key).getAsDouble():state.strength(art,metal);}
    private AdvancedArtsClient(){}
}
