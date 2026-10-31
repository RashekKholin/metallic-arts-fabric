package dev.metallicarts.domain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.Locale;

/** Stable serialized IDs; declaration order is never a save or wire format. */
public enum Metal {
    IRON, STEEL, TIN, PEWTER, ZINC, BRASS, COPPER, BRONZE,
    ALUMINUM, DURALUMIN, CHROMIUM, NICROSIL, GOLD, ELECTRUM, CADMIUM, BENDALLOY;

    public String id() { return name().toLowerCase(Locale.ROOT); }
    public static final Codec<Metal> CODEC = Codec.STRING.comapFlatMap(id ->
        Arrays.stream(values()).filter(m -> m.id().equals(id)).findFirst()
            .map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown metal: " + id)), Metal::id);
}
