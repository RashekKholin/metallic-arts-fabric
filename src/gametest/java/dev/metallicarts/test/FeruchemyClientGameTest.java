package dev.metallicarts.test;

import dev.metallicarts.client.*;
import dev.metallicarts.allomancy.ReserveLedger;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.EconomyContent;
import dev.metallicarts.feruchemy.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;
import java.util.*;

public final class FeruchemyClientGameTest implements FabricClientGameTest {
    private static final String[] SLOTS={"1 Left ring","2 Right ring","3 Left wrist","4 Right wrist","5 Left bracer","6 Right bracer","7 Left ear","8 Right ear"};
    private static void mode(ClientGameTestContext c,int slot,int mode,String button){
        c.clickScreenButton(SLOTS[slot]);c.clickScreenButton(button);
        c.waitFor(client->FeruchemyClient.snapshot!=null&&FeruchemyClient.snapshot.modes().get(slot)==mode);
    }
    @Override public void runTest(ClientGameTestContext c){
        net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave save;
        UUID[] identity=new UUID[1];Map<UUID,Long> expected=new HashMap<>();long[] charged=new long[1];
        try(TestSingleplayerContext world=c.worldBuilder().create()){
            save=world.getWorldSave();
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();var state=p.getAttachedOrCreate(ModRegistries.SPIRITWEB);identity[0]=state.identity();
                for(Metal metal:List.of(Metal.IRON,Metal.STEEL,Metal.PEWTER,Metal.TIN)){
                    UUID id=UUID.randomUUID();state=ReserveLedger.grant(state,new PowerGrant(id,Art.FERUCHEMY,metal,PowerGrant.Origin.ADMIN,id,1));
                }
                p.setAttached(ModRegistries.SPIRITWEB,state);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)server.overworld().setBlock(new BlockPos(x,80,z),Blocks.STONE.defaultBlockState(),3);
                p.teleportTo(.5,81,.5);p.getInventory().setSelectedSlot(0);p.getInventory().setItem(0,new ItemStack(EconomyContent.item("iron","ring")));
            });
            c.getInput().pressKey(GLFW.GLFW_KEY_M);c.waitForScreen(SpiritwebScreen.class);
            c.getInput().pressKey(GLFW.GLFW_KEY_F);c.waitForScreen(MetalmindScreen.class);
            c.clickScreenButton("Equip held");c.waitFor(client->FeruchemyClient.snapshot!=null&&FeruchemyClient.snapshot.equipment().item(0).has(ModRegistries.METALMIND));
            mode(c,0,1,"Store");c.waitTicks(20);
            world.getServer().runOnServer(server->{if(FeruchemyRuntime.mass(server.getPlayerList().getPlayers().getFirst())!=60)throw new AssertionError("Weight storage has no mass cost");});
            mode(c,0,0,"Off");c.waitTicks(2);
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();var data=ContactService.equipment(p).item(0).get(ModRegistries.METALMIND);
                charged[0]=MetalmindLedger.get(server).resolve(data.token()).charge();if(charged[0]<=0)throw new AssertionError("No stored charge");
            });
            mode(c,0,2,"Tap");c.waitTicks(3);
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();var data=ContactService.equipment(p).item(0).get(ModRegistries.METALMIND);
                if(FeruchemyRuntime.mass(p)!=100||MetalmindLedger.get(server).resolve(data.token()).charge()>=charged[0])throw new AssertionError("Tapping did not spend charge");
            });
            mode(c,0,0,"Off");
            world.getServer().runOnServer(server->server.getPlayerList().getPlayers().getFirst().getInventory().setItem(0,new ItemStack(EconomyContent.item("steel","earring"))));
            c.clickScreenButton(SLOTS[1]);c.clickScreenButton("Equip held");
            c.waitFor(client->FeruchemyClient.snapshot.equipment().item(1).has(ModRegistries.METALMIND));
            mode(c,1,1,"Store");c.waitTicks(20);
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();var speed=p.getAttribute(Attributes.ATTACK_SPEED).getModifier(dev.metallicarts.MetallicArts.id("feruchemical_actions"));
                if(speed==null||speed.amount()!=-.25)throw new AssertionError("Speed storage has no action cost");
            });
            mode(c,1,2,"Tap");c.waitTicks(3);
            c.waitFor(client->client.player.getAttribute(Attributes.ATTACK_SPEED).getModifier(dev.metallicarts.MetallicArts.id("feruchemical_actions"))!=null&&
                client.player.getAttribute(Attributes.ATTACK_SPEED).getModifier(dev.metallicarts.MetallicArts.id("feruchemical_actions")).amount()==.25);
            world.getServer().runOnServer(server->{if(Math.abs(server.getPlayerList().getPlayers().getFirst().getDestroySpeed(Blocks.STONE.defaultBlockState())-1.25)>.0001)throw new AssertionError("Server mining schedule did not accelerate");});
            c.runOnClient(client->{if(Math.abs(client.player.getDestroySpeed(Blocks.STONE.defaultBlockState())-1.25)>.0001)throw new AssertionError("Client mining prediction differs");});
            mode(c,1,0,"Off");
            String[] senses={"sight","hearing","touch","smell","taste"};
            for(int i=0;i<5;i++){
                final int index=i;
                world.getServer().runOnServer(server->server.getPlayerList().getPlayers().getFirst().getInventory().setItem(0,new ItemStack(EconomyContent.item("tin","ring"))));
                if(i>0)c.clickScreenButton("Tin: "+senses[i-1]);
                c.clickScreenButton(SLOTS[i+2]);c.clickScreenButton("Equip held");
                c.waitFor(client->FeruchemyClient.snapshot.equipment().item(index+2).has(ModRegistries.METALMIND));
                mode(c,i+2,1,"Store");c.waitTicks(20);mode(c,i+2,0,"Off");
            }
            mode(c,2,2,"Tap");c.waitFor(client->FeruchemyClient.senses.sight()>1);
            c.runOnClient(client->{
                var light=new net.minecraft.client.renderer.state.LightmapRenderState();
                new net.minecraft.client.renderer.LightmapRenderStateExtractor(client.gameRenderer,client).extract(light,0);
                if(light.nightVisionEffectIntensity<=0)throw new AssertionError("Feruchemical sight not rendered");
            });
            mode(c,2,0,"Off");
            mode(c,6,1,"Store");
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();p.getFoodData().setFoodLevel(10);new ItemStack(Items.APPLE).finishUsingItem(p.level(),p);
            });
            c.waitFor(client->FeruchemyClient.senses.cue().contains("Taste muted"));
            mode(c,6,2,"Tap");c.waitFor(client->FeruchemyClient.senses.cue().contains("nutrition"));mode(c,6,0,"Off");
            c.getInput().pressKey(GLFW.GLFW_KEY_W);c.clickScreenButton("3");c.takeScreenshot("metallicarts-feruchemy-wheel");
            c.getInput().pressKey(GLFW.GLFW_KEY_W);
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();p.getInventory().setItem(0,new ItemStack(EconomyContent.item("pewter","bracer")));
                server.overworld().setBlock(new BlockPos(2,81,0),Blocks.IRON_BLOCK.defaultBlockState(),3);
            });
            c.clickScreenButton(SLOTS[7]);c.clickScreenButton("Equip held");
            c.waitFor(client->FeruchemyClient.snapshot.equipment().item(7).has(ModRegistries.METALMIND));
            c.clickScreenButton("+");c.clickScreenButton("+");mode(c,7,1,"Store");
            c.waitTicks(240);mode(c,7,0,"Off");c.clickScreenButton("+");mode(c,7,2,"Tap");
            c.waitFor(client->client.player.getAttribute(Attributes.SCALE).getModifier(dev.metallicarts.MetallicArts.id("feruchemical_bulk"))!=null&&
                Math.abs(client.player.getAttribute(Attributes.SCALE).getModifier(dev.metallicarts.MetallicArts.id("feruchemical_bulk")).amount()-.3)<.0001);
            c.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
            c.runOnClient(client->{
                var d=net.minecraft.world.phys.Vec3.atCenterOf(new BlockPos(2,81,0)).subtract(client.player.getEyePosition());
                client.player.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));
                client.player.setXRot((float)-Math.toDegrees(Math.atan2(d.y,Math.hypot(d.x,d.z))));
            });
            c.waitTicks(3);c.getInput().pressKey(GLFW.GLFW_KEY_B);
            c.waitFor(client->FeruchemyClient.senses.cue().contains("Cargo"));
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();
                if(PhysicalCargo.carriedMass(p)!=120||!server.overworld().getBlockState(new BlockPos(2,81,0)).isAir())throw new AssertionError("Real carry input did not move iron block");
            });
            c.runOnClient(client->{client.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);client.player.setXRot(0);});
            c.waitTicks(2);c.takeScreenshot("metallicarts-feruchemical-carry");c.getInput().pressKey(GLFW.GLFW_KEY_B);
            c.runOnClient(client->client.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
            c.getInput().pressKey(GLFW.GLFW_KEY_M);c.waitForScreen(SpiritwebScreen.class);
            c.getInput().pressKey(GLFW.GLFW_KEY_F);c.waitForScreen(MetalmindScreen.class);mode(c,7,0,"Off");
            c.takeScreenshot("metallicarts-metalminds");c.waitTicks(2);
            mode(c,0,1,"Store");
            Map<UUID,Long> transition=new HashMap<>();
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();
                for(var stack:ContactService.equipment(p).items()){
                    var data=stack.get(ModRegistries.METALMIND);
                    if(data!=null)transition.put(data.token().instance(),MetalmindLedger.get(server).resolve(data.token()).charge());
                }
                p.teleport(new net.minecraft.world.level.portal.TeleportTransition(server.getLevel(net.minecraft.world.level.Level.NETHER),
                    new net.minecraft.world.phys.Vec3(.5,100,.5),net.minecraft.world.phys.Vec3.ZERO,0,0,
                    net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING));
            });
            c.waitFor(client->client.level.dimension().equals(net.minecraft.world.level.Level.NETHER)&&FeruchemyClient.snapshot.modes().stream().allMatch(m->m==0));
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();
                if(FeruchemyRuntime.mass(p)!=80)throw new AssertionError("Dimension transition retained weight effect");
                for(var stack:ContactService.equipment(p).items()){
                    var data=stack.get(ModRegistries.METALMIND);
                    if(data!=null&&transition.get(data.token().instance())!=MetalmindLedger.get(server).resolve(data.token()).charge())throw new AssertionError("Dimension changed charge");
                }
                if(!FeruchemyRuntime.act(p,new dev.metallicarts.network.ContactIntent(2,1,0,1,1),server.getTickCount()))throw new AssertionError("Cannot start storage before death");
            });
            c.waitFor(client->FeruchemyClient.snapshot.modes().get(1)==1);
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();transition.clear();
                for(var stack:ContactService.equipment(p).items()){
                    var data=stack.get(ModRegistries.METALMIND);
                    if(data!=null)transition.put(data.token().instance(),MetalmindLedger.get(server).resolve(data.token()).charge());
                }
                p.kill(p.level());
            });
            c.waitFor(client->!client.player.isAlive()&&FeruchemyClient.snapshot.modes().stream().allMatch(m->m==0));
            c.runOnClient(client->client.getConnection().send(new net.minecraft.network.protocol.game.ServerboundClientCommandPacket(
                net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action.PERFORM_RESPAWN)));
            c.waitFor(client->client.player.isAlive()&&FeruchemyClient.snapshot.modes().stream().allMatch(m->m==0));
            c.runOnClient(client->client.setScreen(null));
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();
                for(var stack:ContactService.equipment(p).items()){
                    var data=stack.get(ModRegistries.METALMIND);
                    if(data!=null&&transition.get(data.token().instance())!=MetalmindLedger.get(server).resolve(data.token()).charge())throw new AssertionError("Death changed canonical charge");
                }
            });
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();
                for(var stack:ContactService.equipment(p).items()){
                    var data=stack.get(ModRegistries.METALMIND);if(data!=null)expected.put(data.token().instance(),MetalmindLedger.get(server).resolve(data.token()).charge());
                }
                if(expected.size()!=8)throw new AssertionError("Distinct metalminds lost");
            });
        }
        try(TestSingleplayerContext reopened=save.open()){
            c.waitFor(client->FeruchemyClient.snapshot!=null&&FeruchemyClient.snapshot.modes().stream().allMatch(m->m==0));
            reopened.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();
                if(!p.getAttachedOrCreate(ModRegistries.SPIRITWEB).identity().equals(identity[0])||FeruchemyRuntime.mass(p)!=80)throw new AssertionError("Identity lost or weight restored");
                int found=0;
                for(var stack:ContactService.equipment(p).items()){
                    var data=stack.get(ModRegistries.METALMIND);
                    if(data!=null){
                        var asset=MetalmindLedger.get(server).resolve(data.token());
                        if(!expected.containsKey(asset.token().instance())||expected.get(asset.token().instance())!=asset.charge())throw new AssertionError("Canonical charge lost on disk reload");
                        found++;
                    }
                }
                if(found!=8)throw new AssertionError("Contact items lost on reload");
            });
            c.getInput().pressKey(GLFW.GLFW_KEY_M);c.waitForScreen(SpiritwebScreen.class);
            c.getInput().pressKey(GLFW.GLFW_KEY_F);c.waitForScreen(MetalmindScreen.class);c.takeScreenshot("metallicarts-metalminds-reloaded");
        }
    }
}
