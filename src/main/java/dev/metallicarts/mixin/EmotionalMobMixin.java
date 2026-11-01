package dev.metallicarts.mixin;
import dev.metallicarts.arts.*;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Mob.class)
abstract class EmotionalMobMixin {
 @Shadow protected net.minecraft.world.entity.ai.goal.GoalSelector goalSelector;
 @Inject(method="<init>",at=@At("RETURN")) private void metallicarts$goal(CallbackInfo ci){goalSelector.addGoal(0,new EmotionCombatGoal((Mob)(Object)this));}
 @Inject(method="getTarget",at=@At("RETURN"),cancellable=true) private void metallicarts$calm(CallbackInfoReturnable<LivingEntity> ci){if(ci.getReturnValue()!=null&&EmotionService.blocksAttack((Mob)(Object)this,ci.getReturnValue()))ci.setReturnValue(null);}
 @Inject(method="doHurtTarget",at=@At("HEAD"),cancellable=true) private void metallicarts$peace(net.minecraft.server.level.ServerLevel level,Entity target,CallbackInfoReturnable<Boolean> ci){if(target instanceof LivingEntity living&&EmotionService.blocksAttack((Mob)(Object)this,living))ci.setReturnValue(false);}
}
