package dev.metallicarts.arts;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.api.ArtsProtection;
import dev.metallicarts.config.AdvancedConfig;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.EconomyContent;
import dev.metallicarts.equipment.AssetLease;
import dev.metallicarts.feruchemy.*;
import dev.metallicarts.hemalurgy.HemalurgyRuntime;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;

/** Explicit whole-item ingestion/burn. Legacy preview charge never authorizes this transaction. */
public final class Compounding {
 public static void burn(ServerPlayer p,int slot){
  if(!dev.metallicarts.hemalurgy.ConstructRuntime.canUsePowers(p)||!AdvancedConfig.get().compounding().enabled()||!ArtsProtection.allows(p,p,ArtsProtection.Action.COMPOUND))throw new IllegalArgumentException("Compounding disabled or protected");
  if(slot<0||slot>=12)throw new IllegalArgumentException("Invalid destination slot");
  var held=p.getMainHandItem();var reference=held.get(ModRegistries.METALMIND);var form=EconomyContent.form(held.getItem());
  if(held.getCount()!=1||reference==null||held.has(ModRegistries.SPIKE)||form==null)throw new IllegalArgumentException("Hold one free canonical metalmind; previews/flakes/vials cannot mint output");
  var ledger=MetalmindLedger.get(p.level().getServer());var source=ledger.resolve(reference.token());
  if(!form.material().equals(source.metal().id())||!java.util.List.of("ring","bracelet","bracer","earring").contains(form.kind())||source.lease().isPresent()||source.charge()==0||EffectivePower.strength(p,Art.ALLOMANCY,source.metal())<=0||EffectivePower.strength(p,Art.FERUCHEMY,source.metal())<=0)throw new IllegalArgumentException("Need both Arts, free charged material and valid composition");
  UUID sourceKey=key(p,source);
  if(source.attribute()==FeruchemicalAttribute.MEMORY){
   var journal=p.getAttachedOrCreate(ModRegistries.JOURNAL);for(var memory:source.content().memories())journal=journal.remember(memory);
   ledger.burnMemories(source.token(),sourceKey);p.setAttached(ModRegistries.JOURNAL,journal);
  }else{
   var target=ContactService.equipment(p).item(slot).get(ModRegistries.METALMIND);if(target==null)throw new IllegalArgumentException("Choose a contacted destination mind");
   var destination=ledger.owned(target.token(),new AssetLease(p.getUUID(),slot));
   ledger.compound(source.token(),destination.token(),destination.lease().orElseThrow(),sourceKey,key(p,destination),AdvancedFeruchemy.effect(p,FeruchemicalAttribute.IDENTITY)<=-1,AdvancedConfig.get().compounding().multiplier());
  }
  held.shrink(1);
  MetallicArts.LOGGER.info("{} compounded canonical {} ({}) into contact slot {}",p.getUUID(),source.token().instance(),source.attribute(),slot);
  p.sendOverlayMessage(net.minecraft.network.chat.Component.literal(source.attribute()==FeruchemicalAttribute.MEMORY?"Copper burn restored exact memories once; knowledge is not duplicated":"Compounded charge deposited; the entire source metalmind was consumed"));
 }
 private static UUID key(ServerPlayer p,MetalmindAsset asset){var identity=p.getAttachedOrCreate(ModRegistries.SPIRITWEB).identity();if(asset.charge()==0||asset.accessible(identity))return identity;return asset.identity().filter(HemalurgyRuntime.donorKeys(p,asset.metal())::contains).orElseThrow(()->new IllegalArgumentException("Foreign source or destination Identity"));}
 private Compounding(){}
}
