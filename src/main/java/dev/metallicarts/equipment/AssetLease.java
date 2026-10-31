package dev.metallicarts.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import java.util.*;

/** A canonical world asset can occupy only one player's contact slot at a time. */
public record AssetLease(UUID player,int slot) {
    public static final int SLOTS=12;
    public static final Codec<AssetLease> CODEC=RecordCodecBuilder.create(i->i.group(
        UUIDUtil.CODEC.fieldOf("player").forGetter(AssetLease::player),
        Codec.intRange(0,SLOTS-1).fieldOf("slot").forGetter(AssetLease::slot)
    ).apply(i,AssetLease::new));
    public AssetLease{
        Objects.requireNonNull(player);
        if(slot<0||slot>=SLOTS)throw new IllegalArgumentException("Invalid contact slot");
    }
}
