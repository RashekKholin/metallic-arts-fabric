package dev.metallicarts.registry;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.domain.*;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import java.util.Map;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ModRegistries {
    /** Shared closed catalog; not a dynamic Minecraft registry. Unknown IDs fail decoding. */
    public static final Map<String, Metal> METALS = Arrays.stream(Metal.values()).collect(Collectors.toUnmodifiableMap(Metal::id, Function.identity()));
    public static final AttachmentType<Spiritweb> SPIRITWEB = AttachmentRegistry.create(MetallicArts.id("spiritweb"),
        b -> b.initializer(Spiritweb::empty).persistent(Spiritweb.CODEC).copyOnDeath());
    public static final DataComponentType<MetalItemData> METAL_DATA = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE, MetallicArts.id("metal_data"),
        DataComponentType.<MetalItemData>builder().persistent(MetalItemData.CODEC)
            .networkSynchronized(ByteBufCodecs.fromCodec(MetalItemData.CODEC)).build());
    public static final AttachmentType<dev.metallicarts.equipment.ContactEquipment> CONTACT_EQUIPMENT=AttachmentRegistry.create(MetallicArts.id("contact_equipment"),
        b->b.initializer(dev.metallicarts.equipment.ContactEquipment::empty).persistent(dev.metallicarts.equipment.ContactEquipment.CODEC).copyOnDeath());
    public static final DataComponentType<dev.metallicarts.feruchemy.MetalmindData> METALMIND=Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,MetallicArts.id("metalmind"),
        DataComponentType.<dev.metallicarts.feruchemy.MetalmindData>builder().persistent(dev.metallicarts.feruchemy.MetalmindData.CODEC)
            .networkSynchronized(ByteBufCodecs.fromCodec(dev.metallicarts.feruchemy.MetalmindData.CODEC)).build());
    public static final AttachmentType<java.util.UUID> PHYSICAL_CARGO=AttachmentRegistry.create(MetallicArts.id("physical_cargo"),
        b->b.persistent(net.minecraft.core.UUIDUtil.CODEC));
    public static final AttachmentType<dev.metallicarts.equipment.ContactEquipment> SPIKES=AttachmentRegistry.create(MetallicArts.id("implanted_spikes"),
        b->b.initializer(dev.metallicarts.equipment.ContactEquipment::empty).persistent(dev.metallicarts.equipment.ContactEquipment.CODEC).copyOnDeath());
    public static final DataComponentType<dev.metallicarts.hemalurgy.SpikeData> SPIKE=Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,MetallicArts.id("spike"),
        DataComponentType.<dev.metallicarts.hemalurgy.SpikeData>builder().persistent(dev.metallicarts.hemalurgy.SpikeData.CODEC)
            .networkSynchronized(ByteBufCodecs.fromCodec(dev.metallicarts.hemalurgy.SpikeData.CODEC)).build());
    public static void initialize() { MetallicArts.LOGGER.info("Registered {} base metal IDs, spiritweb attachment and metal component", METALS.size()); }
    private ModRegistries() {}
}
