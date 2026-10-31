package dev.metallicarts.client.mixin;
import dev.metallicarts.client.*;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class TinLightmapMixin {
    @Inject(method="extract",at=@At("RETURN"))
    private void metallicarts$tin(LightmapRenderState state,float partialTick,CallbackInfo ci){
        if(AllomancyClient.tin()&&TinOptions.INSTANCE.brighterVision()){
            state.needsUpdate=true;
            state.nightVisionEffectIntensity=1;
            state.nightVisionColor=new Vector3f(1,1,1);
        }
    }
}
