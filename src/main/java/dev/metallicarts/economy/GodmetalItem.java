package dev.metallicarts.economy;
import dev.metallicarts.arts.GodmetalRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
public final class GodmetalItem extends MaterialItem {
 private final GodmetalRuntime.Kind kind;
 public GodmetalItem(Properties p,GodmetalRuntime.Kind kind){super(p);this.kind=kind;}
 @Override public InteractionResult use(Level level,Player player,InteractionHand hand){if(!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;if(!p.isShiftKeyDown()||!GodmetalRuntime.burn(p,kind))return InteractionResult.FAIL;p.getItemInHand(hand).shrink(1);return InteractionResult.SUCCESS;}
}
