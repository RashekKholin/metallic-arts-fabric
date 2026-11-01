package dev.metallicarts.arts;
import dev.metallicarts.hemalurgy.*;
import dev.metallicarts.feruchemy.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import java.util.*;
/** Operator diagnostics repair references only when the same canonical owner/slot/seal already exists. */
public final class SystemsCommands {
 public static int recoverLeases(net.minecraft.server.level.ServerPlayer p){
  int recovered=0;var minds=ContactService.equipment(p);var spikes=HemalurgyRuntime.equipment(p);
  for(var a:MetalmindLedger.get(p.level().getServer()).all())if(!a.consumed()&&a.lease().filter(l->l.player().equals(p.getUUID())).isPresent()){
   int slot=a.lease().orElseThrow().slot();if(!minds.item(slot).isEmpty())continue;
   var item=new net.minecraft.world.item.ItemStack(dev.metallicarts.economy.EconomyContent.item(a.metal().id(),"ring"));item.set(ModRegistries.METALMIND,MetalmindData.of(a));item.set(ModRegistries.METAL_DATA,new dev.metallicarts.domain.MetalItemData(1,a.token().instance(),a.metal(),a.massMicrograms(),a.purity(),0,Optional.empty()));minds=minds.with(slot,item);recovered++;
  }
  for(var a:SpikeLedger.get(p.level().getServer()).all())if(a.lease().filter(l->l.player().equals(p.getUUID())).isPresent()){
   int slot=a.lease().orElseThrow().slot();if(!spikes.item(slot).isEmpty())continue;
   var item=new net.minecraft.world.item.ItemStack(dev.metallicarts.economy.EconomyContent.item(a.metal().id(),"spike"));item.set(ModRegistries.SPIKE,SpikeData.of(a));spikes=spikes.with(slot,item);recovered++;
  }
  p.setAttached(ModRegistries.CONTACT_EQUIPMENT,minds);p.setAttached(ModRegistries.SPIKES,spikes);return recovered;
 }
 public static int audit(net.minecraft.server.level.ServerPlayer p,boolean repair){int invalid=0,repaired=0;var minds=ContactService.equipment(p);var spikes=HemalurgyRuntime.equipment(p);var ml=MetalmindLedger.get(p.level().getServer());var sl=SpikeLedger.get(p.level().getServer());
  for(int i=0;i<12;i++){var lease=new dev.metallicarts.equipment.AssetLease(p.getUUID(),i);var mind=minds.item(i).get(ModRegistries.METALMIND);if(mind!=null){var a=ml.find(mind.token().instance());if(a.isPresent()&&!a.get().consumed()&&a.get().lease().filter(lease::equals).isPresent()&&a.get().token().seal().equals(mind.token().seal())){if(repair&&!a.get().token().equals(mind.token())){var item=minds.item(i).copy();item.set(ModRegistries.METALMIND,MetalmindData.of(a.get()));minds=minds.with(i,item);repaired++;}}else invalid++;}
   var spike=spikes.item(i).get(ModRegistries.SPIKE);if(spike!=null){var a=sl.find(spike.token().instance());if(a.isPresent()&&a.get().lease().filter(lease::equals).isPresent()&&a.get().token().seal().equals(spike.token().seal())){if(repair&&!a.get().token().equals(spike.token())){var item=spikes.item(i).copy();item.set(ModRegistries.SPIKE,SpikeData.of(a.get()));spikes=spikes.with(i,item);repaired++;}}else invalid++;}
  }
  if(repair){p.setAttached(ModRegistries.CONTACT_EQUIPMENT,minds);p.setAttached(ModRegistries.SPIKES,spikes);}
  return invalid*1000+repaired;
 }
 public static void initialize(){CommandRegistrationCallback.EVENT.register((dispatcher,registry,environment)->dispatcher.register(Commands.literal("metallicarts").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
  .then(Commands.literal("assets").then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("action",StringArgumentType.word()).suggests((c,b)->{b.suggest("audit");b.suggest("repairrefs");b.suggest("recoverleases");return b.buildFuture();}).executes(c->{var p=EntityArgument.getPlayer(c,"player");String action=StringArgumentType.getString(c,"action");if(action.equals("recoverleases")){int count=recoverLeases(p);c.getSource().sendSuccess(()->Component.literal("Restored "+count+" existing canonical leased references into empty owned slots; no charge created"),false);return count;}if(!Set.of("audit","repairrefs").contains(action))return 0;int result=audit(p,action.equals("repairrefs"));c.getSource().sendSuccess(()->Component.literal("Unrecoverable references="+(result/1000)+", repaired="+(result%1000)+"; no charge was imported or created"),false);return 1;}))))
  .then(Commands.literal("constructbase").then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("lineage",StringArgumentType.word()).suggests((c,b)->{b.suggest("human");b.suggest("mistwraith");b.suggest("reset");return b.buildFuture();}).executes(c->{try{var p=EntityArgument.getPlayer(c,"player");String value=StringArgumentType.getString(c,"lineage");if(value.equals("reset"))ConstructRuntime.reset(p);else ConstructRuntime.lineage(p,ConstructState.Lineage.valueOf(value.toUpperCase(Locale.ROOT)));c.getSource().sendSuccess(()->Component.literal("Construct base updated; canonical spikes and earned grants unchanged"),true);return 1;}catch(IllegalArgumentException e){c.getSource().sendFailure(Component.literal(e.getMessage()));return 0;}}))))
 ));}
 private SystemsCommands(){}
}
