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
        dev.metallicarts.config.AdvancedConfig.load();
        dev.metallicarts.config.ProgressionConfig.load();
        LOGGER.info("Documented account power access: {} (RashekKholin, The17thShard); config enableAccountPowers controls this perk",config.enableAccountPowers());
        ModRegistries.initialize();
        EconomyContent.initialize();
        MetallurgyRecipes.initialize();
        EconomyWorld.initialize();
        LOGGER.info("Loaded {} provenance rules", RuleRegistry.load().size());
        ModNetworking.initialize();
        dev.metallicarts.allomancy.AllomancyCommands.initialize();
        dev.metallicarts.arts.AdvancedFeruchemy.initialize();
        dev.metallicarts.feruchemy.FeruchemyRuntime.initialize();
        dev.metallicarts.hemalurgy.HemalurgyRuntime.initialize();
        dev.metallicarts.arts.AdvancedAllomancy.initialize();
        dev.metallicarts.arts.MemoryCapture.initialize();
        dev.metallicarts.arts.Injuries.initialize();
        dev.metallicarts.arts.AdvancedSystems.initialize();
        dev.metallicarts.arts.SurvivalProgression.initialize();
        dev.metallicarts.arts.SurvivalCommands.initialize();
        dev.metallicarts.allomancy.AllomancyRuntime.initialize();
        LOGGER.info("The Metallic Arts initialized with the sixteen standard Allomantic and Feruchemical metals");
    }
}

