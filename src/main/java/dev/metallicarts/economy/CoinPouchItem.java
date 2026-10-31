package dev.metallicarts.economy;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.*;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;
import java.util.*;

/** Vanilla bundle transactions restricted to actual coins; charge/metadata is preserved. */
public final class CoinPouchItem extends BundleItem {
    public CoinPouchItem(Properties properties){super(properties.component(DataComponents.BUNDLE_CONTENTS,BundleContents.EMPTY));}
    public static boolean coin(ItemStack stack){
        var form=EconomyContent.form(stack.getItem());return !stack.isEmpty()&&form!=null&&form.kind().equals("coin");
    }
    @Override public boolean overrideStackedOnOther(ItemStack pouch,Slot slot,ClickAction action,Player player){
        if(slot.hasItem()&&!coin(slot.getItem()))return false;
        return super.overrideStackedOnOther(pouch,slot,action,player);
    }
    @Override public boolean overrideOtherStackedOnMe(ItemStack pouch,ItemStack cursor,Slot slot,ClickAction action,Player player,SlotAccess access){
        if(!cursor.isEmpty()&&!coin(cursor))return false;
        return super.overrideOtherStackedOnMe(pouch,cursor,slot,action,player,access);
    }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){return InteractionResult.PASS;}
    public static Optional<ItemStack> peek(ItemStack pouch){
        var stacks=pouch.getOrDefault(DataComponents.BUNDLE_CONTENTS,BundleContents.EMPTY).itemCopyStream().toList();
        int selected=selected(pouch,stacks);
        return selected<0?Optional.empty():Optional.of(stacks.get(selected).copyWithCount(1));
    }
    private static int selected(ItemStack pouch,List<ItemStack> stacks){
        int selected=BundleItem.getSelectedItemIndex(pouch);
        if(selected>=0&&selected<stacks.size()&&coin(stacks.get(selected)))return selected;
        for(int i=0;i<stacks.size();i++)if(coin(stacks.get(i)))return i;
        return -1;
    }
    public static ItemStack takeOne(ItemStack pouch){
        var stacks=new ArrayList<>(pouch.getOrDefault(DataComponents.BUNDLE_CONTENTS,BundleContents.EMPTY).itemCopyStream().toList());
        int selected=selected(pouch,stacks);
        if(selected>=0){
            var stack=stacks.get(selected);boolean hadSelection=BundleItem.getSelectedItemIndex(pouch)>=0;
            var result=stack.copyWithCount(1);stack.shrink(1);
            stacks.removeIf(ItemStack::isEmpty);
            pouch.set(DataComponents.BUNDLE_CONTENTS,new BundleContents(stacks.stream().filter(s->!s.isEmpty()).map(ItemStackTemplate::fromNonEmptyStack).toList()));
            if(hadSelection&&!stacks.isEmpty())BundleItem.toggleSelectedItem(pouch,Math.min(selected,stacks.size()-1));
            return result;
        }
        return ItemStack.EMPTY;
    }
}
