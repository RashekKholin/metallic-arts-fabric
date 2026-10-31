package dev.metallicarts.domain;

import com.mojang.serialization.Codec;
import java.util.Locale;

/** Hemalurgy grants typed powers; it is not an innate metal-burning capability. */
public enum Art {
    ALLOMANCY, FERUCHEMY;
    public static final Codec<Art> CODEC = DomainCodecs.enumeration(values(), a -> a.name().toLowerCase(Locale.ROOT));
}
