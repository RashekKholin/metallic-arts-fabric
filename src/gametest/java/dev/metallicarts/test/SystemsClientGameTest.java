package dev.metallicarts.test;
import dev.metallicarts.client.*;
import dev.metallicarts.allomancy.*;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.EconomyContent;
import dev.metallicarts.equipment.*;
import dev.metallicarts.feruchemy.*;
import dev.metallicarts.hemalurgy.*;
import dev.metallicarts.network.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;
import java.util.*;
public final class SystemsClientGameTest implements FabricClientGameTest {
 private static void grant(ServerPlayer p,Art art,Metal metal){UUID id=UUID.randomUUID();p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.grant(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),new PowerGrant(id,art,metal,PowerGrant.Origin.ADMIN,id,1)));}
 private static ItemStack mind(ServerPlayer p,long charge){var ledger=MetalmindLedger.get(p.level().getServer());var lease=new AssetLease(UUID.randomUUID(),0);var a=ledger.claim(ledger.create(FeruchemicalAttribute.HEALTH,9000000,1,false).token(),lease);if(charge>0)a=ledger.change(a.token(),lease,p.getAttachedOrCreate(ModRegistries.SPIRITWEB).identity(),charge);a=ledger.release(a.token(),lease);var item=new ItemStack(EconomyContent.item("gold","ring"));item.set(ModRegistries.METALMIND,MetalmindData.of(a));return item;}
 public void runTest(ClientGameTestContext c){try(TestSingleplayerContext world=c.worldBuilder().create()){
  world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setNoGravity(true);p.teleportTo(.5,81,.5);for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)server.overworld().setBlock(new BlockPos(x,80,z),Blocks.STONE.defaultBlockState(),3);grant(p,Art.ALLOMANCY,Metal.GOLD);grant(p,Art.FERUCHEMY,Metal.GOLD);p.getInventory().setSelectedSlot(0);p.getInventory().setItem(0,mind(p,0));ModNetworking.send(p);});
  c.getInput().pressKey(GLFW.GLFW_KEY_M);c.waitForScreen(SpiritwebScreen.class);c.getInput().pressKey(GLFW.GLFW_KEY_F);c.waitForScreen(MetalmindScreen.class);c.clickScreenButton("Equip held");c.waitFor(client->FeruchemyClient.snapshot.equipment().item(0).has(ModRegistries.METALMIND));
  world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();p.getInventory().setItem(0,mind(p,1000));});c.waitFor(client->client.player.getMainHandItem().has(ModRegistries.METALMIND));
  c.runOnClient(client->client.setScreen(null));c.getInput().pressKey(GLFW.GLFW_KEY_P);c.waitForScreen(SystemsScreen.class);c.clickScreenButton("Intent: OFF");c.clickScreenButton("Compound held mind");c.waitFor(client->client.player.getMainHandItem().isEmpty());
  world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();var d=ContactService.equipment(p).item(0).get(ModRegistries.METALMIND);if(MetalmindLedger.get(server).resolve(d.token()).charge()!=10000)throw new AssertionError("UI compounding did not conserve exact output");});c.waitTicks(2);c.takeScreenshot("metallicarts-compounding");
  world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();ConstructRuntime.lineage(p,ConstructState.Lineage.MISTWRAITH);var ledger=SpikeLedger.get(server);for(int i=0;i<2;i++){var a=ledger.create(Metal.IRON,new StolenPower(StolenPower.Kind.STRENGTH,Metal.IRON),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1,server.overworld().getGameTime());var item=new ItemStack(EconomyContent.item("iron","spike"));item.set(ModRegistries.SPIKE,SpikeData.of(a));if(i==0)p.getInventory().setItem(0,item);else p.setItemSlot(EquipmentSlot.OFFHAND,item);}p.getInventory().setItem(1,new ItemStack(Items.BONE,2));});
  c.waitFor(client->client.player.getMainHandItem().has(ModRegistries.SPIKE)&&client.player.getOffhandItem().has(ModRegistries.SPIKE));c.clickScreenButton(">");c.clickScreenButton("Prepare: koloss_staple");c.clickScreenButton("Prepare held");c.waitTicks(3);
  world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();if(ConstructLedger.get(server).all().size()!=2||!p.getInventory().getItem(1).isEmpty())throw new AssertionError("Actual UI Blessing preparation failed or did not pay");});
  c.runOnClient(client->client.setScreen(null));c.getInput().pressKey(GLFW.GLFW_KEY_M);c.waitForScreen(SpiritwebScreen.class);c.getInput().pressKey(GLFW.GLFW_KEY_H);c.waitForScreen(HemalurgyScreen.class);c.clickScreenButton("1");c.clickScreenButton("Insert held");c.waitFor(client->HemalurgyClient.snapshot.equipment().item(0).has(ModRegistries.SPIKE));
  world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();p.getInventory().setItem(0,p.getOffhandItem());p.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);});c.waitFor(client->client.player.getMainHandItem().has(ModRegistries.SPIKE));c.clickScreenButton("2");c.clickScreenButton("Insert held");c.waitFor(client->HemalurgyClient.snapshot.equipment().item(1).has(ModRegistries.SPIKE));
  c.runOnClient(client->client.setScreen(null));c.getInput().pressKey(GLFW.GLFW_KEY_P);c.waitForScreen(SystemsScreen.class);c.clickScreenButton(">");c.clickScreenButton("Intent: OFF");c.clickScreenButton("Body: koloss");c.clickScreenButton("Body: inquisitor");c.clickScreenButton("Transform");c.waitFor(client->{var a=SystemsClient.APPEARANCE.get(client.player.getUUID());return a!=null&&a.kind()==3&&!a.collapsed();});
  world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();if(p.getAttachedOrCreate(ModRegistries.CONSTRUCT).kind()!=ConstructState.Kind.KANDRA)throw new AssertionError("No actual kandra transformation");p.getInventory().setItem(2,new ItemStack(Items.BONE,8));p.getInventory().setItem(3,new ItemStack(Items.LEATHER,2));});
  c.clickScreenButton("Shape: human");c.clickScreenButton("Assume form");c.waitFor(client->{var a=SystemsClient.APPEARANCE.get(client.player.getUUID());return a!=null&&a.shape()==1&&!a.collapsed();});c.runOnClient(client->{client.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);client.setScreen(null);});c.waitTicks(5);
  c.runOnClient(client->{var renderer=client.getEntityRenderDispatcher().getRenderer(client.player);var state=renderer.createRenderState(client.player,0);if(!(state instanceof ConstructAppearance.Data data)||data.metallicarts$visual()==null||!(data.metallicarts$visual().state() instanceof net.minecraft.client.renderer.entity.state.WolfRenderState))throw new AssertionError("Kandra body did not extract an actual wolf model");});c.takeScreenshot("metallicarts-kandra-wolf");
  world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();p.getInventory().clearContent();HemalurgyRuntime.remove(p,0);HemalurgyRuntime.remove(p,1);});c.waitFor(client->{var a=SystemsClient.APPEARANCE.get(client.player.getUUID());return a!=null&&a.collapsed();});c.waitTicks(5);c.takeScreenshot("metallicarts-kandra-collapse");c.runOnClient(client->client.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
 }}
}
