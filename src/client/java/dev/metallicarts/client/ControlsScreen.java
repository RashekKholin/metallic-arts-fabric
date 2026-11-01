package dev.metallicarts.client;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
/** Live binding labels, complete narration, and scrollable explanations rather than fixed-key guesses. */
public final class ControlsScreen extends Screen {
 private final Screen parent;private int page,scroll;
 public ControlsScreen(Screen parent){super(Component.translatable("screen.metallicarts.controls"));this.parent=parent;}
 public static Component binding(String name){var key=KeyMapping.get("key.metallicarts."+name);return key==null?Component.literal("?"):key.getTranslatedKeyMessage();}
 private Component text(){var body=Component.translatable("controls.metallicarts.page"+page).copy();String[] keys=page==0?new String[]{"open","push","pull","select","clear","toss","auto_select","controls"}:page==1?new String[]{"feruchemy_menu","hemalurgy_menu","allomancy_menu","layout","power_type","memory_menu","unseal","systems","carry"}:new String[]{"previous_page","next_page","opening","journal"};for(var name:keys)body.append("\n").append(binding(name)).append(" — ").append(Component.translatable("key.metallicarts."+name));return body;}
 @Override public boolean isPauseScreen(){return false;}
 @Override public Component getNarrationMessage(){return title.copy().append(". ").append(text());}
 @Override protected void init(){int w=Math.min(95,(width-32)/3);addRenderableWidget(Button.builder(Component.translatable("metallicarts.guide.previous"),b->{page=Math.floorMod(page-1,3);scroll=0;}).bounds(8,height-27,w,20).build());addRenderableWidget(Button.builder(Component.translatable("gui.back"),b->onClose()).bounds(width/2-w/2,height-27,w,20).build());addRenderableWidget(Button.builder(Component.translatable("metallicarts.guide.next"),b->{page=(page+1)%3;scroll=0;}).bounds(width-w-8,height-27,w,20).build());addRenderableWidget(Button.builder(Component.literal("↑"),b->scroll=Math.max(0,scroll-3)).bounds(width-22,38,16,18).build());addRenderableWidget(Button.builder(Component.literal("↓"),b->scroll+=3).bounds(width-22,60,16,18).build());}
 @Override public boolean mouseScrolled(double x,double y,double h,double v){scroll=Math.max(0,scroll-(int)Math.signum(v)*3);return true;}
 @Override public void onClose(){minecraft.setScreen(parent);}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float dt){g.fill(4,4,width-4,height-4,0xFA111820);super.extractRenderState(g,x,y,dt);g.centeredText(font,title.copy().append(" — "+(page+1)+"/3"),width/2,14,0xFFFFFFFF);var lines=font.split(text(),Math.min(480,width-48));int visible=Math.max(1,(height-80)/12);scroll=Math.min(scroll,Math.max(0,lines.size()-visible));for(int row=0;row<visible&&scroll+row<lines.size();row++)g.text(font,lines.get(scroll+row),Math.max(16,(width-480)/2),42+row*12,0xFFFFFFFF);}
}
