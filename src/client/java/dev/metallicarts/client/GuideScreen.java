package dev.metallicarts.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
/** Wrapped, scrollable, fully narrated tutorial pages with keyboard-focusable navigation. */
public final class GuideScreen extends Screen {
 private int page,scroll;
 public GuideScreen(){super(Component.translatable("metallicarts.guide.title"));}
 @Override public boolean isPauseScreen(){return false;}
 private Component body(){return Component.translatable("metallicarts.guide."+page);}
 @Override public Component getNarrationMessage(){return title.copy().append(". ").append(body());}
 @Override protected void init(){int w=Math.max(45,Math.min(90,(width-32)/3)),top=Math.max(70,Math.min(120,(width-24)/2));
  addRenderableWidget(Button.builder(Component.translatable("metallicarts.guide.metals"),b->minecraft.setScreen(new MetalLessonsScreen(this))).bounds(8,32,top,18).build());
  addRenderableWidget(Button.builder(Component.translatable("metallicarts.guide.settings"),b->minecraft.setScreen(new SensorySettingsScreen(this))).bounds(width-top-8,32,top,18).build());
  addRenderableWidget(Button.builder(Component.translatable("metallicarts.guide.previous"),b->{page=Math.floorMod(page-1,11);scroll=0;rebuildWidgets();}).bounds(8,height-28,w,20).build());
  addRenderableWidget(Button.builder(Component.translatable("metallicarts.guide.close"),b->onClose()).bounds(width/2-w/2,height-28,w,20).build());
  addRenderableWidget(Button.builder(Component.translatable("metallicarts.guide.next"),b->{page=(page+1)%11;scroll=0;rebuildWidgets();}).bounds(width-w-8,height-28,w,20).build());
  addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.9555ffd72341"),b->scroll=Math.max(0,scroll-2)).bounds(width-20,58,16,18).build());
  addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.fc486696adf1"),b->scroll+=2).bounds(width-20,80,16,18).build());
 }
 @Override public boolean mouseScrolled(double x,double y,double h,double v){scroll=Math.max(0,scroll-(int)Math.signum(v)*2);return true;}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float dt){g.fill(4,4,width-4,height-4,ClientPreferences.value(6)?0xFF000000:0xF0111820);super.extractRenderState(g,x,y,dt);g.centeredText(font,title.copy().append(" — "+(page+1)+" / 11"),width/2,14,0xFFFFFFFF);var lines=font.split(body(),Math.min(460,width-48));int visible=Math.max(1,(height-100)/12);scroll=Math.min(scroll,Math.max(0,lines.size()-visible));int row=62;for(int i=scroll;i<Math.min(lines.size(),scroll+visible);i++){g.text(font,lines.get(i),Math.max(20,(width-460)/2),row,0xFFFFFFFF);row+=12;}}
}
