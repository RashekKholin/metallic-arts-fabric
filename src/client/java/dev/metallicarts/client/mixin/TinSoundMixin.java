package dev.metallicarts.client.mixin;
import dev.metallicarts.client.*;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public abstract class TinSoundMixin {
    @Inject(method="calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F",at=@At("RETURN"),cancellable=true)
    private void metallicarts$tin(SoundInstance sound,CallbackInfoReturnable<Float> cir){
        if(AllomancyClient.tin()&&TinOptions.INSTANCE.louderSound()&&
            java.util.Set.of(net.minecraft.sounds.SoundSource.BLOCKS,net.minecraft.sounds.SoundSource.HOSTILE,
                net.minecraft.sounds.SoundSource.NEUTRAL,net.minecraft.sounds.SoundSource.AMBIENT,net.minecraft.sounds.SoundSource.PLAYERS).contains(sound.getSource()))
            cir.setReturnValue(Math.min(4f,cir.getReturnValue()*(AllomancyClient.status.mode(3)==2?2f:1.4f)));
    }
}
