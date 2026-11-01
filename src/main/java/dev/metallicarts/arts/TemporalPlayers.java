package dev.metallicarts.arts;
import dev.metallicarts.MetallicArts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Holder;
/** Player-safe local clocks: never skips keepalive/network ticks or changes global server time. */
public final class TemporalPlayers {
 public static double localRate(LivingEntity entity){if(!(entity instanceof Player))return 1;var attribute=entity.getAttribute(Attributes.BLOCK_BREAK_SPEED);var modifier=attribute==null?null:attribute.getModifier(MetallicArts.id("temporal_rate"));return modifier==null?1:Math.clamp(1+modifier.amount(),.1,8);}
 public static void frame(MinecraftServer server){for(var player:server.getPlayerList().getPlayers()){apply(player);if(server.getTickCount()%10==0&&net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.canSend(player,dev.metallicarts.network.TemporalFields.TYPE))net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player,new dev.metallicarts.network.TemporalFields(TemporalEngine.surfaces(player.level(),player.position())));}}
 public static void apply(ServerPlayer p){double rate=p.isAlive()&&!p.isSpectator()?TemporalEngine.rate(p.level(),p.position()):1;for(var attribute:java.util.List.of(Attributes.MOVEMENT_SPEED,Attributes.FLYING_SPEED,Attributes.ATTACK_SPEED,Attributes.BLOCK_BREAK_SPEED,Attributes.JUMP_STRENGTH))change(p,attribute,rate-1);change(p,Attributes.GRAVITY,rate*rate-1);}
 public static void clear(ServerPlayer p){for(var attribute:java.util.List.of(Attributes.MOVEMENT_SPEED,Attributes.FLYING_SPEED,Attributes.ATTACK_SPEED,Attributes.BLOCK_BREAK_SPEED,Attributes.JUMP_STRENGTH,Attributes.GRAVITY))change(p,attribute,0);}
 private static void change(ServerPlayer p,Holder<Attribute> type,double amount){var attribute=p.getAttribute(type);if(attribute==null)return;var id=MetallicArts.id("temporal_rate");var old=attribute.getModifier(id);if(amount==0){if(old!=null)attribute.removeModifier(id);}else if(old==null||old.amount()!=amount)attribute.addOrUpdateTransientModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
 private TemporalPlayers(){}
}
