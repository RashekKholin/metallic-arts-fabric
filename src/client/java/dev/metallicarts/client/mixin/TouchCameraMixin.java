package dev.metallicarts.client.mixin;
import dev.metallicarts.client.FeruchemyClient;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Reduced touch dampens the existing hurt cue; enhanced touch uses text instead of extra shaking. */
@Mixin(GameRenderer.class)
public abstract class TouchCameraMixin {
    @ModifyConstant(method="bobHurt",constant=@Constant(doubleValue=14.0))
    private double metallicarts$touch(double amplitude){return amplitude*Math.min(1,FeruchemyClient.senses.touch());}
}
