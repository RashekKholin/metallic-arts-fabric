package dev.metallicarts.client;

import dev.metallicarts.metallurgy.MetallurgyMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class MetallurgyScreen extends AbstractContainerScreen<MetallurgyMenu> {
    public MetallurgyScreen(MetallurgyMenu menu,Inventory inventory,Component title) { super(menu,inventory,title,176,236); inventoryLabelY=142; }
    private void action(int id) { if (minecraft.gameMode!=null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id); }
    private final Button[] tabs = new Button[3];
    @Override protected void init() {
        super.init();
        String[] names={"Refine","Alloy","Shape"};
        for (int i=0;i<3;i++) { int id=i; tabs[i]=addRenderableWidget(Button.builder(UiText.of(names[i]),b -> action(id)).bounds(leftPos+8+i*54,topPos+18,52,20).build()); }
        addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.c4dd3c8cdd8d"),b -> action(3)).bounds(leftPos+60,topPos+90,20,20).build());
        addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.091385be99b4"),b -> action(4)).bounds(leftPos+110,topPos+90,20,20).build());
        addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.8bf1ef5668b1"),b -> action(5)).bounds(leftPos+8,topPos+118,52,20).build());
        addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.4873cc0821e9"),b -> action(6)).bounds(leftPos+68,topPos+118,100,20).build());
    }
    @Override protected void containerTick() { super.containerTick(); for(int i=0;i<3;i++) tabs[i].active=menu.data.get(0)!=i; }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTick) {
        super.extractBackground(g,mouseX,mouseY,partialTick);
        g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF272C34);
        for (var slot : menu.slots) { int x=leftPos+slot.x,y=topPos+slot.y; g.fill(x-1,y-1,x+17,y+17,0xFF68717D); g.fill(x,y,x+16,y+16,0xFF10151C); }
        g.text(font,"Inputs",leftPos+20,topPos+41,0xFFCCCCCC);
        g.text(font,"Outputs",leftPos+124,topPos+41,0xFFCCCCCC);
        g.text(font,"Fuel",leftPos+18,topPos+80,0xFFFFBB66);
        g.text(font,"Recipe",leftPos+76,topPos+80,0xFFCCCCCC);
        String status=switch(menu.data.get(6)) { case 1 -> "Check inputs / recipe"; case 2 -> "Output slots full"; case 3 -> "Add furnace fuel"; default -> menu.data.get(4)==1?"Processing "+menu.data.get(2)+"t":"Choose recipe; Start"; };
        g.text(font,status,leftPos+8,topPos+6,0xFFFFFFFF);
        g.text(font,"Batch: "+(menu.data.get(5)==1?"ON":"OFF"),leftPos+8,topPos+140,0xFFDDCC88);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY) {
        g.text(font,playerInventoryTitle,92,142,0xFFCCCCCC);
    }
}
