package dev.metallicarts.test;
import dev.metallicarts.client.*;
import dev.metallicarts.allomancy.*;
import dev.metallicarts.arts.*;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.*;
import dev.metallicarts.equipment.*;
import dev.metallicarts.feruchemy.*;
import dev.metallicarts.registry.ModRegistries;
import dev.metallicarts.network.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import org.lwjgl.glfw.GLFW;
import java.util.*;
/** Real controls, network memory transactions, typed power selection, and native furnace time. */
public final class AdvancedArtsClientGameTest implements FabricClientGameTest {
 private static void grant(ServerPlayer p,Art art,Metal metal){UUID id=UUID.randomUUID();p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.grant(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),new PowerGrant(id,art,metal,PowerGrant.Origin.ADMIN,id,1)));}
 public void runTest(ClientGameTestContext c){try(TestSingleplayerContext world=c.worldBuilder().create()){
  world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setNoGravity(true);p.teleportTo(.5,81,.5);for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)server.overworld().setBlock(new BlockPos(x,80,z),Blocks.STONE.defaultBlockState(),3);
   for(var m:Metal.values()){grant(p,Art.ALLOMANCY,m);grant(p,Art.FERUCHEMY,m);p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.credit(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),m,1000000));}
   var memory=new MemoryEntry(UUID.randomUUID(),"discovery","Found a refuge","minecraft:overworld",1,80,2,1);p.setAttached(ModRegistries.JOURNAL,new ArtsJournal(List.of(memory),List.of(memory),List.of("refuge")));p.getInventory().setSelectedSlot(0);p.getInventory().setItem(0,new ItemStack(EconomyContent.item("copper","ring")));ModNetworking.send(p);
  });
  c.getInput().pressKey(GLFW.GLFW_KEY_M);c.waitForScreen(SpiritwebScreen.class);c.clickScreenButton("copper");c.waitFor(client->AllomancyClient.status.mode(6)==1);c.takeScreenshot("metallicarts-mental-allomancy");
  c.getInput().pressKey(GLFW.GLFW_KEY_F);c.waitForScreen(MetalmindScreen.class);c.clickScreenButton("Equip held");c.waitFor(client->FeruchemyClient.snapshot.equipment().item(0).has(ModRegistries.METALMIND));c.getInput().pressKey(GLFW.GLFW_KEY_K);c.waitForScreen(MemoryScreen.class);
  c.waitFor(client->AdvancedArtsClient.memoryJournal.has("memories")&&AdvancedArtsClient.memoryJournal.getAsJsonArray("memories").size()==1);c.waitTicks(2);c.clickScreenButton("Found a refuge");c.waitFor(client->AdvancedArtsClient.memoryJournal.getAsJsonArray("memories").isEmpty()&&AdvancedArtsClient.memoryJournal.getAsJsonArray("stored").size()==1);c.takeScreenshot("metallicarts-coppermind-stored");
  world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();if(!p.getAttachedOrCreate(ModRegistries.JOURNAL).memories().isEmpty())throw new AssertionError("Memory still accessible after storing");});
  c.waitTicks(2);c.clickScreenButton("Found a refuge");c.waitFor(client->AdvancedArtsClient.memoryJournal.getAsJsonArray("stored").isEmpty());
  world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();if(!p.getAttachedOrCreate(ModRegistries.JOURNAL).memories().getFirst().label().equals("Found a refuge"))throw new AssertionError("Wrong memory restored");p.getInventory().setItem(0,new ItemStack(EconomyContent.item("nicrosil","ring")));});
  c.runOnClient(client->client.setScreen(null));c.getInput().pressKey(GLFW.GLFW_KEY_M);c.waitForScreen(SpiritwebScreen.class);c.getInput().pressKey(GLFW.GLFW_KEY_F);c.waitForScreen(MetalmindScreen.class);c.clickScreenButton("2 "+ContactEquipment.NAMES.get(1));c.getInput().pressKey(GLFW.GLFW_KEY_N);c.waitForScreen(PowerSelectScreen.class);c.clickScreenButton("steel");c.waitForScreen(MetalmindScreen.class);c.clickScreenButton("Equip held");c.waitFor(client->FeruchemyClient.snapshot.equipment().item(1).has(ModRegistries.METALMIND));c.clickScreenButton("Store");c.waitFor(client->FeruchemyClient.snapshot.modes().get(1)==1);c.waitTicks(5);c.clickScreenButton("Off");c.waitFor(client->FeruchemyClient.snapshot.modes().get(1)==0);c.takeScreenshot("metallicarts-typed-nicrosil");
  world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();var d=ContactService.equipment(p).item(1).get(ModRegistries.METALMIND);var a=MetalmindLedger.get(server).resolve(d.token());if(a.charge()==0||a.content().power().orElseThrow().metal()!=Metal.STEEL||EffectivePower.strength(p,Art.ALLOMANCY,Metal.STEEL)!=1)throw new AssertionError("Typed UI failed or retained access suppression");
   AllomancyRuntime.stop(p);p.teleportTo(.5,81,.5);var pos=new BlockPos(2,81,0);server.overworld().setBlock(pos,Blocks.FURNACE.defaultBlockState(),3);var furnace=(FurnaceBlockEntity)server.overworld().getBlockEntity(pos);furnace.setItem(0,new ItemStack(Items.IRON_ORE,8));furnace.setItem(1,new ItemStack(Items.COAL,8));if(!AllomancyRuntime.request(p,15,1,server.getTickCount()))throw new AssertionError("Cannot start local time");
  });
  c.runOnClient(client->client.setScreen(null));c.waitTicks(30);
  world.getServer().runOnServer(server->{var furnace=(FurnaceBlockEntity)server.overworld().getBlockEntity(new BlockPos(2,81,0));int cooked=furnace.saveWithoutMetadata(server.registryAccess()).getShortOr("cooking_time_spent",(short)0);if(cooked<70||cooked>115)throw new AssertionError("Native furnace not accelerated threefold: "+cooked);var p=server.getPlayerList().getPlayers().getFirst();AllomancyRuntime.stop(p);});
 }}
}
