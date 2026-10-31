package dev.metallicarts.economy;

import dev.metallicarts.domain.Metal;
import java.util.*;

/** Integer bookkeeping units. One ingot = 9 nuggets = 900 portions. */
public final class Materials {
    public static final long PORTION_MASS = 10_000L;
    public static final List<String> ALL = java.util.stream.Stream.concat(Arrays.stream(Metal.values()).map(Metal::id),
        java.util.stream.Stream.of("silver", "nickel", "lead", "bismuth", "carbon", "silicon")).toList();
    public static Optional<Metal> metal(String id) { return Arrays.stream(Metal.values()).filter(m -> m.id().equals(id)).findFirst(); }
    private Materials() {}
}
