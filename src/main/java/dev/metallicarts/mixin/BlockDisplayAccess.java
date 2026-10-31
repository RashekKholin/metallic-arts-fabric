package dev.metallicarts.mixin;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(Display.BlockDisplay.class)
public interface BlockDisplayAccess {
    @Invoker("setBlockState")void metallicarts$setBlockState(BlockState state);
    @Invoker("getBlockState")BlockState metallicarts$getBlockState();
}
