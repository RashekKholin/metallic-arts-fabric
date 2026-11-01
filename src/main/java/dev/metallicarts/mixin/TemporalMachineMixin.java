package dev.metallicarts.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.metallicarts.arts.TemporalEngine;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Level.class)
abstract class TemporalMachineMixin {
    @WrapOperation(method="tickBlockEntities",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/block/entity/TickingBlockEntity;tick()V"))
    private void metallicarts$clock(TickingBlockEntity ticker,Operation<Void> original){
        int steps=(Object)this instanceof ServerLevel level?TemporalEngine.machineSteps(level,ticker.getPos(),ticker.getType()):1;
        for(int n=0;n<steps&&!ticker.isRemoved();n++)original.call(ticker);
    }
}
