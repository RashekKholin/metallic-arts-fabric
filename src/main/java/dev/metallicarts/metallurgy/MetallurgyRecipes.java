package dev.metallicarts.metallurgy;

import com.google.gson.*;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.economy.*;
import dev.metallicarts.domain.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.packs.resources.ResourceManager;
import java.io.*;
import java.util.*;

/** Server-only reloadable recipes. UI receives a ghost preview, never trusts client recipe content. */
public final class MetallurgyRecipes {
    public record Ingredient(String item, String tag, int count) {
        public boolean matches(ItemStack stack) {
            return !stack.isEmpty() && (item != null ? BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(item)
                : stack.is(TagKey.create(Registries.ITEM, Identifier.parse(tag))));
        }
    }
    public record Output(String item, int count) {}
    public record Recipe(String id, int tab, int ticks, List<Ingredient> inputs, List<Output> outputs, String alloy, Map<String, Integer> ratio, boolean slice) {
        public boolean kindsMatch(List<ItemStack> input) {
            return input.stream().filter(s -> !s.isEmpty()).allMatch(s -> inputs.stream().anyMatch(i -> i.matches(s)))
                && inputs.stream().allMatch(i -> input.stream().anyMatch(i::matches))
                && (!slice || input.stream().anyMatch(s -> s.has(ModRegistries.METAL_DATA) && Ingestion.invested(s.get(ModRegistries.METAL_DATA))));
        }
    }
    public record Plan(int[] consumed, List<ItemStack> outputs, int ticks, Map<Integer,ItemStack> replacements) {
        public Plan(int[] consumed,List<ItemStack> outputs,int ticks) { this(consumed,outputs,ticks,Map.of()); }
    }
    private static List<Recipe> recipes = List.of();
    public static List<Recipe> candidates(int tab, List<ItemStack> inputs) {
        return recipes.stream().filter(r -> r.tab == tab && r.kindsMatch(inputs)).toList();
    }
    public static List<Recipe> all() { return recipes; }
    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> reload(server.getResourceManager()));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, manager, success) -> { if (success) reload(manager); });
    }
    public static void reload(ResourceManager resources) {
        Map<String, Recipe> loaded = new TreeMap<>();
        try {
            for (var resource : resources.listResources("metallurgy", id -> id.getPath().endsWith(".json")).entrySet()) {
                try (Reader reader = resource.getValue().openAsReader()) {
                    for (Recipe recipe : new Gson().fromJson(reader, Recipe[].class)) {
                        validate(recipe);
                        if (loaded.putIfAbsent(recipe.id, recipe) != null) throw new IllegalArgumentException("Duplicate recipe " + recipe.id);
                    }
                }
            }
            if (loaded.isEmpty()) throw new IllegalArgumentException("Missing metallurgy recipes");
            recipes = List.copyOf(loaded.values());
            MetallicArts.LOGGER.info("Loaded {} metallurgy recipes", recipes.size());
        } catch (IOException | RuntimeException e) {
            if (recipes.isEmpty()) throw new IllegalStateException("Cannot initialize metallurgy recipes", e);
            MetallicArts.LOGGER.error("Rejected metallurgy reload; keeping previous valid catalog", e);
        }
    }
    private static void validate(Recipe recipe) {
        if (recipe.id == null || recipe.tab < 0 || recipe.tab > 2 || recipe.ticks < 1 || recipe.ticks > 24000 || recipe.inputs == null || recipe.inputs.isEmpty() || recipe.inputs.size() > 4)
            throw new IllegalArgumentException("Invalid recipe");
        for (var i : recipe.inputs) if ((i.item == null) == (i.tag == null) || i.count < 1 || i.count > 396 || i.item != null && !BuiltInRegistries.ITEM.containsKey(Identifier.parse(i.item)))
            throw new IllegalArgumentException("Invalid input in " + recipe.id);
        if (recipe.alloy != null) {
            if (recipe.tab != 1 || Materials.metal(recipe.alloy).isEmpty() || recipe.ratio == null || recipe.ratio.size() > 4 ||
                recipe.ratio.values().stream().anyMatch(n -> n <= 0) || recipe.ratio.values().stream().mapToInt(n -> n).sum() != 100)
                throw new IllegalArgumentException("Invalid alloy formula " + recipe.id);
        } else if (recipe.outputs == null || recipe.outputs.isEmpty() || recipe.outputs.size() > 4 ||
            recipe.outputs.stream().anyMatch(o -> o.count < 1 || o.count > 396 || !BuiltInRegistries.ITEM.containsKey(Identifier.parse(o.item))))
            throw new IllegalArgumentException("Invalid output in " + recipe.id);
        if(recipe.slice && (recipe.tab!=2 || recipe.inputs.size()!=1 || recipe.inputs.getFirst().count!=1 ||
            recipe.inputs.getFirst().item==null || recipe.outputs.size()!=1 || recipe.outputs.getFirst().count!=1))
            throw new IllegalArgumentException("Invested slicing requires a single-item input and output");
    }
    public static Plan plan(Recipe recipe, List<ItemStack> inputs) {
        if(inputs.stream().anyMatch(s->s.has(ModRegistries.METALMIND)||s.has(ModRegistries.SPIKE)))return null;
        if (!recipe.kindsMatch(inputs)) return null;
        if (recipe.slice) return slicePlan(recipe,inputs);
        if (recipe.alloy != null) return alloyPlan(recipe, inputs);
        int[] used = new int[4];
        for (var ingredient : recipe.inputs) {
            int left = ingredient.count;
            for (int slot = 0; slot < 4 && left > 0; slot++) if (ingredient.matches(inputs.get(slot))) {
                int take = Math.min(left, inputs.get(slot).getCount() - used[slot]); used[slot] += take; left -= take;
            }
            if (left > 0) return null;
        }
        List<ItemStack> out = new ArrayList<>();
        for (Output result : recipe.outputs) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(result.item));
            split(out, new ItemStack(item), result.count);
        }
        var sourceStacks = new ArrayList<ItemStack>();
        for (int i = 0; i < 4; i++) if (used[i] > 0) sourceStacks.add(inputs.get(i));
        var firstData = sourceStacks.getFirst().get(ModRegistries.METAL_DATA);
        var firstBatch = sourceStacks.getFirst().get(EconomyContent.COMPOSITION);
        if(sourceStacks.stream().anyMatch(s -> s.has(ModRegistries.METAL_DATA) &&
            (EconomyContent.form(s.getItem())==null || s.get(ModRegistries.METAL_DATA).massMicrograms()!=EconomyContent.form(s.getItem()).units()*Materials.PORTION_MASS))) return null;
        // Do not launder mixed qualities, Identity or charge by combining mismatched source batches.
        if (sourceStacks.stream().anyMatch(s -> !Objects.equals(s.get(EconomyContent.COMPOSITION), firstBatch) ||
            !Objects.equals(s.get(ModRegistries.METAL_DATA), firstData))) return null;
        boolean invested = firstData != null && Ingestion.invested(firstData);
        if (invested && (Arrays.stream(used).sum() != 1 || out.size() != 1 || out.getFirst().getCount() != 1)) return null;
        for (ItemStack result : out) {
            var form = EconomyContent.form(result.getItem());
            if ((firstBatch != null || firstData != null) && form == null) return null;
            if (firstBatch != null) result.set(EconomyContent.COMPOSITION, firstBatch);
            if (firstData != null && (!firstData.metal().id().equals(form.material()) ||
                invested && firstData.massMicrograms() != form.units() * Materials.PORTION_MASS)) return null;
            if (form != null && Materials.metal(form.material()).isPresent() && (firstData != null || List.of("ring","bracelet","bracer","earring","spike").contains(form.kind()))) {
                result.set(ModRegistries.METAL_DATA, invested ? firstData : new MetalItemData(1, UUID.randomUUID(),
                    Materials.metal(form.material()).orElseThrow(), form.units() * Materials.PORTION_MASS,
                    firstData == null ? firstBatch == null ? 1 : firstBatch.purity() : firstData.purity(), 0, Optional.empty()));
                if (invested) result.set(DataComponents.MAX_STACK_SIZE, 1);
            }
        }
        return out.size() <= 4 ? new Plan(used, List.copyOf(out), recipe.ticks) : null;
    }
    private static Plan alloyPlan(Recipe recipe, List<ItemStack> inputs) {
        Map<String, Integer> actual = new TreeMap<>(); int[] used = new int[4];
        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = inputs.get(slot); if (stack.isEmpty()) continue;
            var form = EconomyContent.form(stack.getItem());
            // Only clean measured elemental portions enter a new alloy batch.
            if (form == null || !form.kind().equals("portion") || stack.has(ModRegistries.METAL_DATA) || stack.has(EconomyContent.COMPOSITION)) return null;
            used[slot] = stack.getCount(); actual.merge(form.material(), used[slot], Integer::sum);
        }
        double purity = BatchComposition.purity(actual, recipe.ratio);
        BatchComposition batch = new BatchComposition(actual, purity);
        Item target = purity >= 0.95 ? EconomyContent.item(recipe.alloy, "portion") : EconomyContent.SLAG;
        ItemStack output = new ItemStack(target); output.set(EconomyContent.COMPOSITION, batch);
        List<ItemStack> out = new ArrayList<>();
        split(out, output, Arrays.stream(used).sum());
        return out.size() <= 4 ? new Plan(used, List.copyOf(out), recipe.ticks) : null;
    }
    private static Plan slicePlan(Recipe recipe,List<ItemStack> inputs) {
        int slot=-1;for(int i=0;i<4;i++)if(!inputs.get(i).isEmpty()){if(slot!=-1)return null;slot=i;}
        if(slot<0)return null;
        ItemStack original=inputs.get(slot);var data=original.get(ModRegistries.METAL_DATA);
        if(original.getCount()!=1||data==null||!Ingestion.invested(data)||data.massMicrograms()<Materials.PORTION_MASS ||
            !data.metal().id().equals(EconomyContent.form(original.getItem()).material())) return null;
        long charge=java.math.BigInteger.valueOf(data.charge()).multiply(java.math.BigInteger.valueOf(Materials.PORTION_MASS))
            .divide(java.math.BigInteger.valueOf(data.massMicrograms())).longValueExact();
        ItemStack flake=new ItemStack(EconomyContent.item(data.metal().id(),"flakes"));
        flake.set(ModRegistries.METAL_DATA,new MetalItemData(1,UUID.randomUUID(),data.metal(),Materials.PORTION_MASS,data.purity(),charge,data.identity()));
        flake.set(DataComponents.MAX_STACK_SIZE,1);
        if(original.has(EconomyContent.COMPOSITION))flake.set(EconomyContent.COMPOSITION,original.get(EconomyContent.COMPOSITION));
        int[] used=new int[4];Map<Integer,ItemStack> replacements=new HashMap<>();
        if(data.massMicrograms()==Materials.PORTION_MASS) used[slot]=1;
        else{
            ItemStack remainder=original.copyWithCount(1);
            remainder.set(ModRegistries.METAL_DATA,new MetalItemData(1,data.instance(),data.metal(),data.massMicrograms()-Materials.PORTION_MASS,data.purity(),data.charge()-charge,data.identity()));
            remainder.set(DataComponents.MAX_STACK_SIZE,1);replacements.put(slot,remainder);
        }
        return new Plan(used,List.of(flake),recipe.ticks,Map.copyOf(replacements));
    }
    private static void split(List<ItemStack> out, ItemStack template, int count) {
        while (count > 0) { int n = Math.min(count, template.getMaxStackSize()); out.add(template.copyWithCount(n)); count -= n; }
    }
    private MetallurgyRecipes() {}
}
