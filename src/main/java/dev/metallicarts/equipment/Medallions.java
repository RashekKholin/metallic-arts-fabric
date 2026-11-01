package dev.metallicarts.equipment;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.economy.EconomyContent;
import dev.metallicarts.feruchemy.*;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import java.util.*;
/** A physical 2-3 core carrier. Each independent core retains its canonical lease, charge and typed ability. */
public final class Medallions {
 public static final DataComponentType<List<ItemStack>> CORES=Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,MetallicArts.id("medallion_cores"),DataComponentType.<List<ItemStack>>builder().persistent(ItemStack.CODEC.listOf(1,3)).networkSynchronized(ByteBufCodecs.fromCodec(ItemStack.CODEC.listOf(1,3))).build());
 public static void bind(ServerPlayer p){var held=p.getMainHandItem();var other=p.getOffhandItem();var list=new ArrayList<ItemStack>();if(held.getItem()==EconomyContent.ITEMS.get("medallion")){var cores=held.get(CORES);if(cores==null)throw new IllegalArgumentException("Invalid medallion");cores.forEach(s->list.add(s.copy()));}else list.add(held.copy());list.add(other.copy());if(list.size()>3||held.getCount()!=1||other.getCount()!=1)throw new IllegalArgumentException("At most three individual cores");var ledger=MetalmindLedger.get(p.level().getServer());var ids=new HashSet<UUID>();for(var core:list){var ref=core.get(ModRegistries.METALMIND);if(ref==null)throw new IllegalArgumentException("Use charged unsealed Nicrosil ability cores");var a=ledger.resolve(ref.token());if(!ids.add(a.token().instance())||a.attribute()!=FeruchemicalAttribute.INVESTITURE||a.metal()!=dev.metallicarts.domain.Metal.NICROSIL||a.lease().isPresent()||a.charge()<=0||!a.content().unsealed()||a.content().power().isEmpty())throw new IllegalArgumentException("Each core must be distinct, free, charged and unsealed");}var result=new ItemStack(EconomyContent.ITEMS.get("medallion"));result.set(CORES,list.stream().map(ItemStack::copy).toList());p.setItemInHand(InteractionHand.MAIN_HAND,result);p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);}
 public static void equip(ServerPlayer p,int first){var held=p.getMainHandItem();var cores=held.get(CORES);if(held.getItem()!=EconomyContent.ITEMS.get("medallion")||held.getCount()!=1||cores==null||cores.size()<2||first<0||first+cores.size()>12)throw new IllegalArgumentException("Hold a 2-3 core medallion and choose adjacent free contact slots");var ledger=MetalmindLedger.get(p.level().getServer());var equipment=ContactService.equipment(p);var ids=new HashSet<UUID>();for(int i=0;i<cores.size();i++){var ref=cores.get(i).get(ModRegistries.METALMIND);if(ref==null)throw new IllegalArgumentException("Missing core reference");var a=ledger.resolve(ref.token());var lease=new AssetLease(p.getUUID(),first+i);if(!ids.add(a.token().instance())||a.attribute()!=FeruchemicalAttribute.INVESTITURE||a.metal()!=dev.metallicarts.domain.Metal.NICROSIL||a.lease().isPresent()||!a.content().unsealed()||a.content().power().isEmpty()||a.charge()<=0||!equipment.item(first+i).isEmpty()||ledger.occupied(lease))throw new IllegalArgumentException("Invalid core or occupied contact slot");}for(int i=0;i<cores.size();i++){var core=cores.get(i).copy();var a=ledger.claim(core.get(ModRegistries.METALMIND).token(),new AssetLease(p.getUUID(),first+i));core.set(ModRegistries.METALMIND,MetalmindData.of(a));equipment=equipment.with(first+i,core);}p.setAttached(ModRegistries.CONTACT_EQUIPMENT,equipment);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);}
 private Medallions(){}
}


