package dev.metallicarts.test;

import dev.metallicarts.client.*;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.*;
import dev.metallicarts.metallurgy.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.*;
import org.lwjgl.glfw.GLFW;
import java.util.*;

public final class EconomyClientGameTest implements FabricClientGameTest {
    private static final BlockPos TABLE=new BlockPos(0,80,0),STAND=new BlockPos(2,80,0);
    @Override public void runTest(ClientGameTestContext context) {
        TestWorldSave save;
        try(TestSingleplayerContext world=context.worldBuilder().setUseConsistentSettings(false).adjustSettings(state->state.setSeed("metallicarts-phase2")).create()){
            save=world.getWorldSave();
            // A real, newly generated normal world: scan nine chunks for each added ore family.
            world.getServer().runOnServer(server->{
                var level=server.overworld();Set<String> found=new HashSet<>();
                var spawn=server.getRespawnData().globalPos().pos();
                int cx=spawn.getX()>>4,cz=spawn.getZ()>>4;
                BlockPos.MutableBlockPos cursor=new BlockPos.MutableBlockPos();
                for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){
                    var chunk=level.getChunk(cx+dx,cz+dz);
                    for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=-64;y<113;y++){
                        cursor.set((cx+dx)*16+x,y,(cz+dz)*16+z);
                        Block b=chunk.getBlockState(cursor).getBlock();
                        for(var ore:EconomyContent.ORES.entrySet())if(b==ore.getValue())found.add(ore.getKey().replace("deepslate_",""));
                    }
                }
                if(found.size()!=8)throw new AssertionError("Natural ore coverage: "+found);
                var player=server.getPlayerList().getPlayers().getFirst();player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.teleportTo(0.5,81,2.5);
                level.setBlock(TABLE,EconomyContent.TABLE.defaultBlockState(),3);
                level.setBlock(STAND,Blocks.BREWING_STAND.defaultBlockState(),3);
                // First playable loop: iron -> measured portions -> flakes -> water vial -> ingested reserve.
                var table=(MetallurgyBlockEntity)level.getBlockEntity(TABLE);
                table.tab=2;table.setItem(0,new ItemStack(Items.IRON_NUGGET));table.setItem(4,new ItemStack(Items.COAL,2));
                var options=MetallurgyRecipes.candidates(2,table.inputs());
                for(int i=0;i<options.size();i++)if(options.get(i).id().equals("iron_nugget_to_portions"))table.selection=i;
                table.running=true;for(int i=0;i<40;i++)MetallurgyBlockEntity.tick(level,TABLE,level.getBlockState(TABLE),table);
                if(table.getItem(5).getCount()!=99||table.getItem(6).getCount()!=1)throw new AssertionError("Portion conversion");
                ItemStack portions=table.removeItem(5,99);
                table.setItem(0,portions.split(1));player.getInventory().add(portions);table.running=true;table.selection=0;
                for(int i=0;i<20;i++)MetallurgyBlockEntity.tick(level,TABLE,level.getBlockState(TABLE),table);
                ItemStack flake=table.removeItem(5,1);
                if(!flake.is(EconomyContent.item("iron","flakes")))throw new AssertionError("Flake shaping");
                var stand=(BrewingStandBlockEntity)level.getBlockEntity(STAND);
                var water=new ItemStack(Items.POTION);water.set(DataComponents.POTION_CONTENTS,new PotionContents(Potions.WATER));
                stand.setItem(0,water);stand.setItem(3,flake);stand.setItem(4,new ItemStack(Items.BLAZE_POWDER));
                for(int i=0;i<=400;i++)BrewingStandBlockEntity.serverTick(level,STAND,level.getBlockState(STAND),stand);
                var vial=stand.removeItem(0,1);var returned=vial.finishUsingItem(level,player);
                if(!returned.is(Items.GLASS_BOTTLE))throw new AssertionError("Bottle return");
                var state=player.getAttachedOrCreate(ModRegistries.SPIRITWEB);
                if(state.reserves().getOrDefault(Metal.IRON,0L)!=Materials.PORTION_MASS||!state.grants().isEmpty())throw new AssertionError("Ingestion must not grant powers");
                table.setItem(0,new ItemStack(EconomyContent.item("iron","portion"),3));table.selection=0;
                player.openMenu(table);
            });
            context.waitForScreen(MetallurgyScreen.class);
            context.waitTicks(10);
            context.takeScreenshot("metallicarts-metallurgy");
            context.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
            context.getInput().pressKey(GLFW.GLFW_KEY_M);
            context.waitForScreen(SpiritwebScreen.class);
            context.waitTicks(120);
            context.takeScreenshot("metallicarts-economy-reserves");
        }
        try(TestSingleplayerContext reopened=save.open()){
            reopened.getServer().runOnServer(server->{
                var player=server.getPlayerList().getPlayers().getFirst();
                if(player.getAttachedOrCreate(ModRegistries.SPIRITWEB).reserves().getOrDefault(Metal.IRON,0L)!=Materials.PORTION_MASS)throw new AssertionError("Ingested reserve lost on world reload");
                var table=(MetallurgyBlockEntity)server.overworld().getBlockEntity(TABLE);
                if(table==null||table.getItem(6).getCount()!=1||table.getItem(4).getCount()!=1)throw new AssertionError("Workstation contents/fuel lost on reload");
            });
            reopened.getServer().runOnServer(server->{
                var old=server.getPlayerList().getPlayers().getFirst();
                var player=server.getPlayerList().respawn(old,false,net.minecraft.world.entity.Entity.RemovalReason.KILLED);
                if(player.getAttachedOrCreate(ModRegistries.SPIRITWEB).reserves().getOrDefault(Metal.IRON,0L)!=Materials.PORTION_MASS)throw new AssertionError("Death/respawn lost reserves");
            });
            context.waitFor(client->client.player!=null && client.player.isAlive() && client.screen==null);
        }
    }
}
