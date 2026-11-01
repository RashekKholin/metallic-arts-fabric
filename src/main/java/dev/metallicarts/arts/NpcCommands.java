package dev.metallicarts.arts;
import dev.metallicarts.allomancy.*;
import dev.metallicarts.domain.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import com.mojang.brigadier.arguments.*;
import net.minecraft.network.chat.Component;
public final class NpcCommands {
 public static void initialize(){CommandRegistrationCallback.EVENT.register((dispatcher,registry,env)->{
  dispatcher.register(Commands.literal("metallicarts").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(Commands.literal("npcfuel").then(Commands.argument("entity",EntityArgument.entity()).then(Commands.argument("metal",StringArgumentType.word()).then(Commands.argument("micrograms",LongArgumentType.longArg(1,Spiritweb.MAX_RESERVE)).executes(c->{var e=EntityArgument.getEntity(c,"entity");if(!(e instanceof net.minecraft.world.entity.Mob mob))return 0;try{var metal=Metal.valueOf(StringArgumentType.getString(c,"metal").toUpperCase(java.util.Locale.ROOT));mob.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.credit(mob.getAttachedOrCreate(ModRegistries.SPIRITWEB),metal,LongArgumentType.getLong(c,"micrograms")));return 1;}catch(IllegalArgumentException ex){c.getSource().sendFailure(Component.literal(ex.getMessage()));return 0;}}))))));
  dispatcher.register(Commands.literal("metallicarts").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(Commands.literal("npcburn").then(Commands.argument("entity",EntityArgument.entity()).then(Commands.argument("metal",StringArgumentType.word()).then(Commands.argument("mode",StringArgumentType.word()).suggests((c,b)->{b.suggest("off");b.suggest("burn");b.suggest("flare");return b.buildFuture();}).executes(c->{var e=EntityArgument.getEntity(c,"entity");if(!(e instanceof net.minecraft.world.entity.Mob mob))return 0;try{return NpcArts.request(mob,Metal.valueOf(StringArgumentType.getString(c,"metal").toUpperCase(java.util.Locale.ROOT)),BurnMode.valueOf(StringArgumentType.getString(c,"mode").toUpperCase(java.util.Locale.ROOT)))?1:0;}catch(IllegalArgumentException ex){c.getSource().sendFailure(Component.literal(ex.getMessage()));return 0;}}))))));
 });}
 private NpcCommands(){}
}
