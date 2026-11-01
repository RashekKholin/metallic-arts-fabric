package dev.metallicarts.mixin;
import dev.metallicarts.arts.AdvancedFeruchemy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LivingEntity.class) public abstract class GoldRecoveryMixin {
 @Inject(method="checkTotemDeathProtection",at=@At("RETURN"),cancellable=true) private void metallicarts$rescue(DamageSource source,CallbackInfoReturnable<Boolean> cir){if(!cir.getReturnValue()&&(Object)this instanceof ServerPlayer p&&AdvancedFeruchemy.rescue(p,source))cir.setReturnValue(true);}
}
