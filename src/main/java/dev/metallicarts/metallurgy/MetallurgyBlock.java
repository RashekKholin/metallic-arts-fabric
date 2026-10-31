package dev.metallicarts.metallurgy;

import com.mojang.serialization.MapCodec;
import dev.metallicarts.economy.EconomyContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;

public final class MetallurgyBlock extends BaseEntityBlock {
    public static final MapCodec<MetallurgyBlock> CODEC = simpleCodec(MetallurgyBlock::new);
    public static final IntegerProperty SPECIALTY = IntegerProperty.create("specialty", 0, 3);
    public MetallurgyBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(SPECIALTY, 0)); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(SPECIALTY); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new MetallurgyBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, EconomyContent.TABLE_ENTITY, MetallurgyBlockEntity::tick);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                int specialty = (state.getValue(SPECIALTY) + 1) % 4;
                level.setBlock(pos, state.setValue(SPECIALTY, specialty), 3);
                player.sendOverlayMessage(Component.literal("Metallurgist specialty: " + new String[]{"Common metals", "Alloys", "Rare metals", "Equipment"}[specialty]));
            } else if (level.getBlockEntity(pos) instanceof MetallurgyBlockEntity table) player.openMenu(table);
        }
        return InteractionResult.SUCCESS;
    }
}
