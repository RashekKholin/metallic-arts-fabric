package dev.metallicarts.domain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.function.Function;

public final class DomainCodecs {
    public static Codec<Long> longRange(long min, long max) {
        return Codec.LONG.validate(v -> v >= min && v <= max ? DataResult.success(v)
            : DataResult.error(() -> "Value outside range " + min + ".." + max));
    }
    public static <E extends Enum<E>> Codec<E> enumeration(E[] values, Function<E, String> id) {
        return Codec.STRING.comapFlatMap(s -> Arrays.stream(values).filter(e -> id.apply(e).equals(s)).findFirst()
            .map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown enum ID: " + s)), id);
    }
    private DomainCodecs() {}
}
