package dev.metallicarts.mixin;
import dev.metallicarts.arts.SocialConnections;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AbstractVillager.class) public abstract class MerchantRelationshipMixin {
 @Inject(method="notifyTrade",at=@At("TAIL")) private void metallicarts$trade(MerchantOffer offer,CallbackInfo ci){if((Object)this instanceof Villager v)SocialConnections.trade(v);}
}
