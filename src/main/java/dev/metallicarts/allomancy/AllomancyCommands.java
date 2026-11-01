package dev.metallicarts.allomancy;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.*;
import dev.metallicarts.domain.*;
import dev.metallicarts.network.ModNetworking;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

/** Operator testing tools. They edit ownership/fuel, never activate an unimplemented effect. */
public final class AllomancyCommands {
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> dispatcher.register(
            Commands.literal("metallicarts").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("grant").then(Commands.argument("player",EntityArgument.player())
                    .then(metal().then(Commands.argument("strength",DoubleArgumentType.doubleArg(0.000001,1000))
                        .executes(c->edit(c,"grant"))))))
                .then(Commands.literal("grantferuchemy").then(Commands.argument("player",EntityArgument.player())
                    .then(metal().then(Commands.argument("strength",DoubleArgumentType.doubleArg(0.000001,1000))
                        .executes(c->edit(c,"grantferuchemy"))))))
                .then(Commands.literal("revoke").then(Commands.argument("player",EntityArgument.player())
                    .then(Commands.argument("grant",StringArgumentType.word()).executes(c->edit(c,"revoke")))))
                .then(Commands.literal("fuel").then(Commands.argument("player",EntityArgument.player())
                    .then(metal().then(Commands.argument("micrograms",LongArgumentType.longArg(1,Spiritweb.MAX_RESERVE))
                        .executes(c->edit(c,"fuel"))))))
                .then(Commands.literal("inspect").then(Commands.argument("player",EntityArgument.player()).executes(AllomancyCommands::inspect)))
                .then(Commands.literal("unkeymetalmind").then(Commands.argument("player",EntityArgument.player()).executes(c->{
                    var player=EntityArgument.getPlayer(c,"player");var stack=player.getMainHandItem();var mind=stack.get(ModRegistries.METALMIND);
                    if(stack.getCount()!=1||mind==null)throw new SimpleCommandExceptionType(Component.literal("Hold one registered metalmind")).create();
                    try{
                        var asset=dev.metallicarts.feruchemy.MetalmindLedger.get(player.level().getServer()).unkey(mind.token());
                        stack.set(ModRegistries.METALMIND,dev.metallicarts.feruchemy.MetalmindData.of(asset));
                        c.getSource().sendSuccess(()->Component.literal("Administrative unkeying complete; charge retained; Feruchemical ability still required"),true);return 1;
                    }catch(IllegalArgumentException e){throw new SimpleCommandExceptionType(Component.literal(e.getMessage())).create();}
                })))
        ));
    }
    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack,String> metal() {
        return Commands.argument("metal",StringArgumentType.word()).suggests((context,builder)->{
            for(Metal m:Metal.values())if(m.id().startsWith(builder.getRemainingLowerCase()))builder.suggest(m.id());
            return builder.buildFuture();
        });
    }
    private static int edit(CommandContext<CommandSourceStack> c,String operation) throws CommandSyntaxException {
        ServerPlayer player=EntityArgument.getPlayer(c,"player");
        Spiritweb before=player.getAttachedOrCreate(ModRegistries.SPIRITWEB);
        try {
            Spiritweb after;String detail;
            if(operation.equals("revoke")) {
                UUID id=UUID.fromString(StringArgumentType.getString(c,"grant"));
                after=ReserveLedger.revoke(before,id);detail="Revoked grant "+id;
            } else {
                String name=StringArgumentType.getString(c,"metal");
                Metal metal=Arrays.stream(Metal.values()).filter(m->m.id().equals(name)).findFirst()
                    .orElseThrow(()->new IllegalArgumentException("Unknown standard metal"));
                if(operation.startsWith("grant")) {
                    UUID id=UUID.randomUUID();
                    var art=operation.equals("grantferuchemy")?Art.FERUCHEMY:Art.ALLOMANCY;
                    var grant=new PowerGrant(id,art,metal,PowerGrant.Origin.ADMIN,id,DoubleArgumentType.getDouble(c,"strength"));
                    after=ReserveLedger.grant(before,grant);detail="Granted "+metal.id()+" "+art+"; grant/source "+id+(AllomancyConfig.SUPPORTED.contains(metal)?"":" (effect not implemented)");
                } else {
                    long amount=LongArgumentType.getLong(c,"micrograms");
                    after=ReserveLedger.credit(before,metal,amount);detail="Added "+amount+" micrograms of ordinary "+metal.id();
                }
            }
            player.setAttached(ModRegistries.SPIRITWEB,after);ModNetworking.send(player);
            String message=detail+" to "+player.getName().getString();
            c.getSource().sendSuccess(()->Component.literal(message),true);return 1;
        } catch(IllegalArgumentException e) {
            throw new SimpleCommandExceptionType(Component.literal(e.getMessage())).create();
        }
    }
    private static int inspect(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        var player=EntityArgument.getPlayer(c,"player");
        var state=player.getAttachedOrCreate(ModRegistries.SPIRITWEB);
        c.getSource().sendSuccess(()->Component.literal(player.getName().getString()+" | Identity "+state.identity()+" | valid="+ReserveLedger.valid(state)),false);
        if(AccountPowers.enabled(player.getUUID(),dev.metallicarts.MetallicArts.config.enableAccountPowers()))c.getSource().sendSuccess(()->Component.literal("Documented account access: all standard Allomancy/Feruchemy, strength1 baseline; costs apply; enableAccountPowers controls access"),false);
        for(var grant:state.grants())c.getSource().sendSuccess(()->Component.literal(
            grant.id()+" | "+grant.art()+" "+grant.metal().id()+" strength="+grant.strength()+" origin="+grant.origin()+" source="+grant.source()),false);
        for(Metal metal:Metal.values())if(state.reserves().getOrDefault(metal,0L)>0) {
            long total=state.reserves().get(metal), invested=ReserveLedger.investedMass(state,metal);
            c.getSource().sendSuccess(()->Component.literal(metal.id()+" total="+total+" invested="+invested+" ordinary="+(total-invested)+" micrograms"),false);
        }
        return 1;
    }
    private AllomancyCommands(){}
}
