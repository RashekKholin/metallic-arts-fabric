package dev.metallicarts.mixin;

import dev.metallicarts.economy.Ingestion;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(MerchantOffer.class)
public abstract class MerchantOfferMixin {
    @Inject(method="satisfiedBy",at=@At("HEAD"),cancellable=true)
    private void metallicarts$protectInvestedItems(ItemStack a,ItemStack b,CallbackInfoReturnable<Boolean> cir) {
        for (ItemStack stack : new ItemStack[]{a,b}) {
            var data=stack.get(ModRegistries.METAL_DATA);
            if (data!=null && Ingestion.invested(data)) { cir.setReturnValue(false); return; }
            var batch=stack.get(dev.metallicarts.economy.EconomyContent.COMPOSITION);
            if(batch!=null && !batch.viable()) { cir.setReturnValue(false); return; }
        }
    }
}
