package dev.metallicarts.mixin;

import dev.metallicarts.economy.EconomyContent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(PotionBrewing.class)
public abstract class PotionBrewingMixin {
    @Inject(method = "isIngredient", at = @At("HEAD"), cancellable = true)
    private void metallicarts$ingredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (EconomyContent.flakes(stack)) cir.setReturnValue(true);
    }
}
