package dev.metallicarts.client.mixin;
import dev.metallicarts.client.ConstructAppearance;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(LivingEntityRenderer.class) public abstract class ConstructRendererMixin {
 @Inject(method="extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",at=@At("TAIL")) private void metallicarts$extract(LivingEntity entity,LivingEntityRenderState state,float delta,CallbackInfo ci){ConstructAppearance.extract(entity,state);}
 @Inject(method="getModelTint",at=@At("RETURN"),cancellable=true) private void metallicarts$tint(LivingEntityRenderState state,CallbackInfoReturnable<Integer> cir){cir.setReturnValue(ConstructAppearance.tint(state,cir.getReturnValue()));}
 @Inject(method="submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",at=@At("HEAD"),cancellable=true) private void metallicarts$body(LivingEntityRenderState state,PoseStack poses,SubmitNodeCollector collector,CameraRenderState camera,CallbackInfo ci){if(ConstructAppearance.submit(state,poses,collector,camera))ci.cancel();}
}
