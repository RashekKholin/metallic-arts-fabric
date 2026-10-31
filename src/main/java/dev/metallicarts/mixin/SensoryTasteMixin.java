package dev.metallicarts.mixin;
import dev.metallicarts.feruchemy.FeruchemyRuntime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class SensoryTasteMixin {
    @Inject(method="finishUsingItem",at=@At("HEAD"))
    private void metallicarts$taste(Level level,LivingEntity user,CallbackInfoReturnable<ItemStack> result){
        if(user instanceof ServerPlayer player)FeruchemyRuntime.taste(player,(ItemStack)(Object)this);
    }
}
