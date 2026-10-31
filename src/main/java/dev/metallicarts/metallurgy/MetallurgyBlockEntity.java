package dev.metallicarts.metallurgy;

import dev.metallicarts.economy.EconomyContent;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import java.util.*;

public final class MetallurgyBlockEntity extends BaseContainerBlockEntity implements ExtendedMenuProvider<BlockPos> {
    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    public int tab, selection, progress, burn, status;
    public boolean running, batch;
    private String job = "";
    private final ContainerData data = new ContainerData() {
        public int get(int i) { return switch (i) { case 0 -> tab; case 1 -> selection; case 2 -> progress; case 3 -> burn; case 4 -> running ? 1 : 0; case 5 -> batch ? 1 : 0; case 6 -> status; default -> 0; }; }
        public void set(int i, int v) { switch (i) { case 0 -> tab=v; case 1 -> selection=v; case 2 -> progress=v; case 3 -> burn=v; case 4 -> running=v!=0; case 5 -> batch=v!=0; case 6 -> status=v; } }
        public int getCount() { return 7; }
    };
    public MetallurgyBlockEntity(BlockPos pos, BlockState state) { super(EconomyContent.TABLE_ENTITY, pos, state); }
    public List<ItemStack> inputs() { return items.subList(0, 4); }
    public MetallurgyRecipes.Recipe selected() {
        var options = MetallurgyRecipes.candidates(tab, inputs()); if (options.isEmpty()) return null;
        selection = Math.floorMod(selection, options.size()); return options.get(selection);
    }
    public ItemStack preview() {
        var recipe = selected(); if (recipe == null) return ItemStack.EMPTY;
        var plan = MetallurgyRecipes.plan(recipe, inputs());
        ItemStack result=plan!=null?plan.outputs().getFirst().copy():recipe.alloy()!=null?
            new ItemStack(EconomyContent.item(recipe.alloy(),"portion")):
            new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(recipe.outputs().getFirst().item())));
        result.remove(dev.metallicarts.registry.ModRegistries.METAL_DATA); // Stable ghost preview; actual outputs keep their unique instances.
        List<Component> hints=new ArrayList<>();
        hints.add(Component.literal("Recipe: "+recipe.id()));
        if(recipe.alloy()!=null) recipe.ratio().forEach((material,percent)->hints.add(Component.literal(material+": "+percent+"%")));
        else recipe.inputs().forEach(i->hints.add(Component.literal(i.count()+" × "+(i.item()!=null?i.item():i.tag()))));
        hints.add(Component.literal(recipe.ticks()+" ticks"));
        result.set(net.minecraft.core.component.DataComponents.LORE,new net.minecraft.world.item.component.ItemLore(hints));
        return result;
    }
    public void button(int id) {
        if (id >= 0 && id <= 2) { tab=id; selection=0; running=false; progress=0; }
        else if (id==3 || id==4) { selection += id==3 ? -1 : 1; running=false; progress=0; }
        else if (id==5) batch=!batch;
        else if (id==6) { running=!running; progress=0; }
        setChanged();
    }
    /** Preflight all output slots against copies. Nothing is consumed unless every result fits. */
    public List<ItemStack> fit(List<ItemStack> output) {
        List<ItemStack> slots = new ArrayList<>(); for (int i=5;i<9;i++) slots.add(items.get(i).copy());
        for (var result : output) {
            ItemStack remainder=result.copy();
            for (var slot : slots) if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot,remainder)) {
                int moved=Math.min(remainder.getCount(),slot.getMaxStackSize()-slot.getCount()); slot.grow(moved); remainder.shrink(moved);
            }
            for (int i=0;i<4 && !remainder.isEmpty();i++) if (slots.get(i).isEmpty()) { slots.set(i,remainder.copy()); remainder.setCount(0); }
            if (!remainder.isEmpty()) return null;
        }
        return slots;
    }
    public static void tick(Level level, BlockPos pos, BlockState state, MetallurgyBlockEntity table) {
        if (!table.running) return;
        var recipe=table.selected(); var plan=recipe==null?null:MetallurgyRecipes.plan(recipe,table.inputs());
        if (plan==null) { table.status=1; table.progress=0; table.setChanged(); return; }
        if (!recipe.id().equals(table.job)) { table.progress=0; table.job=recipe.id(); }
        var fitted=table.fit(plan.outputs());
        if (fitted==null) { table.status=2; table.setChanged(); return; }
        if (table.burn<=0) {
            ItemStack fuel=table.items.get(4); int ticks=level.fuelValues().burnDuration(fuel);
            if (ticks<=0) { table.status=3; return; }
            table.burn=ticks;
            var remainder=fuel.getItem().getCraftingRemainder(); fuel.shrink(1);
            if (fuel.isEmpty() && remainder!=null) table.items.set(4,remainder.create());
        }
        table.burn--; table.progress++; table.status=0;
        if (table.progress>=plan.ticks()) {
            // Game-thread transaction: inputs and outputs change together; no client values participate.
            for (int i=0;i<4;i++) table.items.get(i).shrink(plan.consumed()[i]);
            plan.replacements().forEach((slot,replacement)->table.items.set(slot,replacement));
            for (int i=0;i<4;i++) table.items.set(5+i,fitted.get(i));
            table.progress=0; if (!table.batch) table.running=false;
        }
        table.setChanged();
    }
    @Override protected Component getDefaultName() { return Component.translatable("container.metallicarts.metallurgy"); }
    @Override public int getContainerSize() { return 9; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items=items; }
    @Override public int getMaxStackSize() { return 99; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot<4 || slot==4 && level!=null && level.fuelValues().burnDuration(stack)>0; }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new MetallurgyMenu(id,inventory,this,data); }
    @Override public BlockPos getScreenOpeningData(ServerPlayer player) { return worldPosition; }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input); items=NonNullList.withSize(9,ItemStack.EMPTY); ContainerHelper.loadAllItems(input,items);
        tab=Math.clamp(input.getIntOr("Tab",0),0,2); selection=input.getIntOr("Selection",0);
        progress=Math.clamp(input.getIntOr("Progress",0),0,24000); burn=Math.clamp(input.getIntOr("Burn",0),0,32767);
        running=input.getBooleanOr("Running",false); batch=input.getBooleanOr("Batch",false); job=input.getStringOr("Job","");
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output); ContainerHelper.saveAllItems(output,items);
        output.putInt("Tab",tab); output.putInt("Selection",selection); output.putInt("Progress",progress); output.putInt("Burn",burn);
        output.putBoolean("Running",running); output.putBoolean("Batch",batch); output.putString("Job",job);
    }
}
