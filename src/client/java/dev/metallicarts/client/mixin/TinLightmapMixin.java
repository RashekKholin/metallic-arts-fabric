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
        double gain=FeruchemyClient.senses.sight();
        if(gain<1){
            state.needsUpdate=true;state.brightness*=gain;
            state.darknessEffectScale=Math.max(state.darknessEffectScale,(float)((1-gain)*.8));
            state.nightVisionEffectIntensity*=gain;
        }
        if(TinOptions.INSTANCE.brighterVision()&&(AllomancyClient.tin()||gain>1)){
            state.needsUpdate=true;
            state.nightVisionEffectIntensity=Math.max(state.nightVisionEffectIntensity,(float)Math.min(1,(AllomancyClient.tin()?1:gain-1)*Math.min(1,gain)));
            state.nightVisionColor=new Vector3f(1,1,1);
        }
    }
}
