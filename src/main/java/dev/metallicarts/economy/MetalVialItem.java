package dev.metallicarts.economy;

import dev.metallicarts.domain.*;
import dev.metallicarts.registry.ModRegistries;
import dev.metallicarts.network.ModNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

public final class MetalVialItem extends MaterialItem {
    private final Metal metal;
    public MetalVialItem(Properties properties, Metal metal) { super(properties); this.metal = metal; }
    public ItemStack prepared() {
        ItemStack stack=new ItemStack(this);
        stack.set(ModRegistries.METAL_DATA,new MetalItemData(1,java.util.UUID.randomUUID(),metal,Materials.PORTION_MASS,1,0,java.util.Optional.empty()));
        return stack;
    }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var data = player.getItemInHand(hand).get(ModRegistries.METAL_DATA);
        if (data == null || data.metal() != metal) return InteractionResult.FAIL;
        if (player instanceof ServerPlayer serverPlayer && !Ingestion.canAccept(serverPlayer.getAttachedOrCreate(ModRegistries.SPIRITWEB), data)) {
            player.sendOverlayMessage(Component.literal("Unsafe metal or full reserves. The vial was kept."));
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }
    @Override public int getUseDuration(ItemStack stack, LivingEntity user) { return 32; }
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack) { return ItemUseAnimation.DRINK; }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (!(user instanceof ServerPlayer player)) return stack;
        var data = stack.get(ModRegistries.METAL_DATA);
        var state = player.getAttachedOrCreate(ModRegistries.SPIRITWEB);
        if (data == null || data.metal() != metal || !Ingestion.canAccept(state, data)) return stack;
        player.setAttached(ModRegistries.SPIRITWEB, Ingestion.accept(state, data));
        ModNetworking.send(player);
        // Even creative consumption uses the vial: ingested resources cannot be duplicated by drinking it repeatedly.
        stack.shrink(1);
        player.sendOverlayMessage(Component.literal("Ingested " + metal.id() + "; no power activated."));
        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        if (stack.isEmpty()) return bottle;
        if (!player.getInventory().add(bottle)) player.drop(bottle, false);
        return stack;
    }
}
