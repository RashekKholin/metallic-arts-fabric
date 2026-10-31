package dev.metallicarts;

import dev.metallicarts.config.ServerConfig;
import dev.metallicarts.network.ModNetworking;
import dev.metallicarts.registry.ModRegistries;
import dev.metallicarts.rules.RuleRegistry;
import dev.metallicarts.economy.EconomyContent;
import dev.metallicarts.metallurgy.MetallurgyRecipes;
import dev.metallicarts.economy.EconomyWorld;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MetallicArts implements ModInitializer {
    public static final String ID = "metallicarts";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    public static ServerConfig config;
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(ID, path); }

    @Override public void onInitialize() {
        config = ServerConfig.load();
        ModRegistries.initialize();
        EconomyContent.initialize();
        MetallurgyRecipes.initialize();
        EconomyWorld.initialize();
        LOGGER.info("Loaded {} provenance rules", RuleRegistry.load().size());
        ModNetworking.initialize();
        dev.metallicarts.allomancy.AllomancyCommands.initialize();
        dev.metallicarts.allomancy.AllomancyRuntime.initialize();
        LOGGER.info("The Metallic Arts initialized with Phase 3 development Allomancy");
    }
}
