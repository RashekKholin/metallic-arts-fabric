package dev.metallicarts.client;

import dev.metallicarts.network.*;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.minecraft.client.Minecraft;

public final class HemalurgyClient {
    public static SpikeSnapshot snapshot;
    public static void initialize(){
        ClientPlayNetworking.registerGlobalReceiver(SpikeSnapshot.TYPE,(payload,context)->snapshot=payload);
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->snapshot=null);
    }
    public static void open(){
        if(ClientPlayNetworking.canSend(SpikeIntent.TYPE)){send(0,0,0);Minecraft.getInstance().setScreen(new HemalurgyScreen());}
    }
    public static void send(int action,int point,int power){if(ClientPlayNetworking.canSend(SpikeIntent.TYPE))ClientPlayNetworking.send(new SpikeIntent(action,point,power));}
    private HemalurgyClient(){}
}
