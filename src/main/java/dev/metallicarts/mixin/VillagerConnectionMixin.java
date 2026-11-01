package dev.metallicarts.mixin;
import dev.metallicarts.arts.SocialConnections;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Villager.class) public abstract class VillagerConnectionMixin {
 @Inject(method="updateSpecialPrices",at=@At("TAIL")) private void metallicarts$prices(Player p,CallbackInfo ci){SocialConnections.apply((Villager)(Object)this,p);}
 @Inject(method="resetSpecialPrices",at=@At("HEAD")) private void metallicarts$reset(CallbackInfo ci){SocialConnections.forget((Villager)(Object)this);}
}
