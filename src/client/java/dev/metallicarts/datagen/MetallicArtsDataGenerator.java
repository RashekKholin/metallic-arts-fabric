package dev.metallicarts.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/** Provider pack ready for Phase 2 recipes, tags, models and loot tables. No fake outputs. */
public final class MetallicArtsDataGenerator implements DataGeneratorEntrypoint {
    @Override public void onInitializeDataGenerator(FabricDataGenerator generator) { generator.createPack(); }
}
