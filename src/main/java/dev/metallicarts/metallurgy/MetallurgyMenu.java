package dev.metallicarts.metallurgy;

import dev.metallicarts.economy.EconomyContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;

public final class MetallurgyMenu extends AbstractContainerMenu {
    public final Container container;
    public final ContainerData data;
    private final SimpleContainer preview = new SimpleContainer(1);
    private final Player owner;
    public MetallurgyMenu(int id, Inventory inventory, BlockPos pos) { this(id,inventory,new SimpleContainer(9),new SimpleContainerData(7)); }
    public MetallurgyMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(EconomyContent.TABLE_MENU,id); this.container=container; this.data=data; this.owner=inventory.player;
        for (int i=0;i<4;i++) addSlot(new Slot(container,i,20+i*20,52));
        addSlot(new Slot(container,4,20,92) { @Override public boolean mayPlace(ItemStack stack) { return fuel(stack); } });
        for (int i=0;i<4;i++) addSlot(new Slot(container,5+i,124+(i%2)*20,52+(i/2)*20) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        addSlot(new Slot(preview,0,88,92) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) { return false; }
        });
        for (int row=0;row<3;row++) for (int col=0;col<9;col++) addSlot(new Slot(inventory,col+row*9+9,8+col*18,154+row*18));
        for (int col=0;col<9;col++) addSlot(new Slot(inventory,col,8+col*18,212));
        addDataSlots(data);
    }
    @Override public void broadcastChanges() {
        if (container instanceof MetallurgyBlockEntity table) preview.setItem(0,table.preview());
        super.broadcastChanges();
    }
    @Override public boolean stillValid(Player player) { return container.stillValid(player); }
    private boolean fuel(ItemStack stack) { return owner.level().fuelValues().burnDuration(stack)>0; }
    @Override public boolean clickMenuButton(Player player,int id) {
        if (!stillValid(player) || id<0 || id>6 || !(container instanceof MetallurgyBlockEntity table)) return false;
        table.button(id); broadcastChanges(); return true;
    }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if (index==9 || index<0 || index>=slots.size()) return ItemStack.EMPTY;
        Slot slot=slots.get(index); if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(), original=stack.copy();
        if (index<9) { if (!moveItemStackTo(stack,10,46,true)) return ItemStack.EMPTY; }
        else if (fuel(stack)) { if (!moveItemStackTo(stack,4,5,false)) return ItemStack.EMPTY; }
        else if (!moveItemStackTo(stack,0,4,false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }
}
