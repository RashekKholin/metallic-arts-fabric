package dev.metallicarts.economy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.domain.*;
import java.util.*;

/** Ratios, not extra mass: the item form defines the mass per item. */
public record BatchComposition(Map<String, Integer> parts, double purity) {
    public static final Codec<BatchComposition> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.unboundedMap(Codec.STRING, Codec.intRange(1, 396)).fieldOf("parts").forGetter(BatchComposition::parts),
        Codec.doubleRange(0, 1).fieldOf("purity").forGetter(BatchComposition::purity)
    ).apply(i, BatchComposition::new));
    public BatchComposition { parts = Map.copyOf(parts); if (parts.isEmpty() || parts.size() > 4 || !Double.isFinite(purity) || purity < 0 || purity > 1) throw new IllegalArgumentException("Invalid composition"); }
    public static double purity(Map<String, Integer> actual, Map<String, Integer> target) {
        double a = actual.values().stream().mapToInt(v -> v).sum();
        double t = target.values().stream().mapToInt(v -> v).sum();
        if (a <= 0 || t <= 0 || !actual.keySet().equals(target.keySet())) return 0;
        double distance = actual.keySet().stream().mapToDouble(k -> Math.abs(actual.get(k) / a - target.get(k) / t)).sum() / 2;
        return Math.max(0, 1 - distance);
    }
    public boolean viable() { return purity >= 0.999999; }
    public String quality() { return viable() ? "Viable" : purity >= 0.95 ? "Imperfect" : "Invalid"; }
}
