package dev.metallicarts.domain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import java.util.*;

/** Immutable, versioned authoritative state. Reserve units are micrograms, not Investiture. */
public record Spiritweb(int schema, UUID identity, List<PowerGrant> grants, Map<Metal, Long> reserves) {
    public static final int VERSION = 1;
    public static final long MAX_RESERVE = 1_000_000_000L;
    public static final Codec<Spiritweb> CODEC = RecordCodecBuilder.<Spiritweb>create(i -> i.group(
        Codec.intRange(VERSION, VERSION).fieldOf("schema").forGetter(Spiritweb::schema),
        UUIDUtil.CODEC.fieldOf("identity").forGetter(Spiritweb::identity),
        PowerGrant.CODEC.listOf(0, 128).fieldOf("grants").forGetter(Spiritweb::grants),
        Codec.unboundedMap(Metal.CODEC, DomainCodecs.longRange(0, MAX_RESERVE)).fieldOf("reserves").forGetter(Spiritweb::reserves)
    ).apply(i, Spiritweb::new)).validate(s -> s.grants.stream().map(PowerGrant::id).distinct().count() == s.grants.size()
        ? DataResult.success(s) : DataResult.error(() -> "Duplicate power grant IDs"));

    public Spiritweb {
        Objects.requireNonNull(identity);
        grants = List.copyOf(grants); reserves = Map.copyOf(reserves);
        if (schema != VERSION || grants.size() > 128) throw new IllegalArgumentException("Unsupported spiritweb");
        if (reserves.values().stream().anyMatch(v -> v < 0 || v > MAX_RESERVE)) throw new IllegalArgumentException("Invalid reserve");
    }
    public static Spiritweb empty() { return new Spiritweb(VERSION, UUID.randomUUID(), List.of(), Map.of()); }
    public Spiritweb withoutSource(UUID source) {
        return new Spiritweb(schema, identity, grants.stream().filter(g -> !g.source().equals(source)).toList(), reserves);
    }
    /** Phase 1 interpretation: strongest grant wins. No additive power multiplication yet. */
    public double strength(Art art, Metal metal) {
        return grants.stream().filter(g -> g.art() == art && g.metal() == metal).mapToDouble(PowerGrant::strength).max().orElse(0);
    }
}
