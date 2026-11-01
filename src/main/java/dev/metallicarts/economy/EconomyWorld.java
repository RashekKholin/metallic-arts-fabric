package dev.metallicarts.economy;

import com.google.common.collect.ImmutableSet;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.metallurgy.MetallurgyBlock;
import net.fabricmc.fabric.api.biome.v1.*;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.item.trading.TradeSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.*;

public final class EconomyWorld {
    public static final List<ResourceKey<VillagerProfession>> PROFESSIONS = new ArrayList<>();
    public static void initialize() {
        Registry.register(BuiltInRegistries.FEATURE,MetallicArts.id("research_camp"),new ResearchCampFeature());
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),GenerationStep.Decoration.SURFACE_STRUCTURES,ResourceKey.create(Registries.PLACED_FEATURE,MetallicArts.id("research_camp")));
        for (String ore : List.of("tin","zinc","silver","bauxite","nickel","chromite","lead","bismuth")) {
            ResourceKey<PlacedFeature> feature = ResourceKey.create(Registries.PLACED_FEATURE, MetallicArts.id(ore + "_ore"));
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),GenerationStep.Decoration.UNDERGROUND_ORES,feature);
        }
        String[] names={"metallurgist","alloy_metallurgist","rare_metallurgist","equipment_metallurgist"};
        for (int specialty=0;specialty<4;specialty++) {
            String name=names[specialty]; int n=specialty;
            var states=EconomyContent.TABLE.getStateDefinition().getPossibleStates().stream().filter(s -> s.getValue(MetallurgyBlock.SPECIALTY)==n).collect(java.util.stream.Collectors.toSet());
            ResourceKey<PoiType> poi=ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE,MetallicArts.id(name));
            PoiHelper.register(poi.identifier(),1,1,states);
            var trades=new Int2ObjectOpenHashMap<ResourceKey<TradeSet>>();
            for (int level=1;level<=5;level++) trades.put(level,ResourceKey.create(Registries.TRADE_SET,MetallicArts.id(name+"/level_"+level)));
            ResourceKey<VillagerProfession> key=ResourceKey.create(Registries.VILLAGER_PROFESSION,MetallicArts.id(name));
            Registry.register(BuiltInRegistries.VILLAGER_PROFESSION,key,new VillagerProfession(
                Component.translatable("entity.minecraft.villager.metallicarts."+name),holder -> holder.is(poi),holder -> holder.is(poi),
                ImmutableSet.of(),ImmutableSet.of(),SoundEvents.VILLAGER_WORK_TOOLSMITH,trades));
            PROFESSIONS.add(key);
        }
    }
    private EconomyWorld() {}
}
