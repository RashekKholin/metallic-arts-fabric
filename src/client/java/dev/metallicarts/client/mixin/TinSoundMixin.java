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
        if(
            java.util.Set.of(net.minecraft.sounds.SoundSource.BLOCKS,net.minecraft.sounds.SoundSource.HOSTILE,
                net.minecraft.sounds.SoundSource.NEUTRAL,net.minecraft.sounds.SoundSource.AMBIENT,net.minecraft.sounds.SoundSource.PLAYERS).contains(sound.getSource()))
        {
            double gain=FeruchemyClient.senses.hearing();
            if(!TinOptions.INSTANCE.louderSound())gain=Math.min(1,gain);
            else if(AllomancyClient.tin())gain*=AllomancyClient.status.mode(3)==2?2:1.4;
            cir.setReturnValue((float)Math.min(4,cir.getReturnValue()*gain));
        }
    }
}
