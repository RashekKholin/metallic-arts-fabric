package dev.metallicarts.hemalurgy;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.allomancy.ReserveLedger;
import dev.metallicarts.domain.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import java.util.*;

/** Explicit donor setup for development; does not pretend that natural powered-NPC progression exists. */
public final class HemalurgyCommands {
    private static final ResourceKey<Item> BLOOD_KEY=ResourceKey.create(Registries.ITEM,MetallicArts.id("blood_vial"));
    public static final Item BLOOD_VIAL=Registry.register(BuiltInRegistries.ITEM,BLOOD_KEY,new Item(new Item.Properties().setId(BLOOD_KEY).stacksTo(16)));
    public static void initialize(){
        CommandRegistrationCallback.EVENT.register((dispatcher,registry,environment)->dispatcher.register(
            Commands.literal("metallicarts").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("grantdonor").then(Commands.argument("donor",EntityArgument.entity())
                    .then(Commands.argument("art",StringArgumentType.word()).suggests((c,b)->{b.suggest("allomancy");b.suggest("feruchemy");return b.buildFuture();})
                        .then(Commands.argument("metal",StringArgumentType.word()).suggests((c,b)->{for(var m:Metal.values())b.suggest(m.id());return b.buildFuture();})
                            .then(Commands.argument("strength",DoubleArgumentType.doubleArg(.000001,1000)).executes(c->{
                                var entity=EntityArgument.getEntity(c,"donor");
                                if(!(entity instanceof LivingEntity living))throw new SimpleCommandExceptionType(Component.literal("Donor must be living")).create();
                                try{
                                    Art art=Art.valueOf(StringArgumentType.getString(c,"art").toUpperCase(Locale.ROOT));
                                    if(art!=Art.ALLOMANCY&&art!=Art.FERUCHEMY)throw new IllegalArgumentException("Use allomancy or feruchemy");
                                    Metal metal=Arrays.stream(Metal.values()).filter(m->m.id().equals(StringArgumentType.getString(c,"metal"))).findFirst().orElseThrow();
                                    UUID id=UUID.randomUUID();var state=living.getAttachedOrCreate(ModRegistries.SPIRITWEB);
                                    living.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.grant(state,new PowerGrant(id,art,metal,PowerGrant.Origin.ADMIN,id,DoubleArgumentType.getDouble(c,"strength"))));
                                    c.getSource().sendSuccess(()->Component.literal("Development donor grant "+id+"; later power effects may be unavailable"),true);return 1;
                                }catch(IllegalArgumentException|NoSuchElementException e){throw new SimpleCommandExceptionType(Component.literal("Invalid art/metal or grant capacity")).create();}
                            }))))))
        ));
    }
    private HemalurgyCommands(){}
}
