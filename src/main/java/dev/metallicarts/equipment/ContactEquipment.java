package dev.metallicarts.equipment;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Deeply copied persistent equipment. The runtime is transient; the actual items are not. */
public record ContactEquipment(List<ItemStack> items) {
    public static final List<String> NAMES=List.of("Left ring","Right ring","Left wrist","Right wrist","Left bracer","Right bracer",
        "Left ear","Right ear","Neck","Left ankle","Right ankle","Belt");
    public static final Codec<ContactEquipment> CODEC=ItemStack.OPTIONAL_CODEC.listOf(AssetLease.SLOTS,AssetLease.SLOTS).xmap(ContactEquipment::new,ContactEquipment::items);
    public ContactEquipment{
        if(items.size()!=AssetLease.SLOTS||items.stream().anyMatch(s->!s.isEmpty()&&s.getCount()!=1))
            throw new IllegalArgumentException("Contact equipment requires twelve individual slots");
        items=items.stream().map(ItemStack::copy).toList();
    }
    @Override public List<ItemStack> items(){return items.stream().map(ItemStack::copy).toList();}
    public ItemStack item(int slot){return items.get(slot).copy();}
    public ContactEquipment with(int slot,ItemStack stack){
        var changed=new ArrayList<>(items);changed.set(slot,stack.copy());return new ContactEquipment(changed);
    }
    public static ContactEquipment empty(){return new ContactEquipment(java.util.stream.IntStream.range(0,AssetLease.SLOTS).mapToObj(i->ItemStack.EMPTY).toList());}
}
