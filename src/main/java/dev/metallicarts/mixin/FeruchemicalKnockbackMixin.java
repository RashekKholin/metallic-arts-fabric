package dev.metallicarts.mixin;
import dev.metallicarts.feruchemy.FeruchemyRuntime;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(LivingEntity.class)
public abstract class FeruchemicalKnockbackMixin {
    @ModifyVariable(method="knockback",at=@At("HEAD"),argsOnly=true,ordinal=0)
    private double metallicarts$weight(double strength){
        return (Object)this instanceof ServerPlayer player?strength*80/FeruchemyRuntime.mass(player):strength;
    }
}
