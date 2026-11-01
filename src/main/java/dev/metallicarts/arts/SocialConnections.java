package dev.metallicarts.arts;
import dev.metallicarts.feruchemy.FeruchemicalAttribute;
import dev.metallicarts.hemalurgy.*;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import java.util.*;
/** Own price deltas only; vanilla reputation and demand remain intact. */
public final class SocialConnections {
 private record Prices(ServerPlayer player,Map<MerchantOffer,Integer> deltas){}
 private static final Map<Villager,Prices> PRICES=new HashMap<>();
 public static void apply(Villager v,Player player){if(!(player instanceof ServerPlayer p))return;
  var previous=PRICES.remove(v);if(previous!=null)previous.deltas().forEach((offer,delta)->offer.addToSpecialPriceDiff(-delta));
  double connection=AdvancedFeruchemy.effect(p,FeruchemicalAttribute.CONNECTION)+.25*HemalurgyRuntime.passive(p,StolenPower.Kind.CONNECTION_IDENTITY);
  double trust=v.getAttachedOrCreate(ModRegistries.EMOTIONS).value(EmotionProfile.Emotion.TRUST)-.25;
  double rapport=p.getAttachedOrCreate(ModRegistries.RELATIONSHIPS).familiarity(v.getUUID())*.1;double faction=p.getTeam()==null||v.getTeam()==null?0:p.getTeam()==v.getTeam()?.03:-.02;
  double fraction=Math.clamp(rapport+faction+connection*dev.metallicarts.config.AdvancedConfig.get().social().tradeCoefficient()*(1+dev.metallicarts.config.AdvancedConfig.get().social().familiarityBonus()*p.getAttachedOrCreate(ModRegistries.CONNECTIONS).familiarity(region(p)))+trust*.1,-dev.metallicarts.config.AdvancedConfig.get().social().maximumSurcharge(),dev.metallicarts.config.AdvancedConfig.get().social().maximumDiscount());var deltas=new IdentityHashMap<MerchantOffer,Integer>();
  for(var offer:v.getOffers()){int delta=-(int)Math.floor(offer.getBaseCostA().getCount()*fraction);if(delta!=0){offer.addToSpecialPriceDiff(delta);deltas.put(offer,delta);}}
  PRICES.put(v,new Prices(p,deltas));
 }
 public static void trade(net.minecraft.world.entity.npc.villager.Villager v){if(v.getTradingPlayer() instanceof ServerPlayer p){p.setAttached(ModRegistries.RELATIONSHIPS,p.getAttachedOrCreate(ModRegistries.RELATIONSHIPS).trade(v.getUUID()));MemoryCapture.observe(p,"relationship","Traded with "+v.getName().getString(),"merchant:"+v.getUUID());}}
 public static void forget(Villager v){PRICES.remove(v);}
 public static void tick(ServerPlayer p,int tick){
  if(tick%20==0){String region=region(p);p.setAttached(ModRegistries.CONNECTIONS,p.getAttachedOrCreate(ModRegistries.CONNECTIONS).visit(region));}
  for(var entry:new ArrayList<>(PRICES.entrySet()))if(entry.getValue().player()==p){var v=entry.getKey();if(v.isRemoved()||v.getTradingPlayer()!=p){entry.getValue().deltas().forEach((offer,delta)->offer.addToSpecialPriceDiff(-delta));PRICES.remove(v);continue;}
   var before=v.getOffers().stream().map(MerchantOffer::getSpecialPriceDiff).toList();apply(v,p);
   if(!before.equals(v.getOffers().stream().map(MerchantOffer::getSpecialPriceDiff).toList()))p.sendMerchantOffers(p.containerMenu.containerId,v.getOffers(),v.getVillagerData().level(),v.getVillagerXp(),v.showProgressBar(),v.canRestock());
  }
 }
 public static String region(ServerPlayer p){return p.level().dimension().identifier()+":"+(p.getBlockX()>>7)+":"+(p.getBlockZ()>>7);}
 public static void stop(ServerPlayer p){for(var e:new ArrayList<>(PRICES.entrySet()))if(e.getValue().player()==p){e.getValue().deltas().forEach((offer,delta)->offer.addToSpecialPriceDiff(-delta));PRICES.remove(e.getKey());}}
 public static void clear(){for(var e:PRICES.values())e.deltas().forEach((offer,delta)->offer.addToSpecialPriceDiff(-delta));PRICES.clear();}
 private SocialConnections(){}
}
