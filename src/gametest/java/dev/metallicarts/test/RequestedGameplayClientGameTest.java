package dev.metallicarts.test;
import dev.metallicarts.client.*;
import dev.metallicarts.allomancy.*;
import dev.metallicarts.arts.*;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.EconomyContent;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;
public final class RequestedGameplayClientGameTest implements FabricClientGameTest {
 public void runTest(ClientGameTestContext c){try(TestSingleplayerContext world=c.worldBuilder().create()){
  world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setNoGravity(true);p.setInvulnerable(true);p.teleportTo(.5,81,.5);for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)server.overworld().setBlock(new BlockPos(x,80,z),Blocks.STONE.defaultBlockState(),3);server.overworld().setBlock(new BlockPos(-4,82,4),Blocks.IRON_BLOCK.defaultBlockState(),3);server.overworld().setBlock(new BlockPos(4,82,4),Blocks.GOLD_BLOCK.defaultBlockState(),3);server.overworld().setBlock(new BlockPos(0,82,-5),Blocks.COPPER_BLOCK.defaultBlockState(),3);for(var metal:java.util.List.of(Metal.STEEL,Metal.IRON,Metal.BENDALLOY)){AdvancedSystemsGameTest.grant(p,Art.ALLOMANCY,metal);p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.credit(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),metal,1000000));AllomancyRuntime.request(p,AllomancyConfig.SUPPORTED.indexOf(metal),1,server.getTickCount());}p.getInventory().setItem(0,new ItemStack(EconomyContent.item("steel","coin"),16));});
  c.runOnClient(client->{var sound=client.getSoundManager().getSoundEvent(dev.metallicarts.MetallicArts.id("bronze_pulse"));if(sound==null||sound.getWeight()<=0)throw new AssertionError("Bronze pulse has no resolvable audio samples");});
  c.waitFor(client->AllomancyClient.status.mode(0)>0&&AllomancyClient.status.mode(1)>0&&TemporalPlayers.localRate(client.player)==3&&MetalSightClient.sources.size()>=3);c.runOnClient(client->{client.player.setYRot(0);client.player.setXRot(0);});c.waitTicks(25);c.takeScreenshot("metallicarts-all-direction-sight-sphere");
  world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();AllomancyRuntime.stopMetal(p,Metal.BENDALLOY);var mob=net.minecraft.world.entity.EntityType.VILLAGER.create(server.overworld(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);mob.setPos(.5,87,16.5);mob.setNoGravity(true);mob.setNoAi(true);server.overworld().addFreshEntity(mob);AdvancedSystemsGameTest.grant(mob,Art.ALLOMANCY,Metal.CADMIUM);mob.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.credit(mob.getAttachedOrCreate(ModRegistries.SPIRITWEB),Metal.CADMIUM,1000000));NpcArts.request(mob,Metal.CADMIUM,BurnMode.BURN);});c.waitFor(client->TemporalPlayers.localRate(client.player)==1&&TemporalFieldsClient.fields.stream().anyMatch(f->!f.fast()&&f.center().z>10));c.runOnClient(client->{client.player.setYRot(0);client.player.setXRot(-15);});c.waitTicks(35);c.takeScreenshot("metallicarts-full-sphere-outside");c.runOnClient(client->client.player.setXRot(0));
  c.getInput().pressKey(GLFW.GLFW_KEY_C);c.waitFor(client->(AllomancyClient.status.flags()&4)!=0&&MetalSightClient.sources.stream().anyMatch(s->s.selected()));c.takeScreenshot("metallicarts-auto-coin-lock");
  c.getInput().pressKey(GLFW.GLFW_KEY_E);c.waitForScreen(InventoryScreen.class);c.takeScreenshot("metallicarts-inventory-help-tab");c.clickScreenButton("Arts help");c.waitForScreen(ControlsScreen.class);
  c.runOnClient(client->{var mapping=KeyMapping.get("key.metallicarts.push");mapping.setKey(com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_Z));KeyMapping.resetMapping();if(!client.screen.getNarrationMessage().getString().contains("Z —"))throw new AssertionError("Controls help failed to show the rebound Push key");});c.takeScreenshot("metallicarts-controls-rebound");
  c.runOnClient(client->{var mapping=KeyMapping.get("key.metallicarts.push");mapping.setKey(mapping.getDefaultKey());KeyMapping.resetMapping();client.setScreen(null);});c.getInput().pressKey(GLFW.GLFW_KEY_I);c.waitForScreen(ControlsScreen.class);c.clickScreenButton("Next");c.takeScreenshot("metallicarts-nutrition-controls");c.clickScreenButton("Next");c.takeScreenshot("metallicarts-temporal-controls");
 }}
}
