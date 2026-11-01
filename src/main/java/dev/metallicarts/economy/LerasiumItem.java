package dev.metallicarts.economy;
import dev.metallicarts.domain.*;
import dev.metallicarts.allomancy.ReserveLedger;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import java.util.*;
/** Explicit bead burn action. Separate from the stable sixteen-metal fuel/wheel protocol. */
public final class LerasiumItem extends MaterialItem {
 private final Optional<Metal> alloy;
 public LerasiumItem(Properties p,Optional<Metal> alloy){super(p);this.alloy=alloy;}
 @Override public InteractionResult use(Level level,Player player,InteractionHand hand){if(!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;if(!p.isShiftKeyDown())return InteractionResult.FAIL;var state=p.getAttachedOrCreate(ModRegistries.SPIRITWEB);var selected=alloy.isPresent()?List.of(alloy.get()):List.of(Metal.values());var missing=selected.stream().filter(m->state.strength(Art.ALLOMANCY,m)<2).toList();if(missing.isEmpty())return InteractionResult.FAIL;if(state.grants().size()+missing.size()>128)return InteractionResult.FAIL;var next=state;for(var m:missing){UUID source=UUID.randomUUID();next=ReserveLedger.grant(next,new PowerGrant(source,Art.ALLOMANCY,m,PowerGrant.Origin.INNATE,source,2));}p.setAttached(ModRegistries.SPIRITWEB,next);p.getItemInHand(hand).shrink(1);dev.metallicarts.network.ModNetworking.send(p);return InteractionResult.SUCCESS;}
}
