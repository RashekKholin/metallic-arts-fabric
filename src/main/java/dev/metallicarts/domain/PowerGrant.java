package dev.metallicarts.domain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import java.util.Objects;
import java.util.UUID;

/** A capability, not an active effect. External ownership always keeps its source identity. */
public record PowerGrant(UUID id, Art art, Metal metal, Origin origin, UUID source, double strength) {
    public enum Origin { INNATE, HEMALURGIC, UNSEALED, ADMIN }
    public static final Codec<PowerGrant> CODEC = RecordCodecBuilder.create(i -> i.group(
        UUIDUtil.CODEC.fieldOf("id").forGetter(PowerGrant::id),
        Art.CODEC.fieldOf("art").forGetter(PowerGrant::art),
        Metal.CODEC.fieldOf("metal").forGetter(PowerGrant::metal),
        DomainCodecs.enumeration(Origin.values(), Origin::name).fieldOf("origin").forGetter(PowerGrant::origin),
        UUIDUtil.CODEC.fieldOf("source").forGetter(PowerGrant::source),
        Codec.doubleRange(0.000000001, 1000).fieldOf("strength").forGetter(PowerGrant::strength)
    ).apply(i, PowerGrant::new));
    public PowerGrant {
        Objects.requireNonNull(id); Objects.requireNonNull(art); Objects.requireNonNull(metal);
        Objects.requireNonNull(origin); Objects.requireNonNull(source);
        if (!Double.isFinite(strength) || strength <= 0 || strength > 1000) throw new IllegalArgumentException("Invalid strength");
    }
}
