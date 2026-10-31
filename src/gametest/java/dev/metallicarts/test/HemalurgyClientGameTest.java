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
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;
import java.util.*;

/** Real UI/network extraction, implantation, paid powers, blood preservation and exact save/reload. */
public final class HemalurgyClientGameTest implements FabricClientGameTest {
    private static void grant(LivingEntity e,Art art,Metal metal){
        UUID id=UUID.randomUUID();e.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.grant(e.getAttachedOrCreate(ModRegistries.SPIRITWEB),new PowerGrant(id,art,metal,PowerGrant.Origin.ADMIN,id,1)));
    }
    private static LivingEntity donor(net.minecraft.server.MinecraftServer server,ServerPlayer p,Art art,Metal metal){
        var e=EntityType.VILLAGER.create(server.overworld(),EntitySpawnReason.COMMAND);e.setNoAi(true);e.setPos(.5,81,2.5);server.overworld().addFreshEntity(e);grant(e,art,metal);
        p.teleportTo(.5,81,.5);p.setYRot(0);p.setXRot(0);return e;
    }
    private static void aim(ClientGameTestContext c){c.runOnClient(client->{client.player.setYRot(0);client.player.setXRot(20);});c.waitTicks(3);}
    private static void select(ClientGameTestContext c,int slot){
        c.runOnClient(client->{client.player.getInventory().setSelectedSlot(slot);client.getConnection().send(new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(slot));});c.waitTicks(2);
    }
    @Override public void runTest(ClientGameTestContext c){
        net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave save;
        UUID[] fSpike=new UUID[1];long[] charge=new long[1];
        try(TestSingleplayerContext world=c.worldBuilder().create()){
            save=world.getWorldSave();
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)server.overworld().setBlock(new BlockPos(x,80,z),Blocks.STONE.defaultBlockState(),3);
                donor(server,p,Art.ALLOMANCY,Metal.STEEL);p.getInventory().setSelectedSlot(0);p.getInventory().setItem(0,new ItemStack(EconomyContent.item("steel","spike")));
            });
            aim(c);c.getInput().pressKey(GLFW.GLFW_KEY_M);c.waitForScreen(SpiritwebScreen.class);
            c.getInput().pressKey(GLFW.GLFW_KEY_H);c.waitForScreen(HemalurgyScreen.class);
            c.clickScreenButton(">");c.clickScreenButton("Intent: OFF");c.clickScreenButton("Extract aimed");
            c.waitFor(client->client.player.getMainHandItem().has(ModRegistries.SPIKE));
            c.clickScreenButton("5");c.clickScreenButton("Insert held");
            c.waitFor(client->HemalurgyClient.snapshot.equipment().item(4).has(ModRegistries.SPIKE));
            c.takeScreenshot("metallicarts-hemalurgy-implanted");
            world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.credit(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),Metal.STEEL,10000));ModNetworking.send(p);});
            c.getInput().pressKey(GLFW.GLFW_KEY_A);c.waitForScreen(SpiritwebScreen.class);c.clickScreenButton("steel");
            c.waitFor(client->AllomancyClient.status.mode(0)==1);c.waitTicks(4);
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();
                if(ReserveLedger.ordinary(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),Metal.STEEL)>=10000)throw new AssertionError("Stolen power did not burn real fuel");
            });
            c.getInput().pressKey(GLFW.GLFW_KEY_H);c.waitForScreen(HemalurgyScreen.class);c.clickScreenButton("5");c.clickScreenButton("Remove");
            c.waitFor(client->AllomancyClient.status.mode(0)==0&&HemalurgyClient.snapshot.equipment().item(4).isEmpty());
            world.getServer().runOnServer(server->{var p=server.getPlayerList().getPlayers().getFirst();p.getInventory().setItem(1,new ItemStack(Items.GLASS_BOTTLE));p.getInventory().setSelectedSlot(1);});
            select(c,1);c.waitTicks(60);
            c.clickScreenButton("Draw blood");c.waitFor(client->client.player.getMainHandItem().is(HemalurgyCommands.BLOOD_VIAL));
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();var blood=p.getInventory().getItem(1);p.getInventory().setItem(1,ItemStack.EMPTY);p.setItemSlot(EquipmentSlot.OFFHAND,blood);p.getInventory().setSelectedSlot(0);
            });
            select(c,0);
            c.clickScreenButton("Blood preserve");c.waitFor(client->client.player.getMainHandItem().get(ModRegistries.SPIKE)!=null&&client.player.getMainHandItem().get(ModRegistries.SPIKE).blood());
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();if(!p.getOffhandItem().isEmpty()||p.getHealth()>=20)throw new AssertionError("Preservation did not spend collected blood");
                var e=donor(server,p,Art.FERUCHEMY,Metal.IRON);var ledger=MetalmindLedger.get(server);var lease=new AssetLease(e.getUUID(),0);
                var a=ledger.claim(ledger.create(FeruchemicalAttribute.WEIGHT,9000000,1,false).token(),lease);a=ledger.change(a.token(),lease,e.getAttachedOrCreate(ModRegistries.SPIRITWEB).identity(),500000);a=ledger.release(a.token(),lease);
                var mind=new ItemStack(EconomyContent.item("iron","ring"));mind.set(ModRegistries.METALMIND,MetalmindData.of(a));p.getInventory().setItem(3,mind);
                p.getInventory().setItem(2,new ItemStack(EconomyContent.item("pewter","spike")));p.getInventory().setSelectedSlot(2);
            });
            select(c,2);aim(c);for(int i=0;i<16;i++)c.clickScreenButton(">");
            // A newly opened H menu starts at A-Iron; sixteen advances selects F-Iron.
            c.clickScreenButton("Intent: OFF");c.clickScreenButton("Extract aimed");
            c.waitFor(client->client.player.getMainHandItem().has(ModRegistries.SPIKE));
            c.clickScreenButton("1");c.clickScreenButton("Insert held");
            c.waitFor(client->HemalurgyClient.snapshot.equipment().item(0).has(ModRegistries.SPIKE));
            world.getServer().runOnServer(server->server.getPlayerList().getPlayers().getFirst().getInventory().setSelectedSlot(3));
            select(c,3);
            c.getInput().pressKey(GLFW.GLFW_KEY_A);c.waitForScreen(SpiritwebScreen.class);c.getInput().pressKey(GLFW.GLFW_KEY_F);c.waitForScreen(MetalmindScreen.class);
            c.clickScreenButton("1 Left ring");c.clickScreenButton("Equip held");c.waitFor(client->FeruchemyClient.snapshot.equipment().item(0).has(ModRegistries.METALMIND));
            c.clickScreenButton("Tap");c.waitFor(client->FeruchemyClient.snapshot.modes().get(0)==2);c.waitTicks(5);c.clickScreenButton("Off");c.waitFor(client->FeruchemyClient.snapshot.modes().get(0)==0);
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();var data=ContactService.equipment(p).item(0).get(ModRegistries.METALMIND);
                charge[0]=MetalmindLedger.get(server).resolve(data.token()).charge();
                if(charge[0]>=500000)throw new AssertionError("Stolen Feruchemy did not access donor-keyed charge");
                fSpike[0]=HemalurgyRuntime.equipment(p).item(0).get(ModRegistries.SPIKE).token().instance();
            });
        }
        try(TestSingleplayerContext world=save.open()){
            c.waitFor(client->HemalurgyClient.snapshot!=null&&HemalurgyClient.snapshot.equipment().item(0).has(ModRegistries.SPIKE));
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();var ref=HemalurgyRuntime.equipment(p).item(0).get(ModRegistries.SPIKE);
                if(!ref.token().instance().equals(fSpike[0])||p.getAttachedOrCreate(ModRegistries.SPIRITWEB).strength(Art.FERUCHEMY,Metal.IRON)<=0)throw new AssertionError("Saved implantation lost its source");
                var mind=ContactService.equipment(p).item(0).get(ModRegistries.METALMIND);if(MetalmindLedger.get(server).resolve(mind.token()).charge()!=charge[0])throw new AssertionError("Donor mind charge changed during reload");
                if(!p.getInventory().getItem(0).get(ModRegistries.SPIKE).blood())throw new AssertionError("Blood preservation lost on save");
            });
            c.getInput().pressKey(GLFW.GLFW_KEY_M);c.waitForScreen(SpiritwebScreen.class);c.getInput().pressKey(GLFW.GLFW_KEY_H);c.waitForScreen(HemalurgyScreen.class);c.takeScreenshot("metallicarts-hemalurgy-reloaded");
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();var ledger=SpikeLedger.get(server);
                var a=ledger.create(Metal.IRON,new StolenPower(StolenPower.Kind.STRENGTH,Metal.IRON),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1,server.overworld().getGameTime());
                var stack=new ItemStack(EconomyContent.item("iron","spike"));stack.set(ModRegistries.SPIKE,SpikeData.of(a));p.getInventory().setItem(2,stack);
            });
            select(c,2);c.clickScreenButton("12");c.clickScreenButton("Insert critical");
            c.waitFor(client->HemalurgyClient.snapshot.equipment().item(11).has(ModRegistries.SPIKE));
            c.waitTicks(65);c.clickScreenButton("Remove");
            c.waitFor(client->!client.player.isAlive()&&HemalurgyClient.snapshot.equipment().item(11).isEmpty());
            c.runOnClient(client->client.getConnection().send(new net.minecraft.network.protocol.game.ServerboundClientCommandPacket(
                net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action.PERFORM_RESPAWN)));
            c.waitFor(client->client.player.isAlive()&&HemalurgyClient.snapshot.equipment().item(0).has(ModRegistries.SPIKE));
            c.waitTicks(5);
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();
                if(p.getAttachedOrCreate(ModRegistries.SPIRITWEB).strength(Art.FERUCHEMY,Metal.IRON)<=0||!p.isAlive())throw new AssertionError("Critical removal caused repeated death or lost unrelated implant");
                var mind=ContactService.equipment(p).item(0).get(ModRegistries.METALMIND);
                if(MetalmindLedger.get(server).resolve(mind.token()).charge()!=charge[0])throw new AssertionError("Critical death changed saved metalmind charge");
            });
            c.getInput().pressKey(GLFW.GLFW_KEY_M);c.waitForScreen(SpiritwebScreen.class);c.getInput().pressKey(GLFW.GLFW_KEY_H);c.waitForScreen(HemalurgyScreen.class);
            c.clickScreenButton("1");c.clickScreenButton("Remove");
            c.waitFor(client->HemalurgyClient.snapshot.equipment().item(0).isEmpty());
            world.getServer().runOnServer(server->{if(server.getPlayerList().getPlayers().getFirst().getAttachedOrCreate(ModRegistries.SPIRITWEB).strength(Art.FERUCHEMY,Metal.IRON)!=0)throw new AssertionError("Removed spike retained ability");});
        }
    }
}
