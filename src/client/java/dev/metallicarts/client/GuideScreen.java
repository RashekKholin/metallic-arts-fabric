package dev.metallicarts.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
/** Short pages retain keyboard focus/narration; wrapped text stays inside the current window. */
public final class GuideScreen extends Screen {
 private int page;
 public GuideScreen(){super(Component.translatable("metallicarts.guide.title"));}
 @Override public boolean isPauseScreen(){return false;}
 @Override protected void init(){addRenderableWidget(Button.builder(Component.translatable("metallicarts.guide.previous"),b->{page=Math.floorMod(page-1,7);rebuildWidgets();}).bounds(12,height-30,90,20).build());addRenderableWidget(Button.builder(Component.translatable("metallicarts.guide.next"),b->{page=(page+1)%7;rebuildWidgets();}).bounds(width-102,height-30,90,20).build());addRenderableWidget(Button.builder(Component.translatable("metallicarts.guide.close"),b->onClose()).bounds(width/2-45,height-30,90,20).build());}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float dt){g.fill(4,4,width-4,height-4,0xEE111820);super.extractRenderState(g,x,y,dt);g.centeredText(font,title,width/2,16,0xFFFFFFFF);g.centeredText(font,Component.literal((page+1)+" / 7"),width/2,32,0xFFFFFF88);int row=54;for(var line:font.split(Component.translatable("metallicarts.guide."+page),Math.min(460,width-32))){if(row>height-45)break;g.text(font,line,Math.max(16,(width-460)/2),row,0xFFFFFFFF);row+=12;}}
}
