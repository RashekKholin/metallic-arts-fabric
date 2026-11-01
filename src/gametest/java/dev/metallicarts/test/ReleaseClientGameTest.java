package dev.metallicarts.test;
import dev.metallicarts.client.*;
import dev.metallicarts.hemalurgy.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import org.lwjgl.glfw.GLFW;
public final class ReleaseClientGameTest implements FabricClientGameTest {
 private static void bounds(Screen screen){for(var child:screen.children())if(child instanceof AbstractWidget w&& (w.getX()<0||w.getY()<0||w.getRight()>screen.width||w.getBottom()>screen.height))throw new AssertionError("Off-screen control in "+screen.getClass().getSimpleName()+": "+w.getMessage().getString());}
 public void runTest(ClientGameTestContext c){try(TestSingleplayerContext world=c.worldBuilder().create()){
  c.runOnClient(client->{client.getWindow().setWindowed(960,720);client.options.guiScale().set(3);client.resizeGui();});c.waitTicks(5);
  c.getInput().pressKey(GLFW.GLFW_KEY_M);c.waitForScreen(SpiritwebScreen.class);c.runOnClient(client->bounds(client.screen));
  c.runOnClient(client->client.setScreen(new GuideScreen()));c.waitForScreen(GuideScreen.class);c.runOnClient(client->{bounds(client.screen);if(client.screen.getNarrationMessage().getString().length()<100)throw new AssertionError("Guide body is not narrated");});c.takeScreenshot("metallicarts-guide-narrow");
  c.clickScreenButton("Metal lessons");c.waitForScreen(MetalLessonsScreen.class);c.runOnClient(client->bounds(client.screen));c.clickScreenButton("Allomancy");c.clickScreenButton("Next");c.runOnClient(client->{if(!client.screen.getNarrationMessage().getString().contains("Steel"))throw new AssertionError("Metal lesson navigation failed");});c.takeScreenshot("metallicarts-lesson-narrow");
  c.runOnClient(client->client.screen.onClose());c.waitForScreen(GuideScreen.class);c.clickScreenButton("Sensory settings");c.waitForScreen(SensorySettingsScreen.class);
  c.runOnClient(client->{bounds(client.screen);boolean old=ClientPreferences.value(6);ClientPreferences.toggle(6);if(ClientPreferences.value(6)==old)throw new AssertionError("Preference did not change");ClientPreferences.toggle(6);});c.takeScreenshot("metallicarts-settings-narrow");
  c.runOnClient(client->{Screen[] screens={new SystemsScreen(),new MemoryScreen(0),new JournalScreen(),new HemalurgyScreen()};for(var screen:screens){client.setScreen(screen);bounds(screen);}client.setScreen(null);});
  world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();p.teleportTo(.5,81,.5);p.setNoGravity(true);for(int x=-5;x<=5;x++)for(int z=-5;z<=8;z++)server.overworld().setBlock(new BlockPos(x,80,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);int x=-2;for(var type:java.util.List.of(NpcConstructs.KOLOSS,NpcConstructs.INQUISITOR,NpcConstructs.KANDRA)){var mob=type.create(server.overworld(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);mob.setPos(x,81,4);mob.setNoAi(true);mob.setYRot(180);mob.yBodyRot=180;mob.setYHeadRot(180);server.overworld().addFreshEntity(mob);NpcConstructs.tick(mob);x+=2;}});
  c.waitTicks(15);c.runOnClient(client->{int seen=0;for(var entity:client.level.entitiesForRendering())if(entity instanceof ConstructMob mob){seen++;var renderer=client.getEntityRenderDispatcher().getRenderer(mob);var state=renderer.createRenderState(mob,0);if(!(state instanceof ConstructAppearance.Data))throw new AssertionError("NPC renderer lacks construct state");}if(seen!=3)throw new AssertionError("Missing distinct construct NPCs: "+seen);client.player.setYRot(0);client.player.setXRot(0);});c.takeScreenshot("metallicarts-construct-npcs");
  c.runOnClient(client->{client.options.guiScale().set(2);client.resizeGui();});
 }}
}
