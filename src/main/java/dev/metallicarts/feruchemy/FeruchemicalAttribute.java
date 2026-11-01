package dev.metallicarts.feruchemy;

import com.mojang.serialization.Codec;
import dev.metallicarts.domain.*;
import java.util.Locale;

/** Attribute IDs are stable save data. Each Tin sense is a distinct charge type. */
public enum FeruchemicalAttribute {
    WEIGHT(Metal.IRON), SPEED(Metal.STEEL), STRENGTH(Metal.PEWTER),
    SIGHT(Metal.TIN), HEARING(Metal.TIN), TOUCH(Metal.TIN), SMELL(Metal.TIN), TASTE(Metal.TIN),
    WAKEFULNESS(Metal.BRONZE), WARMTH(Metal.BRASS), MENTAL_SPEED(Metal.ZINC), MEMORY(Metal.COPPER),
    HEALTH(Metal.GOLD), NUTRITION(Metal.BENDALLOY), BREATH(Metal.CADMIUM), DETERMINATION(Metal.ELECTRUM),
    IDENTITY(Metal.ALUMINUM), CONNECTION(Metal.DURALUMIN), INVESTITURE(Metal.NICROSIL), FORTUNE(Metal.CHROMIUM);
    public static final Codec<FeruchemicalAttribute> CODEC=DomainCodecs.enumeration(values(),FeruchemicalAttribute::id);
    private final Metal metal;
    FeruchemicalAttribute(Metal metal){this.metal=metal;}
    public Metal metal(){return metal;}
    public String id(){return name().toLowerCase(Locale.ROOT);}
}
