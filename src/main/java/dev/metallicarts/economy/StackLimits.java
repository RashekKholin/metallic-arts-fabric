package dev.metallicarts.economy;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
/** Preserve legacy oversized quantities by splitting, never by silently clamping away material. */
public final class StackLimits {
 public static void player(ServerPlayer p){var inventory=p.getInventory();for(int i=0;i<inventory.getContainerSize();i++){var stack=inventory.getItem(i);int maximum=Math.min(64,stack.getMaxStackSize());if(stack.isEmpty()||stack.getCount()<=maximum)continue;var overflow=stack.copyWithCount(stack.getCount()-maximum);stack.setCount(maximum);while(!overflow.isEmpty()){var piece=overflow.split(Math.min(maximum,overflow.getCount()));inventory.add(piece);if(!piece.isEmpty())p.drop(piece,false);}}}
 public static void initialize(){ServerPlayConnectionEvents.JOIN.register((h,s,server)->player(h.player));}
 private StackLimits(){}
}
