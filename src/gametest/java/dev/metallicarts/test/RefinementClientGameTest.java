package dev.metallicarts.test;
import dev.metallicarts.client.*;
import dev.metallicarts.allomancy.*;
import dev.metallicarts.arts.*;
import dev.metallicarts.domain.*;
import dev.metallicarts.registry.ModRegistries;
import dev.metallicarts.network.ModNetworking;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;
public final class RefinementClientGameTest implements FabricClientGameTest {
 public void runTest(ClientGameTestContext c){try(TestSingleplayerContext world=c.worldBuilder().create()){
  c.runOnClient(client->{client.getWindow().setWindowed(1440,960);client.options.guiScale().set(2);client.resizeGui();});
  world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();p.setNoGravity(true);p.setInvulnerable(true);p.teleportTo(.5,81,.5);for(int x=-10;x<=10;x++)for(int z=-10;z<=10;z++)server.overworld().setBlock(new BlockPos(x,80,z),Blocks.STONE.defaultBlockState(),3);for(var m:Metal.values()){AdvancedSystemsGameTest.grant(p,Art.ALLOMANCY,m);AdvancedSystemsGameTest.grant(p,Art.FERUCHEMY,m);p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.credit(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),m,1000000));}for(var pos:java.util.List.of(new BlockPos(4,82,4),new BlockPos(-4,82,4),new BlockPos(0,82,-6)))server.overworld().setBlock(pos,Blocks.IRON_BLOCK.defaultBlockState(),3);for(var m:java.util.List.of(Metal.STEEL,Metal.IRON,Metal.BENDALLOY))AllomancyRuntime.request(p,AllomancyConfig.SUPPORTED.indexOf(m),1,server.getTickCount());ModNetworking.send(p);});
  c.waitFor(client->MetallicArtsClient.snapshot()!=null&&MetallicArtsClient.snapshot().grants().size()==32&&TemporalFieldsClient.fields.stream().anyMatch(f->f.fast()&&f.radius()==4));
  c.getInput().pressKey(GLFW.GLFW_KEY_M);c.waitForScreen(SpiritwebScreen.class);c.waitTicks(10);c.runOnClient(client->{long cells=client.screen.children().stream().filter(child->child instanceof AbstractWidget w&&AllomancyConfig.SUPPORTED.stream().anyMatch(m->m.id().equals(w.getMessage().getString()))).count();if(cells!=16)throw new AssertionError("All sixteen controls must appear together");for(var child:client.screen.children())if(child instanceof AbstractWidget w&&(w.getX()<0||w.getY()<0||w.getRight()>client.screen.width||w.getBottom()>client.screen.height))throw new AssertionError("Control outside viewport");});c.takeScreenshot("metallicarts-rc3-sixteen-metal-wheel");
  c.clickScreenButton("Zinc: anger");c.clickScreenButton("Brass: anger");c.clickScreenButton("Brass: fear");c.waitFor(client->AdvancedArtsClient.journal.has("brassEmotion")&&AdvancedArtsClient.journal.get("zincEmotion").getAsInt()==1&&AdvancedArtsClient.journal.get("brassEmotion").getAsInt()==2);
  c.getInput().pressKey(GLFW.GLFW_KEY_W);c.takeScreenshot("metallicarts-rc3-sixteen-metal-grid");c.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);c.getInput().pressKey(GLFW.GLFW_KEY_N);c.waitFor(client->(AllomancyClient.status.flags()&8)!=0&&(AllomancyClient.status.flags()>>8)>=3);
  for(int yaw:new int[]{0,90,180,270}){c.runOnClient(client->{client.player.setYRot(yaw);client.player.setXRot(0);});c.waitTicks(5);c.runOnClient(client->{var camera=client.gameRenderer.getMainCamera();var forward=new org.joml.Vector3f(camera.forwardVector()).mul(10);new org.joml.Quaternionf(camera.rotation()).conjugate().transform(forward);var p=SightProjection.project(forward.x,forward.y,-forward.z,camera.getFov(),1.5);if(!p.onScreen()||Math.abs(p.x())>.001||Math.abs(p.y())>.001)throw new AssertionError("Camera forward projection drifted at yaw "+yaw);});c.takeScreenshot("metallicarts-rc3-sight-yaw-"+yaw);}
  c.runOnClient(client->{client.getWindow().setWindowed(960,720);client.options.guiScale().set(3);client.resizeGui();});c.getInput().pressKey(GLFW.GLFW_KEY_M);c.waitForScreen(SpiritwebScreen.class);c.takeScreenshot("metallicarts-rc3-all-metals-narrow");c.runOnClient(client->{for(var child:client.screen.children())if(child instanceof AbstractWidget w&&(w.getX()<0||w.getY()<0||w.getRight()>client.screen.width||w.getBottom()>client.screen.height))throw new AssertionError("Narrow control outside viewport");client.options.guiScale().set(2);client.resizeGui();client.setScreen(null);});
 }}
}
