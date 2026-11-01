package dev.metallicarts.domain;
import dev.metallicarts.config.ServerConfig;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class AccountPowersTest {
 @Test void bothVerifiedAccountsHaveEveryStandardArtWithoutMutatingSavedOwnership(){for(var uuid:AccountPowers.ACCOUNTS.keySet()){var s=Spiritweb.empty();for(var art:Art.values())for(var metal:Metal.values())assertEquals(1,AccountPowers.strength(uuid,true,s,art,metal));assertTrue(s.grants().isEmpty());assertTrue(s.reserves().isEmpty());}}
 @Test void unrelatedAndOfflineNameDerivedIdentitiesReceiveNothing(){for(var name:AccountPowers.ACCOUNTS.values()){var offline=UUID.nameUUIDFromBytes(("OfflinePlayer:"+name).getBytes(java.nio.charset.StandardCharsets.UTF_8));assertFalse(AccountPowers.enabled(offline,true));}assertFalse(AccountPowers.enabled(UUID.randomUUID(),true));}
 @Test void disablingPerkReturnsExactlyExistingGrantsAndPreservesSpikeStacking(){var uuid=AccountPowers.ACCOUNTS.keySet().iterator().next();var id=UUID.randomUUID();var spike=new PowerGrant(id,Art.ALLOMANCY,Metal.STEEL,PowerGrant.Origin.HEMALURGIC,id,.9);var s=new Spiritweb(1,UUID.randomUUID(),List.of(spike),Map.of(Metal.STEEL,123L));assertEquals(1.9,AccountPowers.strength(uuid,true,s,Art.ALLOMANCY,Metal.STEEL));assertEquals(.9,AccountPowers.strength(uuid,false,s,Art.ALLOMANCY,Metal.STEEL));assertEquals(0,AccountPowers.strength(uuid,false,s,Art.FERUCHEMY,Metal.GOLD));assertEquals(List.of(spike),s.grants());assertEquals(123,s.reserves().get(Metal.STEEL));}
 @Test void strongerExistingPowersAreNeverWeakenedOrAddedTwice(){var uuid=AccountPowers.ACCOUNTS.keySet().iterator().next();var id=UUID.randomUUID();for(var art:Art.values()){var s=new Spiritweb(1,UUID.randomUUID(),List.of(new PowerGrant(id,art,Metal.GOLD,PowerGrant.Origin.ADMIN,id,3)),Map.of());assertEquals(3,AccountPowers.strength(uuid,true,s,art,Metal.GOLD));}}
 @Test void legacyConfigurationEnablesPerkAndExplicitFalseSurvivesParsing(){assertTrue(ServerConfig.parse("{\"schema\":1,\"allowPlayerHemalurgy\":false,\"snapshotCooldownTicks\":20}").enableAccountPowers());assertFalse(ServerConfig.parse("{\"schema\":1,\"allowPlayerHemalurgy\":true,\"snapshotCooldownTicks\":40,\"enableAccountPowers\":false}").enableAccountPowers());assertThrows(RuntimeException.class,()->ServerConfig.parse("{\"enableAccountPowers\":\"yes\"}"));}
}
