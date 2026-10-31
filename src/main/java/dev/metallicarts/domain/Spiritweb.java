package dev.metallicarts.domain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import java.util.*;

/** Immutable, versioned authoritative state. Reserve units are micrograms, not Investiture. */
public record Spiritweb(int schema, UUID identity, List<PowerGrant> grants, Map<Metal, Long> reserves, List<MetalItemData> investedReserves) {
    public static final int VERSION = 1;
    public static final long MAX_RESERVE = 1_000_000_000L;
    public static final Codec<Spiritweb> CODEC = RecordCodecBuilder.<Spiritweb>create(i -> i.group(
        Codec.intRange(VERSION, VERSION).fieldOf("schema").forGetter(Spiritweb::schema),
        UUIDUtil.CODEC.fieldOf("identity").forGetter(Spiritweb::identity),
        PowerGrant.CODEC.listOf(0, 128).fieldOf("grants").forGetter(Spiritweb::grants),
        Codec.unboundedMap(Metal.CODEC, DomainCodecs.longRange(0, MAX_RESERVE)).fieldOf("reserves").forGetter(Spiritweb::reserves),
        MetalItemData.CODEC.listOf(0, 128).optionalFieldOf("invested_reserves", List.of()).forGetter(Spiritweb::investedReserves)
    ).apply(i, Spiritweb::new)).validate(s -> dev.metallicarts.allomancy.ReserveLedger.valid(s)
        ? DataResult.success(s) : DataResult.error(() -> "Inconsistent spiritweb grants or invested reserve subset"));

    public Spiritweb {
        Objects.requireNonNull(identity);
        grants = List.copyOf(grants); reserves = Map.copyOf(reserves); investedReserves = List.copyOf(investedReserves);
        if (investedReserves.size() > 128) throw new IllegalArgumentException("Too many invested reserves");
        if (schema != VERSION || grants.size() > 128) throw new IllegalArgumentException("Unsupported spiritweb");
        if (reserves.values().stream().anyMatch(v -> v < 0 || v > MAX_RESERVE)) throw new IllegalArgumentException("Invalid reserve");
    }
    public Spiritweb(int schema, UUID identity, List<PowerGrant> grants, Map<Metal, Long> reserves) { this(schema, identity, grants, reserves, List.of()); }
    public static Spiritweb empty() { return new Spiritweb(VERSION, UUID.randomUUID(), List.of(), Map.of()); }
    public Spiritweb withoutSource(UUID source) {
        return new Spiritweb(schema, identity, grants.stream().filter(g -> !g.source().equals(source)).toList(), reserves, investedReserves);
    }
    /** Phase 5 policy: distinct Allomantic spike sources add to the strongest non-spike base. */
    public double strength(Art art, Metal metal) {
        var applicable=grants.stream().filter(g->g.art()==art&&g.metal()==metal).toList();
        if(art==Art.FERUCHEMY)return applicable.stream().mapToDouble(PowerGrant::strength).max().orElse(0);
        double base=applicable.stream().filter(g->g.origin()!=PowerGrant.Origin.HEMALURGIC).mapToDouble(PowerGrant::strength).max().orElse(0);
        var sources=new HashMap<UUID,Double>();
        for(var g:applicable)if(g.origin()==PowerGrant.Origin.HEMALURGIC)sources.merge(g.source(),g.strength(),Math::max);
        return Math.min(1000,base+sources.values().stream().mapToDouble(Double::doubleValue).sum());
    }
}
