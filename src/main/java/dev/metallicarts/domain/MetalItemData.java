package dev.metallicarts.domain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Component foundation only: no charged item creation, transfer or tapping mechanics. */
public record MetalItemData(int schema, UUID instance, Metal metal, long massMicrograms,
                            double purity, long charge, Optional<UUID> identity) {
    public static final Codec<MetalItemData> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.intRange(1, 1).fieldOf("schema").forGetter(MetalItemData::schema),
        UUIDUtil.CODEC.fieldOf("instance").forGetter(MetalItemData::instance),
        Metal.CODEC.fieldOf("metal").forGetter(MetalItemData::metal),
        DomainCodecs.longRange(1, 1_000_000_000_000L).fieldOf("mass_micrograms").forGetter(MetalItemData::massMicrograms),
        Codec.doubleRange(0, 1).fieldOf("purity").forGetter(MetalItemData::purity),
        DomainCodecs.longRange(0, Long.MAX_VALUE).fieldOf("charge").forGetter(MetalItemData::charge),
        UUIDUtil.CODEC.optionalFieldOf("identity").forGetter(MetalItemData::identity)
    ).apply(i, MetalItemData::new));
    public MetalItemData {
        Objects.requireNonNull(instance); Objects.requireNonNull(metal); Objects.requireNonNull(identity);
        if (schema != 1 || massMicrograms <= 0 || massMicrograms > 1_000_000_000_000L ||
            !Double.isFinite(purity) || purity < 0 || purity > 1 || charge < 0) throw new IllegalArgumentException("Invalid metal item data");
    }
}
