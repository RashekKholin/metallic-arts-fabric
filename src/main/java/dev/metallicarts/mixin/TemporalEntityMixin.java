package dev.metallicarts.mixin;

import dev.metallicarts.arts.TemporalEngine;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
abstract class TemporalEntityMixin {
    @Inject(method="tickNonPassenger",at=@At("HEAD"),cancellable=true)
    private void metallicarts$clock(Entity entity,CallbackInfo ci){if(!TemporalEngine.extra()&&TemporalEngine.entitySteps(entity)==0)ci.cancel();}
    @Inject(method="tickNonPassenger",at=@At("TAIL"))
    private void metallicarts$accelerate(Entity entity,CallbackInfo ci){
        if(TemporalEngine.extra()||entity.isRemoved())return;
        int extra=TemporalEngine.extraSteps(entity);var level=(ServerLevel)(Object)this;
        TemporalEngine.runExtra(()->{for(int n=0;n<extra&&!entity.isRemoved();n++)level.tickNonPassenger(entity);});
    }
}
