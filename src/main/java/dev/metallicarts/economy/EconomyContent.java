package dev.metallicarts.economy;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.domain.Metal;
import dev.metallicarts.metallurgy.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import java.util.*;
import java.util.function.Function;

public final class EconomyContent {
    public record Form(String material, String kind, int units) {}
    public static final Map<String, Item> ITEMS = new LinkedHashMap<>();
    public static final Map<Item, Form> FORMS = new IdentityHashMap<>();
    public static final Map<String, Block> ORES = new LinkedHashMap<>();
    public static final DataComponentType<BatchComposition> COMPOSITION = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
        MetallicArts.id("composition"), DataComponentType.<BatchComposition>builder().persistent(BatchComposition.CODEC)
            .networkSynchronized(ByteBufCodecs.fromCodec(BatchComposition.CODEC)).build());
    public static MetallurgyBlock TABLE;
    public static BlockEntityType<MetallurgyBlockEntity> TABLE_ENTITY;
    public static ExtendedMenuType<MetallurgyMenu, BlockPos> TABLE_MENU;
    public static Item SLAG;

    public static void initialize() {
        for (String material : Materials.ALL) {
            Item ingot = switch (material) { case "iron" -> Items.IRON_INGOT; case "copper" -> Items.COPPER_INGOT; case "gold" -> Items.GOLD_INGOT; default -> add(material + "_ingot", 64, MaterialItem::new); };
            Item nugget = switch (material) { case "iron" -> Items.IRON_NUGGET; case "copper" -> Items.COPPER_NUGGET; case "gold" -> Items.GOLD_NUGGET; default -> add(material + "_nugget", 64, MaterialItem::new); };
            FORMS.put(ingot, new Form(material, "ingot", 900));
            FORMS.put(nugget, new Form(material, "nugget", 100));
            for (String kind : List.of("portion", "flakes")) {
                Item item = add(material + "_" + kind, 99, MaterialItem::new);
                FORMS.put(item, new Form(material, kind, 1));
            }
            if (Materials.metal(material).isPresent()) {
                for (String kind : List.of("coin", "ring", "bracelet", "bracer", "spike")) {
                    Item item = add(material + "_" + kind, kind.equals("coin") ? 64 : 1, MaterialItem::new);
                    FORMS.put(item, new Form(material, kind, kind.equals("coin") ? 10 : 900));
                }
                Metal metal = Materials.metal(material).orElseThrow();
                add(material + "_vial", 1, p -> new MetalVialItem(p, metal));
            }
        }
        SLAG = add("invalid_alloy", 99, MaterialItem::new);
        for (String ore : List.of("tin", "zinc", "silver", "bauxite", "nickel", "chromite", "lead", "bismuth")) {
            add("raw_" + ore, 64, MaterialItem::new);
            add(ore + "_concentrate",64,MaterialItem::new);
            for (boolean deep : List.of(false, true)) {
                String name = (deep ? "deepslate_" : "") + ore + "_ore";
                Block block = addBlock(name, p -> new DropExperienceBlock(net.minecraft.util.valueproviders.UniformInt.of(0, 2), p),
                    BlockBehaviour.Properties.ofFullCopy(deep ? Blocks.DEEPSLATE_IRON_ORE : Blocks.IRON_ORE));
                ORES.put(name, block);
            }
        }
        for (String material : List.of("iron","copper","gold")) add(material + "_concentrate",64,MaterialItem::new);
        TABLE = (MetallurgyBlock) addBlock("metallurgy_table", MetallurgyBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SMITHING_TABLE).strength(3.5F).requiresCorrectToolForDrops());
        TABLE_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, MetallicArts.id("metallurgy_table"),
            FabricBlockEntityTypeBuilder.create(MetallurgyBlockEntity::new, TABLE).build());
        TABLE_MENU = Registry.register(BuiltInRegistries.MENU, MetallicArts.id("metallurgy_table"),
            new ExtendedMenuType<>(MetallurgyMenu::new, BlockPos.STREAM_CODEC));
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, MetallicArts.id("materials"), FabricCreativeModeTab.builder()
            .title(Component.literal("The Metallic Arts")).icon(() -> new ItemStack(TABLE))
            .displayItems((parameters, output) -> ITEMS.values().forEach(item -> { if(item instanceof MetalVialItem vial) output.accept(vial.prepared()); else output.accept(item); })).build());
    }
    public static Item add(String name, int max, Function<Item.Properties, Item> factory) {
        var key = ResourceKey.create(Registries.ITEM, MetallicArts.id(name));
        Item item = Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key).stacksTo(max)));
        ITEMS.put(name, item); return item;
    }
    private static Block addBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        var key = ResourceKey.create(Registries.BLOCK, MetallicArts.id(name));
        Block block = Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
        add(name, 64, p -> new BlockItem(block, p.useBlockDescriptionPrefix()));
        return block;
    }
    public static Form form(Item item) { return FORMS.get(item); }
    public static Item item(String material, String kind) {
        if (kind.equals("ingot") || kind.equals("nugget"))
            return FORMS.entrySet().stream().filter(e -> e.getValue().material().equals(material) && e.getValue().kind().equals(kind)).findFirst().orElseThrow().getKey();
        return Objects.requireNonNull(ITEMS.get(material + "_" + kind));
    }
    public static boolean flakes(ItemStack stack) { Form form = form(stack.getItem()); return form != null && form.kind().equals("flakes") && Materials.metal(form.material()).isPresent(); }
    private EconomyContent() {}
}
