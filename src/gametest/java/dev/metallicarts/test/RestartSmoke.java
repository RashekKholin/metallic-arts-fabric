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
                    var spikeLedger=dev.metallicarts.hemalurgy.SpikeLedger.get(server);
                    var spike=spikeLedger.create(Metal.STEEL,new dev.metallicarts.hemalurgy.StolenPower(dev.metallicarts.hemalurgy.StolenPower.Kind.ALLOMANCY,Metal.STEEL),GRANT,ITEM,UUID.fromString("f8105a93-0d95-4885-9ce8-c9a38165295b"),1,server.overworld().getGameTime());
                    spike=spikeLedger.claim(spike.token(),new dev.metallicarts.equipment.AssetLease(PLAYER,4),server.overworld().getGameTime());
                    var spikeItem=new ItemStack(EconomyContent.item("steel","spike"));spikeItem.set(ModRegistries.SPIKE,dev.metallicarts.hemalurgy.SpikeData.of(spike));
                    player.setAttached(ModRegistries.SPIKES,dev.metallicarts.equipment.ContactEquipment.empty().with(4,spikeItem));
                    player.setAttached(ModRegistries.BODY,new dev.metallicarts.arts.BodyState(1234,35500000,5678,250,10,900));
                    var memory=new dev.metallicarts.arts.MemoryEntry(ITEM,"route","Restart memory","minecraft:overworld",1,80,2,5);
                    player.setAttached(ModRegistries.JOURNAL,new dev.metallicarts.arts.ArtsJournal(List.of(),List.of(),List.of("restart")));
                    player.setAttached(ModRegistries.CONNECTIONS,new dev.metallicarts.arts.ConnectionState(Map.of("minecraft:overworld:0:0",120)));
                    var memoryLease=new dev.metallicarts.equipment.AssetLease(PLAYER,1);var copper=ledger.claim(ledger.create(dev.metallicarts.feruchemy.FeruchemicalAttribute.MEMORY,9000000,1,false).token(),memoryLease);copper=ledger.storeMemory(copper.token(),memoryLease,IDENTITY,memory,false);
                    var copperItem=new ItemStack(EconomyContent.item("copper","ring"));copperItem.set(ModRegistries.METALMIND,dev.metallicarts.feruchemy.MetalmindData.of(copper));player.setAttached(ModRegistries.CONTACT_EQUIPMENT,player.getAttachedOrCreate(ModRegistries.CONTACT_EQUIPMENT).with(1,copperItem));
                    var typed=new dev.metallicarts.feruchemy.MetalmindContent(Optional.of(new dev.metallicarts.feruchemy.StoredPower(Art.FERUCHEMY,Metal.GOLD,1)),List.of(),false);var nLease=new dev.metallicarts.equipment.AssetLease(PLAYER,2);var nicrosil=ledger.claim(ledger.create(dev.metallicarts.feruchemy.FeruchemicalAttribute.INVESTITURE,9000000,1,false,typed).token(),nLease);nicrosil=ledger.change(nicrosil.token(),nLease,IDENTITY,54321,true);nicrosil=ledger.release(nicrosil.token(),nLease);nicrosil=ledger.unseal(nicrosil.token());var nItem=new ItemStack(EconomyContent.item("nicrosil","ring"));nItem.set(ModRegistries.METALMIND,dev.metallicarts.feruchemy.MetalmindData.of(nicrosil));player.getInventory().setItem(1,nItem);
                    player.setAttached(ModRegistries.PROGRESSION,new dev.metallicarts.arts.ProgressionState(true,Optional.of(Metal.IRON),Optional.of(Metal.GOLD),true,true));
                    player.setAttached(ModRegistries.LERASIUM_CLAIMED,true);
                    player.setAttached(ModRegistries.TRAUMA,new dev.metallicarts.arts.TraumaState(1,2,1));
                    player.setAttached(ModRegistries.RELATIONSHIPS,new dev.metallicarts.arts.Relationships(Map.of(ITEM,42)));
                    player.setAttached(ModRegistries.CONSTRUCT,new dev.metallicarts.hemalurgy.ConstructState(dev.metallicarts.hemalurgy.ConstructState.Lineage.MISTWRAITH,dev.metallicarts.hemalurgy.ConstructState.Kind.KANDRA,dev.metallicarts.hemalurgy.ConstructState.Shape.WOLF,1234,5,3));
                    var outLease=new dev.metallicarts.equipment.AssetLease(PLAYER,2);
                    var output=ledger.claim(ledger.create(dev.metallicarts.feruchemy.FeruchemicalAttribute.HEALTH,9000000,1,false).token(),outLease);
                    var sourceLease=new dev.metallicarts.equipment.AssetLease(PLAYER,3);
                    var input=ledger.claim(ledger.create(dev.metallicarts.feruchemy.FeruchemicalAttribute.HEALTH,9000000,1,false).token(),sourceLease);
                    input=ledger.change(input.token(),sourceLease,IDENTITY,1000);input=ledger.release(input.token(),sourceLease);
                    var spentCopy=new ItemStack(EconomyContent.item("gold","ring"));spentCopy.set(ModRegistries.METALMIND,dev.metallicarts.feruchemy.MetalmindData.of(input));player.getInventory().setItem(2,spentCopy);
                    output=ledger.compound(input.token(),output.token(),outLease,IDENTITY,IDENTITY,false,10);
                    var outputItem=new ItemStack(EconomyContent.item("gold","ring"));outputItem.set(ModRegistries.METALMIND,dev.metallicarts.feruchemy.MetalmindData.of(output));player.setAttached(ModRegistries.CONTACT_EQUIPMENT,player.getAttachedOrCreate(ModRegistries.CONTACT_EQUIPMENT).with(2,outputItem));
                    var pairLedger=dev.metallicarts.hemalurgy.ConstructLedger.get(server);
                    var first=spikeLedger.create(Metal.IRON,new dev.metallicarts.hemalurgy.StolenPower(dev.metallicarts.hemalurgy.StolenPower.Kind.STRENGTH,Metal.IRON),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1,server.overworld().getGameTime());
                    var second=spikeLedger.create(Metal.IRON,new dev.metallicarts.hemalurgy.StolenPower(dev.metallicarts.hemalurgy.StolenPower.Kind.STRENGTH,Metal.IRON),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1,server.overworld().getGameTime());pairLedger.bless(first,second);
                    int index=4;for(var member:List.of(first,second)){var stack=new ItemStack(EconomyContent.item("iron","spike"));stack.set(ModRegistries.SPIKE,dev.metallicarts.hemalurgy.SpikeData.of(member));player.getInventory().setItem(index++,stack);}
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
                    if(!player.getAttachedOrCreate(ModRegistries.PROGRESSION).snapped()||!player.getAttachedOrCreate(ModRegistries.LERASIUM_CLAIMED)||player.getAttachedOrCreate(ModRegistries.TRAUMA).total()!=4||player.getAttachedOrCreate(ModRegistries.RELATIONSHIPS).merchants().get(ITEM)!=42||player.getAttachedOrCreate(ModRegistries.CONSTRUCT).shape()!=dev.metallicarts.hemalurgy.ConstructState.Shape.WOLF)throw new AssertionError("Restart lost progression/body/trauma/relationship state");
                    var checkLedger=dev.metallicarts.feruchemy.MetalmindLedger.get(server);var spent=player.getInventory().getItem(2).get(ModRegistries.METALMIND);if(!checkLedger.find(spent.token().instance()).orElseThrow().consumed())throw new AssertionError("Consumed source resurrected");boolean rejected=false;try{checkLedger.resolve(spent.token());}catch(IllegalArgumentException e){rejected=true;}if(!rejected)throw new AssertionError("Copied spent source became spendable after restart");
                    var output=player.getAttachedOrCreate(ModRegistries.CONTACT_EQUIPMENT).item(2).get(ModRegistries.METALMIND);if(checkLedger.owned(output.token(),new dev.metallicarts.equipment.AssetLease(PLAYER,2)).charge()!=10000)throw new AssertionError("Compounded charge changed over restart");
                    var preparationLedger=dev.metallicarts.hemalurgy.ConstructLedger.get(server);var first=preparationLedger.find(player.getInventory().getItem(4).get(ModRegistries.SPIKE).token().instance()).orElseThrow();var second=preparationLedger.find(player.getInventory().getItem(5).get(ModRegistries.SPIKE).token().instance()).orElseThrow();if(!first.pair().equals(second.pair()))throw new AssertionError("Blessing pairing lost over restart");
                    if(!expected().equals(player.getAttached(ModRegistries.SPIRITWEB)))throw new AssertionError("Restart lost spiritweb");
                    if(!itemData().equals(player.getInventory().getItem(0).get(ModRegistries.METAL_DATA)))throw new AssertionError("Restart lost item data");
                    if(!new dev.metallicarts.arts.BodyState(1234,35500000,5678,250,10,900).equals(player.getAttached(ModRegistries.BODY)))throw new AssertionError("Restart lost body resource escrow");
                    if(!player.getAttachedOrCreate(ModRegistries.JOURNAL).memories().isEmpty()||!player.getAttachedOrCreate(ModRegistries.JOURNAL).discoveries().equals(List.of("restart")))throw new AssertionError("Stored memory leaked back into journal");
                    if(player.getAttachedOrCreate(ModRegistries.CONNECTIONS).regions().get("minecraft:overworld:0:0")!=120)throw new AssertionError("Restart lost actual region familiarity");
                    var newLedger=dev.metallicarts.feruchemy.MetalmindLedger.get(server);var cp=player.getAttachedOrCreate(ModRegistries.CONTACT_EQUIPMENT).item(1).get(ModRegistries.METALMIND);var ca=newLedger.owned(cp.token(),new dev.metallicarts.equipment.AssetLease(PLAYER,1));if(ca.charge()!=1000||!ca.content().memories().getFirst().id().equals(ITEM)||!ca.content().memories().getFirst().label().equals("Restart memory"))throw new AssertionError("Restart lost unique stored memory");
                    var nd=player.getInventory().getItem(1).get(ModRegistries.METALMIND);var na=newLedger.resolve(nd.token());if(na.charge()!=54321||!na.content().unsealed()||na.content().power().orElseThrow().metal()!=Metal.GOLD||!na.unkeyed())throw new AssertionError("Restart lost typed unsealed Investiture");
                    var mind=player.getAttachedOrCreate(ModRegistries.CONTACT_EQUIPMENT).item(0).get(ModRegistries.METALMIND);
                    if(mind==null)throw new AssertionError("Restart lost contact item");
                    var asset=dev.metallicarts.feruchemy.MetalmindLedger.get(server).owned(mind.token(),new dev.metallicarts.equipment.AssetLease(PLAYER,0));
                    if(asset.charge()!=123456||!asset.identity().orElseThrow().equals(IDENTITY)||asset.capacity()!=9000000)
                        throw new AssertionError("Restart lost canonical metalmind data");
                    var spikeData=player.getAttachedOrCreate(ModRegistries.SPIKES).item(4).get(ModRegistries.SPIKE);
                    if(spikeData==null)throw new AssertionError("Restart lost physical implant");
                    var spikeLedger=dev.metallicarts.hemalurgy.SpikeLedger.get(server);
                    var spike=spikeLedger.owned(spikeData.token(),new dev.metallicarts.equipment.AssetLease(PLAYER,4));
                    if(spike.potency()!=.9||!spike.donorIdentity().equals(ITEM)||!spikeLedger.extracted(spike.donorSource()))throw new AssertionError("Restart lost canonical spike provenance/charge");
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

