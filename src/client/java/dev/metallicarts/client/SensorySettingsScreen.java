package dev.metallicarts.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class SensorySettingsScreen extends Screen {
 private final Screen parent;
 public SensorySettingsScreen(Screen parent){super(Component.translatable("metallicarts.guide.settings"));this.parent=parent;}
 @Override public boolean isPauseScreen(){return false;}
 @Override protected void init(){int w=Math.min(320,width-20),step=Math.min(21,Math.max(16,(height-80)/7));for(int i=0;i<ClientPreferences.NAMES.length;i++){final int index=i;var message=Component.translatable("option.metallicarts."+ClientPreferences.NAMES[i]).append(": ").append(Component.translatable(ClientPreferences.value(i)?"options.on":"options.off"));addRenderableWidget(Button.builder(message,b->{ClientPreferences.toggle(index);rebuildWidgets();}).bounds((width-w)/2,35+i*step,w,18).build());}addRenderableWidget(Button.builder(Component.translatable("gui.done"),b->onClose()).bounds(width/2-45,height-26,90,20).build());}
 @Override public void onClose(){minecraft.setScreen(parent);}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float dt){g.fill(4,4,width-4,height-4,0xF0111820);super.extractRenderState(g,x,y,dt);g.centeredText(font,title,width/2,14,0xFFFFFFFF);}
}
