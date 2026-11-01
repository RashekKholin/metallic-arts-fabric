package dev.metallicarts.client.mixin;
import dev.metallicarts.client.ConstructAppearance;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.*;
@Mixin(AvatarRenderState.class) public abstract class ConstructAvatarStateMixin implements ConstructAppearance.Data {
 @Unique private int metallicarts$kind;@Unique private ConstructAppearance.Visual metallicarts$visual;
 public void metallicarts$appearance(int kind,int shape,boolean collapsed,ConstructAppearance.Visual visual){metallicarts$kind=kind;metallicarts$visual=visual;}
 public int metallicarts$kind(){return metallicarts$kind;}public ConstructAppearance.Visual metallicarts$visual(){return metallicarts$visual;}
}
