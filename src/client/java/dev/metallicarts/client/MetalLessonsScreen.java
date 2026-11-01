package dev.metallicarts.client;
import dev.metallicarts.domain.Metal;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class MetalLessonsScreen extends Screen {
 private final Screen parent;private int metal,art,scroll;
 public MetalLessonsScreen(Screen parent){super(Component.translatable("metallicarts.guide.metals"));this.parent=parent;}
 @Override public boolean isPauseScreen(){return false;}
 private Component lesson(){return Component.translatable("tutorial.metallicarts."+(art==0?"allomancy":"feruchemy")+"."+Metal.values()[metal].id());}
 @Override public Component getNarrationMessage(){return title.copy().append(". ").append(Component.translatable("metal.metallicarts."+Metal.values()[metal].id())).append(". ").append(lesson());}
 @Override protected void init(){int w=Math.max(45,Math.min(110,(width-32)/3));addRenderableWidget(Button.builder(Component.translatable("metallicarts.guide.previous"),b->{metal=Math.floorMod(metal-1,16);scroll=0;rebuildWidgets();}).bounds(8,34,w,20).build());addRenderableWidget(Button.builder(Component.translatable(art==0?"art.metallicarts.allomancy":"art.metallicarts.feruchemy"),b->{art=1-art;scroll=0;rebuildWidgets();}).bounds(width/2-w/2,34,w,20).build());addRenderableWidget(Button.builder(Component.translatable("metallicarts.guide.next"),b->{metal=(metal+1)%16;scroll=0;rebuildWidgets();}).bounds(width-w-8,34,w,20).build());addRenderableWidget(Button.builder(Component.translatable("gui.back"),b->onClose()).bounds(width/2-45,height-26,90,20).build());}
 @Override public boolean mouseScrolled(double x,double y,double h,double v){scroll=Math.max(0,scroll-(int)Math.signum(v)*2);return true;}
 @Override public void onClose(){minecraft.setScreen(parent);}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float dt){g.fill(4,4,width-4,height-4,0xF0111820);super.extractRenderState(g,x,y,dt);g.centeredText(font,title.copy().append(" — ").append(Component.translatable("metal.metallicarts."+Metal.values()[metal].id())),width/2,14,0xFFFFFFFF);var lines=font.split(lesson(),Math.min(460,width-40));int visible=Math.max(1,(height-100)/12);scroll=Math.min(scroll,Math.max(0,lines.size()-visible));int row=68;for(int i=scroll;i<Math.min(lines.size(),scroll+visible);i++){g.text(font,lines.get(i),Math.max(20,(width-460)/2),row,0xFFFFFFFF);row+=12;}}
}
