package dev.metallicarts.arts;
import dev.metallicarts.config.ProgressionConfig;
import dev.metallicarts.economy.*;
import dev.metallicarts.equipment.Medallions;
import dev.metallicarts.registry.ModRegistries;
import dev.metallicarts.hemalurgy.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import java.util.*;
/** Public survival actions are separate from operator diagnostics. No player target or arbitrary grant command. */
public final class SurvivalCommands {
 public static int count(ServerPlayer p,Item item){int n=0;for(int i=0;i<36;i++)if(p.getInventory().getItem(i).is(item))n+=p.getInventory().getItem(i).getCount();return n;}
 public static void take(ServerPlayer p,Item item,int n){for(int i=0;i<36&&n>0;i++){var stack=p.getInventory().getItem(i);if(stack.is(item)){int amount=Math.min(n,stack.getCount());stack.shrink(amount);n-=amount;}}}
 private static int act(ServerPlayer p,String action){try{switch(action){
 case "guide"->{for(int i=0;i<7;i++)p.sendSystemMessage(Component.translatable("metallicarts.guide."+i));}
 case "discover"->{if(!SurvivalProgression.awaken(p,false))throw new IllegalArgumentException("Discovery needs the configured XP level and happens once per character");var s=p.getAttachedOrCreate(ModRegistries.PROGRESSION);p.sendSystemMessage(Component.literal(s.feruchemy().map(m->"Discovered Feruchemy: "+m.id()).orElse("No innate Feruchemy. Trade for unsealed ability cores or study Hemalurgy.")));}
 case "mix"->MixedVialItem.mix(p);
 case "bind"->Medallions.bind(p);
 case "alloy"->{var held=p.getMainHandItem();var form=EconomyContent.form(p.getOffhandItem().getItem());if(held.getItem()!=EconomyContent.ITEMS.get("lerasium_bead")||held.getCount()!=1||form==null||!form.kind().equals("ingot")||Materials.metal(form.material()).isEmpty()||p.getOffhandItem().has(ModRegistries.METALMIND)||p.getOffhandItem().has(ModRegistries.SPIKE)||p.getOffhandItem().get(ModRegistries.METAL_DATA)!=null&&Ingestion.invested(p.getOffhandItem().get(ModRegistries.METAL_DATA))||p.getOffhandItem().get(EconomyContent.COMPOSITION)!=null&&p.getOffhandItem().get(EconomyContent.COMPOSITION).purity()<.999999)throw new IllegalArgumentException("Hold one pure bead and one ordinary standard-metal ingot in the off hand");p.getOffhandItem().shrink(1);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(EconomyContent.ITEMS.get("lerasium_"+form.material()+"_bead")));}
 case "mistwraith"->{if(!ProgressionConfig.get().mistwraithRitual()||!dev.metallicarts.config.AdvancedConfig.get().constructs().enabled())throw new IllegalArgumentException("Ritual disabled");if(p.getAttachedOrCreate(ModRegistries.CONSTRUCT).lineage()!=ConstructState.Lineage.HUMAN||p.getAttachedOrCreate(ModRegistries.CONSTRUCT).kind()!=ConstructState.Kind.HUMAN||HemalurgyRuntime.equipment(p).items().stream().anyMatch(s->!s.isEmpty()))throw new IllegalArgumentException("Requires an unspiked human body; this irreversible survival ritual removes ordinary body access");if(count(p,Items.SLIME_BALL)<32||count(p,Items.BONE)<32||count(p,Items.NETHER_STAR)<1)throw new IllegalArgumentException("Cost: 32 slime balls, 32 bones, one Nether star. Prepare a real paired Blessing before proceeding.");ConstructRuntime.lineage(p,ConstructState.Lineage.MISTWRAITH);take(p,Items.SLIME_BALL,32);take(p,Items.BONE,32);take(p,Items.NETHER_STAR,1);}
 case "lerasium"->{if(!ProgressionConfig.get().lerasiumRecipe())throw new IllegalArgumentException("Experimental lerasium acquisition disabled in host policy");if(p.getAttachedOrCreate(ModRegistries.LERASIUM_CLAIMED)||p.getStats().getValue(net.minecraft.stats.Stats.ENTITY_KILLED.get(net.minecraft.world.entity.EntityType.ENDER_DRAGON))<1||count(p,Items.NETHER_STAR)<1||count(p,Items.NETHERITE_INGOT)<8)throw new IllegalArgumentException("Once per character: defeat an Ender Dragon, spend one Nether star and eight Netherite ingots");if(p.getInventory().getFreeSlot()<0)throw new IllegalArgumentException("Make inventory space");take(p,Items.NETHER_STAR,1);take(p,Items.NETHERITE_INGOT,8);p.setAttached(ModRegistries.LERASIUM_CLAIMED,true);p.getInventory().add(new ItemStack(EconomyContent.ITEMS.get("lerasium_bead")));}
 default->throw new IllegalArgumentException("Unknown survival action");}return 1;}catch(IllegalArgumentException e){p.sendSystemMessage(Component.literal(e.getMessage()));return 0;}}
 public static void initialize(){
  CommandRegistrationCallback.EVENT.register((d,r,e)->{
   var root=Commands.literal("metallicarts-survival");
   root.then(Commands.argument("action",StringArgumentType.word()).suggests((c,b)->{for(var s:List.of("guide","discover","mix","bind","alloy","mistwraith","lerasium"))b.suggest(s);return b.buildFuture();}).executes(c->act(c.getSource().getPlayerOrException(),StringArgumentType.getString(c,"action"))));
   root.then(Commands.literal("medallion").then(Commands.argument("firstSlot",IntegerArgumentType.integer(0,11)).executes(c->{var p=c.getSource().getPlayerOrException();try{Medallions.equip(p,IntegerArgumentType.getInteger(c,"firstSlot"));return 1;}catch(IllegalArgumentException ex){p.sendSystemMessage(Component.literal(ex.getMessage()));return 0;}})));
   d.register(root);
  });
 } private SurvivalCommands(){}
}




