package dev.metallicarts.client;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.network.*;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class FeruchemyClient {
    public static ContactSnapshot snapshot;
    public static SenseStatus senses=SenseStatus.NORMAL;
    public static void initialize(){
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(dev.metallicarts.feruchemy.PhysicalCargo.TYPE,CargoRenderer::new);
        var category=net.minecraft.client.KeyMapping.Category.register(MetallicArts.id("physical"));
        var carry=net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping(new net.minecraft.client.KeyMapping(
            "key.metallicarts.carry",com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,org.lwjgl.glfw.GLFW.GLFW_KEY_B,category));
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client->{
            while(carry.consumeClick())if(client.player!=null&&client.screen==null&&ClientPlayNetworking.canSend(CargoIntent.TYPE))ClientPlayNetworking.send(new CargoIntent());
        });
        ClientPlayNetworking.registerGlobalReceiver(ContactSnapshot.TYPE,(payload,context)->snapshot=payload);
        ClientPlayNetworking.registerGlobalReceiver(SenseStatus.TYPE,(payload,context)->senses=payload);
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->{snapshot=null;senses=SenseStatus.NORMAL;});
        HudElementRegistry.addLast(MetallicArts.id("physical_senses"),(graphics,delta)->{
            var client=Minecraft.getInstance();
            if(!ClientPreferences.value(5)||client.player==null||client.screen!=null||client.options.hideGui||senses.cue().isEmpty())return;
            int y=22,count=0;
            for(var line:client.font.split(UiText.of(senses.cue()),client.getWindow().getGuiScaledWidth()-16)){
                if(count++>=2)break;
                graphics.text(client.font,line,8,y,0xFFCCDDCC);y+=10;
            }
        });
    }
    public static void open(){
        if(ClientPlayNetworking.canSend(ContactIntent.TYPE)){
            ClientPlayNetworking.send(new ContactIntent(3,0,0,0,1));
            Minecraft.getInstance().setScreen(new MetalmindScreen());
        }
    }
    public static void action(int action,int slot,int sense,int mode,int rate){
        if(ClientPlayNetworking.canSend(ContactIntent.TYPE))ClientPlayNetworking.send(new ContactIntent(action,slot,sense,mode,rate));
    }
    private FeruchemyClient(){}
}
