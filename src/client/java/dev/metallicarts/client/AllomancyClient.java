package dev.metallicarts.client;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.allomancy.AllomancyConfig;
import dev.metallicarts.network.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class AllomancyClient {
    public static AllomancyStatus status=new AllomancyStatus(0,0);
    private static int ticks,lastFlags;
    public static boolean tin(){return status.mode(3)>0;}
    public static void initialize(){
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
            dev.metallicarts.allomancy.CoinProjectile.TYPE,net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(AllomancyStatus.TYPE,(payload,context)->status=payload);
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->{status=new AllomancyStatus(0,0);ticks=0;lastFlags=0;});
        var category=KeyMapping.Category.register(MetallicArts.id("allomancy"));
        var push=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.metallicarts.push",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_R,category));
        var pull=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.metallicarts.pull",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_V,category));
        var toss=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.metallicarts.toss",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_C,category));
        var select=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.metallicarts.select",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_G,category));
        var clear=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.metallicarts.clear",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_H,category));
        var auto=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.metallicarts.auto_select",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_N,category));
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            if(client.player==null)return;
            while(toss.consumeClick())if(client.screen==null&&ClientPlayNetworking.canSend(TossCoin.TYPE))ClientPlayNetworking.send(new TossCoin());
            while(select.consumeClick())if(client.screen==null&&ClientPlayNetworking.canSend(SelectionIntent.TYPE))ClientPlayNetworking.send(new SelectionIntent(0));
            while(clear.consumeClick())if(client.screen==null&&ClientPlayNetworking.canSend(SelectionIntent.TYPE))ClientPlayNetworking.send(new SelectionIntent(1));
            while(auto.consumeClick())if(client.screen==null&&ClientPlayNetworking.canSend(SelectionIntent.TYPE))ClientPlayNetworking.send(new SelectionIntent(2));
            int flags=client.screen==null?((push.isDown()?1:0)|(pull.isDown()?2:0)):0;
            if((flags!=lastFlags||++ticks%3==0)&&ClientPlayNetworking.canSend(ForceIntent.TYPE)){
                ClientPlayNetworking.send(new ForceIntent(flags));lastFlags=flags;
            }
        });
        HudElementRegistry.addLast(MetallicArts.id("allomancy"),(graphics,delta)->{
            var client=net.minecraft.client.Minecraft.getInstance();
            if(client.player==null||client.options.hideGui||client.screen!=null)return;
            if((status.mode(0)>0||status.mode(1)>0)&&ClientPreferences.value(5)){var help=Component.translatable("controls.metallicarts.force",ControlsScreen.binding("push"),ControlsScreen.binding("pull"),ControlsScreen.binding("select"),ControlsScreen.binding("clear"),ControlsScreen.binding("toss"),ControlsScreen.binding("controls"));int row=client.getWindow().getGuiScaledHeight()-92;var helpLines=client.font.split(help,client.getWindow().getGuiScaledWidth()-16);graphics.fill(4,row-3,client.getWindow().getGuiScaledWidth()-4,row+helpLines.size()*10+(lastFlags==3?12:2),0xA0101820);for(var text:helpLines){graphics.text(client.font,text,8,row,0xFF88BBFF);row+=10;}if(lastFlags==3)graphics.text(client.font,Component.translatable("controls.metallicarts.opposed"),8,row,0xFFFFCC77);}
            ArtsHud.draw(graphics);StringBuilder line=new StringBuilder();
            if((status.flags()&1)!=0&&TinOptions.INSTANCE.showOverload())line.append("Tin overload  ");
            if((status.flags()&2)!=0)line.append("Pewter fatigue");
            if((status.flags()&4)!=0)line.append(" targets: ").append(status.flags()>>8);
            if(!line.isEmpty()){int y=8;for(var text:client.font.split(UiText.of(line.toString()),client.getWindow().getGuiScaledWidth()-16)){graphics.text(client.font,text,8,y,0xFF77BBFF);y+=10;if(y>38)break;}}
        });
    }
    public static void cycle(int index){
        if(ClientPlayNetworking.canSend(BurnIntent.TYPE))ClientPlayNetworking.send(new BurnIntent(index,(status.mode(index)+1)%3));
    }
    private AllomancyClient(){}
}
