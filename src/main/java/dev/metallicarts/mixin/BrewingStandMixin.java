package dev.metallicarts.mixin;

import dev.metallicarts.economy.*;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandMixin {
    @Inject(method = "isBrewable", at = @At("HEAD"), cancellable = true)
    private static void metallicarts$canBrew(PotionBrewing brewing, NonNullList<ItemStack> items, CallbackInfoReturnable<Boolean> cir) {
        if (EconomyContent.flakes(items.get(3))) cir.setReturnValue(MetalBrewing.canBrew(items));
    }
    @Inject(method = "doBrew", at = @At("HEAD"), cancellable = true)
    private static void metallicarts$brew(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci) {
        if (EconomyContent.flakes(items.get(3))) {
            MetalBrewing.brew(items); level.levelEvent(1035, pos, 0); ci.cancel();
        }
    }
}
