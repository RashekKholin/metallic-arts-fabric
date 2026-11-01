package dev.metallicarts.mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.metallicarts.arts.TemporalPlayers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
/** Mirrors a player's synchronized local rate on both logical sides, keeping food/use completion singular. */
@Mixin(LivingEntity.class)
abstract class TemporalItemUseMixin {
 @Unique private double metallicarts$useClock;
 @WrapMethod(method="updateUsingItem") private void metallicarts$use(ItemStack stack,Operation<Void> original){var entity=(LivingEntity)(Object)this;double rate=TemporalPlayers.localRate(entity);if(rate==1){metallicarts$useClock=0;original.call(stack);return;}metallicarts$useClock+=rate;int steps=(int)Math.floor(metallicarts$useClock);metallicarts$useClock-=steps;for(int i=0;i<steps&&entity.isUsingItem();i++)original.call(stack);}
}
