package dev.metallicarts.test;

import com.mojang.authlib.GameProfile;
import dev.metallicarts.test.mixin.ServerStorageAccess;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.*;
import dev.metallicarts.metallurgy.MetallurgyBlockEntity;
import dev.metallicarts.metallurgy.MetallurgyRecipes;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.*;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.*;
import java.util.*;
import java.nio.file.*;

/** Test mod only. Runs once per dedicated JVM, saves through real player/world storage, then exits. */
public final class RestartSmoke implements ModInitializer {
    private static final UUID PLAYER=UUID.fromString("583fd912-7edf-4d4a-b767-5b46928dfc53");
    private static final UUID IDENTITY=UUID.fromString("f77bfbfa-50b7-473d-97c9-6ce9b357e1df");
    private static final UUID GRANT=UUID.fromString("3033d47b-f3a3-4e52-8a41-2c5f1d9add23");
    private static final UUID ITEM=UUID.fromString("9aa9dc22-1b3a-43be-bb48-6b735c47a31e");
    private static Spiritweb expected() { return new Spiritweb(1,IDENTITY,List.of(new PowerGrant(GRANT,Art.ALLOMANCY,Metal.IRON,PowerGrant.Origin.INNATE,IDENTITY,1)),Map.of(Metal.IRON,23456L)); }
    private static MetalItemData itemData() { return new MetalItemData(1,ITEM,Metal.GOLD,10000,1,27,Optional.of(IDENTITY)); }
    @Override public void onInitialize() {
        String mode=System.getProperty("metallicarts.restartSmoke");
        if(mode==null)return;
        ServerLifecycleEvents.SERVER_STARTED.register(server->{
            try {
                var player=new ServerPlayer(server,server.overworld(),new GameProfile(PLAYER,"StorageSmoke"),ClientInformation.createDefault());
                BlockPos pos=new BlockPos(0,80,0);server.overworld().getChunkAt(pos);
                if(mode.equals("seed")){
                    player.setAttached(ModRegistries.SPIRITWEB,expected());
                    ItemStack vial=new ItemStack(EconomyContent.item("gold","vial"));vial.set(ModRegistries.METAL_DATA,itemData());
                    player.getInventory().setItem(0,vial);
                    var ledger=dev.metallicarts.feruchemy.MetalmindLedger.get(server);
                    var lease=new dev.metallicarts.equipment.AssetLease(PLAYER,0);
                    // A fresh test world is used for this fixture; charge belongs to SavedData, not the preview.
                    var asset=ledger.create(dev.metallicarts.feruchemy.FeruchemicalAttribute.WEIGHT,9000000,1,false);
                    asset=ledger.claim(asset.token(),lease);asset=ledger.change(asset.token(),lease,IDENTITY,123456);
                    var mind=new ItemStack(EconomyContent.item("iron","ring"));
                    mind.set(ModRegistries.METALMIND,dev.metallicarts.feruchemy.MetalmindData.of(asset));
                    player.setAttached(ModRegistries.CONTACT_EQUIPMENT,dev.metallicarts.equipment.ContactEquipment.empty().with(0,mind));
                    ((ServerStorageAccess)server).metallicarts$storage().save(player);
                    server.overworld().setBlock(pos,Blocks.AIR.defaultBlockState(),3);
                    server.overworld().setBlock(pos,EconomyContent.TABLE.defaultBlockState(),3);
                    var table=(MetallurgyBlockEntity)server.overworld().getBlockEntity(pos);
                    table.setItem(0,new ItemStack(Items.IRON_NUGGET,3));table.tab=2;
                    var options=MetallurgyRecipes.candidates(2,table.inputs());
                    for(int n=0;n<options.size();n++)if(options.get(n).id().equals("iron_nugget_to_portions"))table.selection=n;
                    table.burn=322;table.running=true;MetallurgyBlockEntity.tick(server.overworld(),pos,table.getBlockState(),table);
                    table.progress=12;table.batch=true;table.setChanged();
                    BlockPos brewPos=pos.offset(2,0,0);server.overworld().setBlock(brewPos,Blocks.AIR.defaultBlockState(),3);server.overworld().setBlock(brewPos,Blocks.BREWING_STAND.defaultBlockState(),3);
                    var stand=(BrewingStandBlockEntity)server.overworld().getBlockEntity(brewPos);
                    for(int n=0;n<3;n++){var water=new ItemStack(Items.POTION);water.set(DataComponents.POTION_CONTENTS,new PotionContents(Potions.WATER));stand.setItem(n,water);}
                    stand.setItem(3,new ItemStack(EconomyContent.item("gold","flakes"),5));stand.setItem(4,new ItemStack(Items.BLAZE_POWDER));
                    for(int n=0;n<=100;n++)BrewingStandBlockEntity.serverTick(server.overworld(),brewPos,stand.getBlockState(),stand);
                    server.saveEverything(false,true,true);
                }else if(mode.equals("verify")){
                    var tag=server.getPlayerList().loadPlayerData(new NameAndId(PLAYER,"StorageSmoke")).orElseThrow(()->new AssertionError("No player save after restart"));
                    player.load(TagValueInput.create(ProblemReporter.DISCARDING,server.registryAccess(),tag));
                    if(!expected().equals(player.getAttached(ModRegistries.SPIRITWEB)))throw new AssertionError("Restart lost spiritweb");
                    if(!itemData().equals(player.getInventory().getItem(0).get(ModRegistries.METAL_DATA)))throw new AssertionError("Restart lost item data");
                    var mind=player.getAttachedOrCreate(ModRegistries.CONTACT_EQUIPMENT).item(0).get(ModRegistries.METALMIND);
                    if(mind==null)throw new AssertionError("Restart lost contact item");
                    var asset=dev.metallicarts.feruchemy.MetalmindLedger.get(server).owned(mind.token(),new dev.metallicarts.equipment.AssetLease(PLAYER,0));
                    if(asset.charge()!=123456||!asset.identity().orElseThrow().equals(IDENTITY)||asset.capacity()!=9000000)
                        throw new AssertionError("Restart lost canonical metalmind data");
                    var table=(MetallurgyBlockEntity)server.overworld().getBlockEntity(pos);
                    if(table==null||table.getItem(0).getCount()!=3||table.burn!=321||table.progress!=12||!table.batch||!table.running)throw new AssertionError("Restart lost workstation data");
                    for(int n=0;n<28;n++)MetallurgyBlockEntity.tick(server.overworld(),pos,table.getBlockState(),table);
                    table.running=false;
                    if(table.getItem(0).getCount()!=2||table.getItem(5).getCount()+table.getItem(6).getCount()!=100)throw new AssertionError("Restarted job did not finish exactly once");
                    var brewPos=pos.offset(2,0,0);var stand=(BrewingStandBlockEntity)server.overworld().getBlockEntity(brewPos);
                    if(stand==null||stand.saveWithoutMetadata(server.registryAccess()).getShortOr("BrewTime",(short)0)!=300)throw new AssertionError("Brew timer lost on restart");
                    for(int n=0;n<300;n++)BrewingStandBlockEntity.serverTick(server.overworld(),brewPos,stand.getBlockState(),stand);
                    if(stand.getItem(3).getCount()!=2||!stand.getItem(0).is(EconomyContent.item("gold","vial"))||!stand.getItem(2).is(EconomyContent.item("gold","vial")))throw new AssertionError("Restarted brew duplicated or lost metal");
                }else throw new IllegalArgumentException("Unknown smoke mode");
                Files.writeString(Path.of("restart-"+mode+".result"),"PASS: "+mode+" dedicated process storage");
            }catch(Throwable failure){
                try{Files.writeString(Path.of("restart-"+mode+".result"),"FAIL: "+failure);}catch(Exception ignored){}
                throw new RuntimeException(failure);
            }finally{server.halt(false);}
        });
    }
}
