package dev.metallicarts.test;

import dev.metallicarts.allomancy.*;
import dev.metallicarts.client.*;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.EconomyContent;
import dev.metallicarts.network.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;
import java.util.*;

public final class AllomancyClientGameTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext c) {
        net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave save;
        UUID[] identity=new UUID[1];long[] remaining=new long[1];double[] startY=new double[1];
        try(TestSingleplayerContext world=c.worldBuilder().create()){
            save=world.getWorldSave();
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();
                var state=p.getAttachedOrCreate(ModRegistries.SPIRITWEB);identity[0]=state.identity();
                for(Metal metal:AllomancyConfig.SUPPORTED){
                    UUID id=UUID.randomUUID();
                    state=ReserveLedger.grant(state,new PowerGrant(id,Art.ALLOMANCY,metal,PowerGrant.Origin.ADMIN,id,1));
                    state=ReserveLedger.credit(state,metal,100000);
                }
                p.setAttached(ModRegistries.SPIRITWEB,state);
                p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)server.overworld().setBlock(new BlockPos(x,80,z),Blocks.IRON_BLOCK.defaultBlockState(),3);
                p.teleportTo(.5,81,.5);p.setXRot(90);p.setYRot(0);
                p.getInventory().setItem(0,new ItemStack(EconomyContent.item("steel","coin"),3));
                ModNetworking.send(p);
            });
            c.waitFor(client->MetallicArtsClient.snapshot()!=null&&MetallicArtsClient.snapshot().grants().size()==4);
            c.getInput().pressKey(GLFW.GLFW_KEY_M);c.waitForScreen(SpiritwebScreen.class);
            c.clickScreenButton("steel");c.waitFor(client->AllomancyClient.status.mode(0)==1);
            c.clickScreenButton("pewter");c.waitFor(client->AllomancyClient.status.mode(2)==1);
            c.clickScreenButton("tin");c.waitFor(client->AllomancyClient.status.mode(3)==1);
            c.runOnClient(client->{
                var light=new net.minecraft.client.renderer.state.LightmapRenderState();
                new net.minecraft.client.renderer.LightmapRenderStateExtractor(client.gameRenderer,client).extract(light,0);
                if(light.nightVisionEffectIntensity!=1)throw new AssertionError("Tin lightmap hook did not apply");
            });
            c.waitTicks(120);c.takeScreenshot("metallicarts-allomancy-menu");
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();startY[0]=p.getY();
                var modifier=p.getAttribute(Attributes.ATTACK_DAMAGE).getModifier(dev.metallicarts.MetallicArts.id("pewter_attack"));
                if(modifier==null||modifier.amount()!=3)throw new AssertionError("Pewter attack modifier absent");
                if(p.getAttachedOrCreate(ModRegistries.SPIRITWEB).reserves().get(Metal.TIN)>=100000)throw new AssertionError("Tin did not consume");
            });
            c.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
            c.runOnClient(client->{client.player.setXRot(90);client.player.setYRot(0);});
            c.waitTicks(3);
            c.getInput().holdKeyFor(GLFW.GLFW_KEY_R,12);
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();
                if(p.getY()<=startY[0]+.25)throw new AssertionError("Steel anchor push did not lift real client");
            });
            c.takeScreenshot("metallicarts-steel-push");
            c.getInput().pressKey(GLFW.GLFW_KEY_M);c.waitForScreen(SpiritwebScreen.class);
            c.clickScreenButton("steel");c.waitFor(client->AllomancyClient.status.mode(0)==2);
            c.clickScreenButton("steel");c.waitFor(client->AllomancyClient.status.mode(0)==0);
            c.clickScreenButton("tin");c.waitFor(client->AllomancyClient.status.mode(3)==2);
            c.clickScreenButton("tin");c.waitFor(client->AllomancyClient.status.mode(3)==0);
            // Lost access is authoritative even with an already-active client mode.
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();var state=p.getAttachedOrCreate(ModRegistries.SPIRITWEB);
                var grant=state.grants().stream().filter(g->g.metal()==Metal.PEWTER).findFirst().orElseThrow();
                p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.revoke(state,grant.id()));
            });
            c.waitFor(client->AllomancyClient.status.mode(2)==0);
            // Malformed client intents cannot grant the remaining metals or edit reserves.
            c.runOnClient(client->ClientPlayNetworking.send(new BurnIntent(15,2)));
            c.waitTicks(5);
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();
                if(p.getAttribute(Attributes.ATTACK_DAMAGE).hasModifier(dev.metallicarts.MetallicArts.id("pewter_attack")))throw new AssertionError("Revoked effect remains");
                remaining[0]=p.getAttachedOrCreate(ModRegistries.SPIRITWEB).reserves().get(Metal.STEEL);
            });
        }
        try(TestSingleplayerContext reopened=save.open()){
            c.waitFor(client->client.player!=null&&AllomancyClient.status.modes()==0);
            reopened.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();var state=p.getAttachedOrCreate(ModRegistries.SPIRITWEB);
                if(!state.identity().equals(identity[0])||state.reserves().get(Metal.STEEL)!=remaining[0])throw new AssertionError("Burned reserve or Identity lost");
                if(p.getAttribute(Attributes.ATTACK_DAMAGE).hasModifier(dev.metallicarts.MetallicArts.id("pewter_attack")))throw new AssertionError("Transient modifier restored");
            });
        }
    }
}
