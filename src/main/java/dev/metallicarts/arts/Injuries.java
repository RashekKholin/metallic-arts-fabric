package dev.metallicarts.arts;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.feruchemy.FeruchemicalAttribute;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
public final class Injuries {
 private static final ResourceKey<Item> KEY=ResourceKey.create(Registries.ITEM,MetallicArts.id("bandage"));
 public static final Item BANDAGE=Registry.register(BuiltInRegistries.ITEM,KEY,new Item(new Item.Properties().setId(KEY).stacksTo(16)){
  @Override public InteractionResult use(Level level,Player player,InteractionHand hand){if(!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;var injury=p.getAttachedOrCreate(ModRegistries.TRAUMA);if(injury.total()==0||p.isSprinting()||p.hurtTime>0||p.getCooldowns().isOnCooldown(p.getItemInHand(hand)))return InteractionResult.FAIL;p.setAttached(ModRegistries.TRAUMA,injury.heal());p.getCooldowns().addCooldown(p.getItemInHand(hand),100);p.getItemInHand(hand).shrink(1);tick(p);return InteractionResult.CONSUME;}
 });
 public static void initialize(){ServerLivingEntityEvents.AFTER_DAMAGE.register((entity,source,base,taken,blocked)->{if(entity instanceof ServerPlayer p&&taken>=6&&!blocked&&p.isAlive()){var injury=p.getAttachedOrCreate(ModRegistries.TRAUMA);p.setAttached(ModRegistries.TRAUMA,injury.injure((int)(p.level().getGameTime()+p.getUUID().hashCode())));tick(p);}});}
 public static void tick(ServerPlayer p){var injury=p.getAttachedOrCreate(ModRegistries.TRAUMA);double will=Math.max(0,AdvancedFeruchemy.effect(p,FeruchemicalAttribute.DETERMINATION));modifier(p,Attributes.ATTACK_SPEED,"trauma_arm",-Math.min(.6,injury.arm()*.15),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);modifier(p,Attributes.BLOCK_BREAK_SPEED,"trauma_hand",-Math.min(.6,injury.arm()*.15),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);modifier(p,Attributes.MOVEMENT_SPEED,"trauma_leg",-injury.leg()*.008/(1+will),AttributeModifier.Operation.ADD_VALUE);modifier(p,Attributes.BLOCK_BREAK_SPEED,"trauma_head",-injury.head()*.1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);}
 private static void modifier(ServerPlayer p,Holder<Attribute> attr,String name,double amount,AttributeModifier.Operation op){var a=p.getAttribute(attr);if(a==null)return;var id=MetallicArts.id(name);if(amount==0)a.removeModifier(id);else if(a.getModifier(id)==null||a.getModifier(id).amount()!=amount)a.addOrUpdateTransientModifier(new AttributeModifier(id,amount,op));}
 private Injuries(){}
}
