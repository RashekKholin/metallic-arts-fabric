package dev.metallicarts.economy;
import com.mojang.serialization.Codec;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.domain.*;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import java.util.*;
/** Ordinary metal only: invested previews cannot be laundered into a mixed vial. */
public final class MixedVialItem extends MaterialItem {
 public static final DataComponentType<List<MetalItemData>> CONTENT=Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,MetallicArts.id("mixed_fuel"),DataComponentType.<List<MetalItemData>>builder().persistent(MetalItemData.CODEC.listOf(1,16)).networkSynchronized(ByteBufCodecs.fromCodec(MetalItemData.CODEC.listOf(1,16))).build());
 public MixedVialItem(Properties p){super(p);}
 public static Spiritweb ingest(Spiritweb state,List<MetalItemData> contents){if(contents.isEmpty()||contents.size()>16||contents.stream().anyMatch(Ingestion::invested)||contents.stream().map(MetalItemData::instance).distinct().count()!=contents.size())throw new IllegalArgumentException("Ordinary, distinct fuel batches only");for(var data:contents)state=Ingestion.accept(state,data);return state;}
 @Override public InteractionResult use(Level level,Player p,InteractionHand hand){var data=p.getItemInHand(hand).get(CONTENT);if(data==null)return InteractionResult.FAIL;if(p instanceof ServerPlayer s)try{ingest(s.getAttachedOrCreate(ModRegistries.SPIRITWEB),data);}catch(IllegalArgumentException e){return InteractionResult.FAIL;}p.startUsingItem(hand);return InteractionResult.CONSUME;}
 @Override public int getUseDuration(ItemStack stack,LivingEntity user){return 32;}
 @Override public ItemUseAnimation getUseAnimation(ItemStack stack){return ItemUseAnimation.DRINK;}
 @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity user){if(!(user instanceof ServerPlayer p))return stack;var data=stack.get(CONTENT);if(data==null)return stack;try{var next=ingest(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),data);p.setAttached(ModRegistries.SPIRITWEB,next);stack.shrink(1);dev.metallicarts.network.ModNetworking.send(p);return new ItemStack(Items.GLASS_BOTTLE);}catch(IllegalArgumentException e){return stack;}}
 public static void mix(ServerPlayer p){var left=p.getMainHandItem();var right=p.getOffhandItem();var a=left.get(ModRegistries.METAL_DATA);var b=right.get(ModRegistries.METAL_DATA);var existing=left.get(CONTENT);var contents=new ArrayList<MetalItemData>();if(existing!=null&&left.getItem() instanceof MixedVialItem)contents.addAll(existing);else if(left.getItem() instanceof MetalVialItem&&a!=null)contents.add(a);else throw new IllegalArgumentException("Hold a filled standard or mixed vial in the main hand");if(!(right.getItem() instanceof MetalVialItem)||b==null||left.getCount()!=1||right.getCount()!=1)throw new IllegalArgumentException("Hold one filled standard vial in the off hand");contents.add(b);ingest(Spiritweb.empty(),contents);var result=new ItemStack(EconomyContent.ITEMS.get("mixed_vial"));result.set(CONTENT,List.copyOf(contents));p.setItemInHand(InteractionHand.MAIN_HAND,result);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.GLASS_BOTTLE));}
}
