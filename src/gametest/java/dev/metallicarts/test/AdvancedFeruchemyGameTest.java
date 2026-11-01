package dev.metallicarts.test;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.allomancy.*;
import dev.metallicarts.arts.*;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.*;
import dev.metallicarts.equipment.*;
import dev.metallicarts.feruchemy.*;
import dev.metallicarts.network.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
import net.minecraft.core.BlockPos;
import java.util.*;
public final class AdvancedFeruchemyGameTest {
 private static ServerPlayer player(GameTestHelper c){
  var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(UUID.randomUUID(),"resource-test"),false);
  var p=new ServerPlayer(c.getLevel().getServer(),c.getLevel(),cookie.gameProfile(),cookie.clientInformation());
  var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(connection);c.getLevel().getServer().getPlayerList().placeNewPlayer(connection,p,cookie);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);while(!p.connection.hasClientLoaded())p.connection.tickClientLoadTimeout();return p;
 }
 private static void grant(ServerPlayer p,Art art,Metal metal){UUID id=UUID.randomUUID();p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.grant(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),new PowerGrant(id,art,metal,PowerGrant.Origin.ADMIN,id,1)));}
 private static void equip(ServerPlayer p,Metal metal,int slot,int type){p.getInventory().setSelectedSlot(0);p.getInventory().setItem(0,new ItemStack(EconomyContent.item(metal.id(),"ring")));ContactService.equip(p,slot,type);}
 private static boolean mode(ServerPlayer p,int slot,int mode,int rate){return FeruchemyRuntime.act(p,new ContactIntent(2,slot,0,mode,rate),100);}
 private static MetalmindAsset asset(ServerPlayer p,int slot){return MetalmindLedger.get(p.level().getServer()).resolve(ContactService.equipment(p).item(slot).get(ModRegistries.METALMIND).token());}
 private static void charge(ServerPlayer p,int slot,long n){var a=asset(p,slot);MetalmindLedger.get(p.level().getServer()).change(a.token(),a.lease().orElseThrow(),p.getAttachedOrCreate(ModRegistries.SPIRITWEB).identity(),n);}
 @GameTest public void healthStorageRequiresActualUncancelledDamage(GameTestHelper c){
  var p=player(c);grant(p,Art.FERUCHEMY,Metal.GOLD);equip(p,Metal.GOLD,0,0);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setHealth(20);c.assertTrue(mode(p,0,1,3),"Store health");FeruchemyRuntime.tick(p,101);
  c.assertTrue(p.getHealth()<20&&asset(p,0).charge()>0&&asset(p,0).charge()<=750,"Real health surrendered before bounded credit health="+p.getHealth()+" charge="+asset(p,0).charge()+" creative="+p.isCreative()+" invul="+p.isInvulnerable());
  long before=asset(p,0).charge();p.setInvulnerable(true);FeruchemyRuntime.tick(p,102);c.assertTrue(asset(p,0).charge()==before,"Prevented damage earns no charge");FeruchemyRuntime.stop(p);c.succeed();
 }
 @GameTest public void healingCannotSpendAtFullHealthAndEmergencyIsPaidOnce(GameTestHelper c){
  var p=player(c);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);grant(p,Art.FERUCHEMY,Metal.GOLD);equip(p,Metal.GOLD,0,0);charge(p,0,500000);p.setHealth(20);c.assertTrue(mode(p,0,2,3),"Arm paid recovery");FeruchemyRuntime.tick(p,101);
  c.assertTrue(asset(p,0).charge()==500000&&AdvancedFeruchemy.canEmergency(p),"Full health keeps emergency armed without wasting charge charge="+asset(p,0).charge()+" access="+EffectivePower.strength(p,Art.FERUCHEMY,Metal.GOLD)+" creative="+p.isCreative()+" rescue="+p.getAttachedOrCreate(ModRegistries.BODY).rescueUntil()+" time="+p.level().getGameTime());
  p.hurtServer(p.level(),p.damageSources().magic(),1000);c.assertTrue(p.isAlive()&&p.getHealth()==4&&asset(p,0).charge()==300000,"Native lethal damage rescued after exact canonical debit");
  c.assertFalse(AdvancedFeruchemy.canEmergency(p),"Cooldown blocks repeat rescue");c.assertFalse(AdvancedFeruchemy.rescue(p,p.damageSources().genericKill()),"Kill bypass is never blocked");FeruchemyRuntime.stop(p);c.succeed();
 }
 @GameTest public void wakefulnessAndWarmthChangePersistentBodyAtRealCost(GameTestHelper c){
  var p=player(c);grant(p,Art.FERUCHEMY,Metal.BRONZE);grant(p,Art.FERUCHEMY,Metal.BRASS);equip(p,Metal.BRONZE,0,0);equip(p,Metal.BRASS,1,0);long fatigue=p.getAttachedOrCreate(ModRegistries.BODY).fatigue(),temp=p.getAttachedOrCreate(ModRegistries.BODY).temperature();
  c.assertTrue(mode(p,0,1,3)&&mode(p,1,1,3),"Store independent body attributes");FeruchemyRuntime.tick(p,101);var body=p.getAttachedOrCreate(ModRegistries.BODY);
  c.assertTrue(body.fatigue()>fatigue&&body.temperature()<temp&&asset(p,0).charge()==750&&asset(p,1).charge()==750,"Stored wakefulness causes fatigue and warmth causes cooling");
  c.assertTrue(p.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(MetallicArts.id("body_fatigue")),"Wakefulness storage has immediate drowsy cost");
  c.assertTrue(mode(p,0,2,1)&&mode(p,1,2,1),"Tap separate resources");FeruchemyRuntime.tick(p,102);c.assertTrue(p.getAttachedOrCreate(ModRegistries.BODY).fatigue()<body.fatigue()&&p.getAttachedOrCreate(ModRegistries.BODY).temperature()>body.temperature(),"Paid rest and warmth restored");FeruchemyRuntime.stop(p);c.succeed();
 }
 @GameTest public void airAndNutritionCannotBeStoredFromAbsentResources(GameTestHelper c){
  var p=player(c);grant(p,Art.FERUCHEMY,Metal.CADMIUM);grant(p,Art.FERUCHEMY,Metal.BENDALLOY);equip(p,Metal.CADMIUM,0,0);equip(p,Metal.BENDALLOY,1,0);p.setAirSupply(10);p.getFoodData().setFoodLevel(1);p.getFoodData().setSaturation(0);
  c.assertTrue(mode(p,0,1,3)&&mode(p,1,1,3),"Requests checked again at payment");FeruchemyRuntime.tick(p,101);c.assertTrue(asset(p,0).charge()==0&&asset(p,1).charge()==0,"No air or nutrition means no minted charge");
  p.setAirSupply(300);p.getFoodData().setFoodLevel(20);p.getFoodData().setSaturation(5);mode(p,0,1,3);mode(p,1,1,3);FeruchemyRuntime.tick(p,102);
  c.assertTrue(p.getAirSupply()==299&&p.getAttachedOrCreate(ModRegistries.BODY).air()==250&&asset(p,0).charge()==750,"Breath debit retains fractional escrow");c.assertTrue(p.getFoodData().getSaturationLevel()<5&&asset(p,1).charge()==750,"Actual saturation is surrendered");FeruchemyRuntime.stop(p);c.succeed();
 }
 @GameTest public void fullIdentityAllowsUnkeyedFillingButPartialSuppressionDoesNotEraseKeys(GameTestHelper c){
  var p=player(c);grant(p,Art.FERUCHEMY,Metal.ALUMINUM);grant(p,Art.FERUCHEMY,Metal.IRON);equip(p,Metal.ALUMINUM,0,0);equip(p,Metal.IRON,1,0);
  c.assertTrue(mode(p,0,1,4)&&mode(p,1,1,1),"Full Identity storage supported");FeruchemyRuntime.tick(p,101);c.assertTrue(asset(p,0).unkeyed()&&asset(p,1).unkeyed(),"Paid full suppression fills both empty pools unkeyed");
  c.assertTrue(mode(p,0,0,1),"Stop Identity");long before=asset(p,1).charge();FeruchemyRuntime.tick(p,102);c.assertTrue(asset(p,1).charge()==before,"Normal Identity cannot contaminate unkeyed charge");
  var other=player(c);grant(other,Art.FERUCHEMY,Metal.ALUMINUM);equip(other,Metal.ALUMINUM,0,0);mode(other,0,1,3);FeruchemyRuntime.tick(other,103);c.assertTrue(!asset(other,0).unkeyed()&&asset(other,0).identity().isPresent(),"75 percent Identity remains keyed");FeruchemyRuntime.stop(p);FeruchemyRuntime.stop(other);c.succeed();
 }
 @GameTest public void typedInvestitureSurrendersOnlySelectedAbilityAndStopsCleanly(GameTestHelper c){
  var p=player(c);grant(p,Art.FERUCHEMY,Metal.NICROSIL);grant(p,Art.ALLOMANCY,Metal.STEEL);grant(p,Art.ALLOMANCY,Metal.IRON);equip(p,Metal.NICROSIL,0,Metal.STEEL.ordinal());mode(p,0,1,3);FeruchemyRuntime.tick(p,101);
  c.assertTrue(EffectivePower.strength(p,Art.ALLOMANCY,Metal.STEEL)==.25&&EffectivePower.strength(p,Art.ALLOMANCY,Metal.IRON)==1,"Specific access surrendered, unrelated grant intact");
  mode(p,0,2,1);FeruchemyRuntime.tick(p,102);c.assertTrue(EffectivePower.strength(p,Art.ALLOMANCY,Metal.STEEL)==1.25&&asset(p,0).charge()==500,"Typed paid access restored temporarily");FeruchemyRuntime.stop(p);c.assertTrue(EffectivePower.strength(p,Art.ALLOMANCY,Metal.STEEL)==1,"No persistent grant minted");c.succeed();
 }
 @GameTest public void coppermindMovesOneActualRecordAndRefusesReplayOrForeignAccess(GameTestHelper c){
  var p=player(c);grant(p,Art.FERUCHEMY,Metal.COPPER);equip(p,Metal.COPPER,0,0);var memory=new MemoryEntry(UUID.randomUUID(),"route","Observed test route","minecraft:overworld",1,80,2,1);p.setAttached(ModRegistries.JOURNAL,new ArtsJournal(List.of(memory),List.of(memory),List.of("test")));
  c.assertTrue(AdvancedFeruchemy.memory(p,new MemoryAction(1,0,0,memory.id())),"Store owned actual memory");c.assertTrue(p.getAttachedOrCreate(ModRegistries.JOURNAL).memories().isEmpty()&&p.getAttachedOrCreate(ModRegistries.JOURNAL).history().isEmpty()&&asset(p,0).charge()==1000,"Access actually moves out of journal/history");
  c.assertFalse(AdvancedFeruchemy.memory(p,new MemoryAction(1,0,0,memory.id())),"Replay cannot duplicate memory");c.assertTrue(AdvancedFeruchemy.memory(p,new MemoryAction(2,0,0,memory.id())),"Restore exact record");c.assertTrue(p.getAttachedOrCreate(ModRegistries.JOURNAL).memories().equals(List.of(memory))&&asset(p,0).charge()==0,"No generic charge conversion or invented record");FeruchemyRuntime.stop(p);c.succeed();
 }
 @GameTest public void fortuneCognitionAndDeterminationHaveDistinctReversibleEffects(GameTestHelper c){
  var p=player(c);for(var m:List.of(Metal.CHROMIUM,Metal.ZINC,Metal.ELECTRUM))grant(p,Art.FERUCHEMY,m);equip(p,Metal.CHROMIUM,0,0);equip(p,Metal.ZINC,1,0);equip(p,Metal.ELECTRUM,2,0);mode(p,0,1,3);mode(p,1,1,3);mode(p,2,1,3);FeruchemyRuntime.tick(p,101);
  c.assertTrue(p.getAttribute(Attributes.LUCK).getValue()<0&&p.getAttribute(Attributes.ATTACK_SPEED).hasModifier(MetallicArts.id("mental_actions"))&&p.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(MetallicArts.id("determination")),"Three different meaningful costs");
  FeruchemyRuntime.stop(p);c.assertTrue(!p.getAttribute(Attributes.LUCK).hasModifier(MetallicArts.id("fortune"))&&!p.getAttribute(Attributes.ATTACK_SPEED).hasModifier(MetallicArts.id("mental_actions")),"Cleanup removes only owned modifiers");c.succeed();
 }
 @GameTest public void connectionTradeChangesPreserveAndRestoreVanillaPrices(GameTestHelper c){
  var p=player(c);grant(p,Art.FERUCHEMY,Metal.DURALUMIN);equip(p,Metal.DURALUMIN,0,0);charge(p,0,10000);mode(p,0,2,3);FeruchemyRuntime.tick(p,101);
  var v=c.spawn(EntityType.VILLAGER,new BlockPos(2,1,2));var offers=new MerchantOffers();var offer=new MerchantOffer(new ItemCost(Items.EMERALD,20),new ItemStack(Items.BREAD),10,1,.05f);offer.addToSpecialPriceDiff(-2);offers.add(offer);v.getOffers().clear();v.getOffers().add(offer);SocialConnections.apply(v,p);c.assertTrue(offer.getSpecialPriceDiff()==-4,"Paid Connection adds bounded discount without deleting prior reputation price="+offer.getSpecialPriceDiff()+" effect="+AdvancedFeruchemy.effect(p,FeruchemicalAttribute.CONNECTION));SocialConnections.stop(p);c.assertTrue(offer.getSpecialPriceDiff()==-2,"Removing Connection restores exact vanilla delta");FeruchemyRuntime.stop(p);c.succeed();
 }
 @GameTest public void unsealedConstructionConsumesBinderAndOrdinaryRecipientSpendsForSpecificAccess(GameTestHelper c){
  var p=player(c);for(var m:List.of(Metal.ALUMINUM,Metal.DURALUMIN,Metal.NICROSIL,Metal.IRON))grant(p,Art.FERUCHEMY,m);equip(p,Metal.ALUMINUM,0,0);equip(p,Metal.DURALUMIN,1,0);equip(p,Metal.NICROSIL,2,16+Metal.IRON.ordinal());charge(p,1,5000);mode(p,0,1,4);mode(p,1,2,3);mode(p,2,1,3);FeruchemyRuntime.tick(p,101);
  c.assertTrue(asset(p,2).unkeyed()&&asset(p,2).content().power().orElseThrow().metal()==Metal.IRON,"Typed nicrosil paid while Identity blank");
  ContactService.unequip(p,2);p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(EconomyContent.item("duralumin","ring")));c.assertTrue(AdvancedFeruchemy.unsealHeld(p)&&p.getOffhandItem().isEmpty(),"Explicit construction consumes real binder");
  var other=player(c);other.getInventory().setSelectedSlot(0);other.getInventory().setItem(0,p.getMainHandItem());p.getInventory().setItem(0,ItemStack.EMPTY);ContactService.equip(other,0,0);c.assertTrue(mode(other,0,2,1),"Ordinary recipient can tap deliberately unsealed source");FeruchemyRuntime.tick(other,102);
  c.assertTrue(EffectivePower.strength(other,Art.FERUCHEMY,Metal.IRON)==.25&&other.getAttachedOrCreate(ModRegistries.SPIRITWEB).grants().isEmpty()&&asset(other,0).charge()==500,"Access is paid and temporary, never a permanent raw grant");
  var ledger=MetalmindLedger.get(c.getLevel().getServer());var lease=new AssetLease(other.getUUID(),1);var weight=ledger.claim(ledger.create(FeruchemicalAttribute.WEIGHT,9000000,1,true).token(),lease);weight=ledger.change(weight.token(),lease,UUID.randomUUID(),1000);var item=new ItemStack(EconomyContent.item("iron","ring"));item.set(ModRegistries.METALMIND,MetalmindData.of(weight));other.setAttached(ModRegistries.CONTACT_EQUIPMENT,ContactService.equipment(other).with(1,item));c.assertTrue(mode(other,1,2,1),"Paid temporary ability authorizes a separate unkeyed attribute mind");FeruchemyRuntime.tick(other,103);c.assertTrue(FeruchemyRuntime.mass(other)==100&&asset(other,1).charge()==750,"Ordinary recipient really taps separately paid weight");mode(other,0,0,1);c.assertTrue(EffectivePower.strength(other,Art.FERUCHEMY,Metal.IRON)==0,"Stopping typed source revokes access immediately");FeruchemyRuntime.tick(other,104);c.assertTrue(asset(other,1).charge()==750&&FeruchemyRuntime.mass(other)==80,"Dependent tapping stops with no free charge or persistent access");FeruchemyRuntime.stop(p);FeruchemyRuntime.stop(other);c.succeed();
 }
}
