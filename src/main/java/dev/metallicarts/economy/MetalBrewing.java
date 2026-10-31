package dev.metallicarts.economy;

import dev.metallicarts.domain.*;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import java.util.*;

/** Vanilla owns fuel, the 400-tick timer, interruption and save/reload. This owns material conversion. */
public final class MetalBrewing {
    public static boolean water(ItemStack stack) {
        return stack.is(Items.POTION) && stack.getCount() == 1 &&
            stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER);
    }
    public static boolean canBrew(NonNullList<ItemStack> items) {
        ItemStack flakes = items.get(3);
        if (!EconomyContent.flakes(flakes)) return false;
        var batch = flakes.get(EconomyContent.COMPOSITION);
        if (batch != null && !batch.viable()) return false;
        var data = flakes.get(ModRegistries.METAL_DATA);
        if (data != null && (data.purity() < 0.999999 || data.massMicrograms() != Materials.PORTION_MASS ||
            !data.metal().id().equals(EconomyContent.form(flakes.getItem()).material()) || (Ingestion.invested(data) && flakes.getCount() != 1))) return false;
        for (int i = 0; i < 3; i++) if (water(items.get(i))) return true;
        return false;
    }
    public static void brew(NonNullList<ItemStack> items) {
        if (!canBrew(items)) return;
        ItemStack flakes = items.get(3);
        Metal metal = Materials.metal(EconomyContent.form(flakes.getItem()).material()).orElseThrow();
        for (int i = 0; i < 3 && !flakes.isEmpty(); i++) if (water(items.get(i))) {
            ItemStack vial = new ItemStack(EconomyContent.item(metal.id(), "vial"));
            var original = flakes.get(ModRegistries.METAL_DATA);
            var data = original == null ? new MetalItemData(1, UUID.randomUUID(), metal, Materials.PORTION_MASS, 1, 0, Optional.empty()) : original;
            vial.set(ModRegistries.METAL_DATA, data);
            if (flakes.has(EconomyContent.COMPOSITION)) vial.set(EconomyContent.COMPOSITION, flakes.get(EconomyContent.COMPOSITION));
            items.set(i, vial);
            flakes.shrink(1);
        }
    }
    private MetalBrewing() {}
}
