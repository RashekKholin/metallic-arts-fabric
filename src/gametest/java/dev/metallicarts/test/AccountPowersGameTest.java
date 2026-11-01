package dev.metallicarts.test;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.allomancy.*;
import dev.metallicarts.arts.*;
import dev.metallicarts.config.ServerConfig;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.EconomyContent;
import dev.metallicarts.feruchemy.*;
import dev.metallicarts.network.ContactIntent;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.*;

public final class AccountPowersGameTest {
    private static ServerPlayer player(GameTestHelper c,UUID uuid,String label){
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(uuid,label),false);
        var p=new ServerPlayer(c.getLevel().getServer(),c.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        c.getLevel().getServer().getPlayerList().placeNewPlayer(connection,p,cookie);
        p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);return p;
    }
    private static void resetFixture(ServerPlayer p){
        AllomancyRuntime.stop(p);FeruchemyRuntime.stop(p);
        var ledger=MetalmindLedger.get(p.level().getServer());
        // Fixed test UUIDs recur across runs; release only this fixture's previous canonical contacts.
        for(var asset:ledger.all())asset.lease().filter(lease->lease.player().equals(p.getUUID())).ifPresent(lease->ledger.release(asset.token(),lease));
        p.setAttached(ModRegistries.CONTACT_EQUIPMENT,dev.metallicarts.equipment.ContactEquipment.empty());
        p.setAttached(ModRegistries.SPIRITWEB,Spiritweb.empty());p.getInventory().clearContent();
    }
    @GameTest public void verifiedProfilesUseAllPowersWithNormalCostsAndCanBeDisabledCleanly(GameTestHelper c){
        var original=MetallicArts.config;
        try{
            MetallicArts.config=new ServerConfig(1,false,20,true);
            int n=0;
            for(var uuid:AccountPowers.ACCOUNTS.keySet()){
                var p=player(c,uuid,"renamed-perk-"+(n++));resetFixture(p);var before=p.getAttachedOrCreate(ModRegistries.SPIRITWEB);
                for(var art:Art.values())for(var metal:Metal.values())c.assertTrue(EffectivePower.base(p,art,metal)==1,"Verified UUID grants every type independent of label");
                c.assertFalse(AllomancyRuntime.request(p,0,1,100),"Perk cannot burn without actual metal");
                p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.credit(before,Metal.STEEL,10000));
                c.assertTrue(AllomancyRuntime.request(p,0,1,100),"Normal burn permitted by account access");AllomancyRuntime.prepare(p,100);
                c.assertTrue(AllomancyRuntime.power(p,Metal.STEEL)==1&&ReserveLedger.ordinary(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),Metal.STEEL)<10000,"Real fuel paid before effect");
                p.getInventory().setSelectedSlot(0);p.getInventory().setItem(0,new ItemStack(EconomyContent.item("iron","ring")));ContactService.equip(p,0,0);
                c.assertTrue(FeruchemyRuntime.act(p,new ContactIntent(2,0,0,1,1),100),"Contacted Feruchemy uses account access");FeruchemyRuntime.tick(p,101);
                c.assertTrue(FeruchemyRuntime.mass(p)==60,"Storing has normal weight disadvantage");
                var token=ContactService.equipment(p).item(0).get(ModRegistries.METALMIND).token();var ledger=MetalmindLedger.get(c.getLevel().getServer());
                c.assertTrue(ledger.resolve(token).charge()==250,"No free charge from permission");
                var json=AdvancedAllomancy.journalData(p,0);c.assertTrue(json.getAsJsonObject("baseAccess").get("FERUCHEMY:NICROSIL").getAsDouble()==1,"Owner UI metadata includes eligible base access");
                p.getInventory().setItem(0,new ItemStack(EconomyContent.item("nicrosil","ring")));ContactService.equip(p,1,Metal.STEEL.ordinal());
                c.assertTrue(ledger.resolve(ContactService.equipment(p).item(1).get(ModRegistries.METALMIND).token()).content().power().orElseThrow().metal()==Metal.STEEL,"Account access works in typed selection");
                var state=p.getAttachedOrCreate(ModRegistries.SPIRITWEB);c.assertTrue(state.grants().isEmpty()&&state.identity().equals(before.identity()),"No persistent grant or Identity rewrite");
                MetallicArts.config=new ServerConfig(1,false,20,false);AllomancyRuntime.prepare(p,101);FeruchemyRuntime.tick(p,102);
                c.assertTrue(AllomancyRuntime.power(p,Metal.STEEL)==0&&FeruchemyRuntime.mass(p)==80&&ledger.resolve(token).charge()==250,"Policy off stops actions without lost charge");
                c.assertTrue(p.getAttachedOrCreate(ModRegistries.SPIRITWEB).grants().isEmpty(),"Policy removal needs no saved-world surgery");
                resetFixture(p);MetallicArts.config=new ServerConfig(1,false,20,true);
            }
            var unrelated=player(c,UUID.randomUUID(),"RashekKholin");c.assertTrue(EffectivePower.base(unrelated,Art.ALLOMANCY,Metal.STEEL)==0,"Matching name with wrong UUID gets no privilege");
        }finally{MetallicArts.config=original;}
        c.succeed();
    }
}
