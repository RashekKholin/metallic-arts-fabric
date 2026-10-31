package dev.metallicarts.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.domain.DomainCodecs;
import net.minecraft.core.UUIDUtil;
import java.util.*;

/** An item reference, never authoritative charge. Transfer revisions invalidate copied stale items. */
public record AssetToken(UUID instance,UUID seal,long revision) {
    public static final Codec<AssetToken> CODEC=RecordCodecBuilder.create(i->i.group(
        UUIDUtil.CODEC.fieldOf("instance").forGetter(AssetToken::instance),
        UUIDUtil.CODEC.fieldOf("seal").forGetter(AssetToken::seal),
        DomainCodecs.longRange(0,Long.MAX_VALUE-1).fieldOf("revision").forGetter(AssetToken::revision)
    ).apply(i,AssetToken::new));
    public AssetToken{
        Objects.requireNonNull(instance);Objects.requireNonNull(seal);
        if(revision<0||revision==Long.MAX_VALUE)throw new IllegalArgumentException("Invalid asset revision");
    }
}
