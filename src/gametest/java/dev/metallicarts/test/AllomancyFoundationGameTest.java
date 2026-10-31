package dev.metallicarts.test;

import dev.metallicarts.domain.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.permissions.PermissionSet;
import java.util.*;

public final class AllomancyFoundationGameTest {
    @GameTest public void operatorCommandsPreserveOwnershipAndFuel(GameTestHelper c) throws Exception {
        var player=c.makeMockServerPlayerInLevel();
        var original=player.getAttachedOrCreate(ModRegistries.SPIRITWEB);
        var server=c.getLevel().getServer();
        var dispatcher=server.getCommands().getDispatcher();
        var source=server.createCommandSourceStack().withEntity(player).withSuppressedOutput();
        c.assertTrue(dispatcher.execute("metallicarts grant @s steel 2",source)==1,"Operator can grant");
        var granted=player.getAttachedOrCreate(ModRegistries.SPIRITWEB);
        c.assertTrue(granted.identity().equals(original.identity())&&granted.grants().size()==1,"Identity retained");
        var grant=granted.grants().getFirst();
        c.assertTrue(grant.origin()==PowerGrant.Origin.ADMIN&&grant.strength()==2,"Explicit administrative provenance");
        dispatcher.execute("metallicarts fuel @s steel 10000",source);
        c.assertTrue(player.getAttachedOrCreate(ModRegistries.SPIRITWEB).reserves().get(Metal.STEEL)==10000,"Fuel credited");
        dispatcher.execute("metallicarts revoke @s "+grant.id(),source);
        var after=player.getAttachedOrCreate(ModRegistries.SPIRITWEB);
        c.assertTrue(after.grants().isEmpty()&&after.reserves().get(Metal.STEEL)==10000,"Revocation retains fuel");
        c.succeed();
    }
    @GameTest public void ordinaryPlayersCannotUseAdministrativeCommands(GameTestHelper c) throws Exception {
        var player=c.makeMockServerPlayerInLevel();
        var original=player.getAttachedOrCreate(ModRegistries.SPIRITWEB);
        var dispatcher=c.getLevel().getServer().getCommands().getDispatcher();
        var source=player.createCommandSourceStack().withPermission(PermissionSet.NO_PERMISSIONS).withSuppressedOutput();
        c.assertFalse(dispatcher.getRoot().getChild("metallicarts").canUse(source),"Root rejects non-operators");
        for(String command:List.of("grant @s steel 1","fuel @s steel 10000","inspect @s","revoke @s "+UUID.randomUUID())) {
            boolean refused=false;
            try { dispatcher.execute("metallicarts "+command,source); }
            catch(com.mojang.brigadier.exceptions.CommandSyntaxException expected){refused=true;}
            c.assertTrue(refused,"Command requires operator: "+command);
        }
        c.assertValueEqual(original,player.getAttachedOrCreate(ModRegistries.SPIRITWEB),"Unauthorized requests leave attachment unchanged");
        c.succeed();
    }
    @GameTest public void malformedCommandsDoNotPartiallyMutate(GameTestHelper c) throws Exception {
        var player=c.makeMockServerPlayerInLevel();
        var before=player.getAttachedOrCreate(ModRegistries.SPIRITWEB);
        var server=c.getLevel().getServer();
        var source=server.createCommandSourceStack().withEntity(player).withSuppressedOutput();
        for(String command:List.of("grant @s atium 1","grant @s steel 0","revoke @s bad-uuid","fuel @s iron -1","fuel @s steel 1000000001")) {
            boolean refused=false;
            try {server.getCommands().getDispatcher().execute("metallicarts "+command,source);}
            catch(com.mojang.brigadier.exceptions.CommandSyntaxException expected){refused=true;}
            c.assertTrue(refused,"Malformed command refused: "+command);
            c.assertValueEqual(before,player.getAttachedOrCreate(ModRegistries.SPIRITWEB),"No partial mutation");
        }
        c.succeed();
    }
}
