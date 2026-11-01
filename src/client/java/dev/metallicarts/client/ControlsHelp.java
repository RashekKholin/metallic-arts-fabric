package dev.metallicarts.client;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.client.mixin.InventoryPositionAccess;
import net.fabricmc.fabric.api.client.screen.v1.*;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
public final class ControlsHelp {
 public static void initialize(){var key=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.metallicarts.controls",com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_I,KeyMapping.Category.register(MetallicArts.id("help"))));ClientTickEvents.END_CLIENT_TICK.register(client->{while(key.consumeClick())if(client.player!=null&&client.screen==null)client.setScreen(new ControlsScreen(null));});
  ScreenEvents.AFTER_INIT.register((client,screen,w,h)->{if(!(screen instanceof InventoryScreen))return;var button=Button.builder(Component.translatable("screen.metallicarts.arts_help"),b->client.setScreen(new ControlsScreen(screen))).bounds(0,0,76,18).build();Screens.getWidgets(screen).add(button);Runnable position=()->{var access=(InventoryPositionAccess)screen;button.setPosition(Math.clamp(access.metallicarts$left()+100,4,w-80),Math.max(4,access.metallicarts$top()-21));};position.run();ScreenEvents.afterTick(screen).register(s->position.run());});
 }
 private ControlsHelp(){}
}
