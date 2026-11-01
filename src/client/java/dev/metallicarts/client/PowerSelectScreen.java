package dev.metallicarts.client;
import dev.metallicarts.domain.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.function.IntConsumer;
public final class PowerSelectScreen extends Screen {
 private final IntConsumer select;private int art;private String accessSignature="";
 public PowerSelectScreen(IntConsumer select){super(net.minecraft.network.chat.Component.translatable("ui.metallicarts.66ea2b602613"));this.select=select;}
 public boolean isPauseScreen(){return false;}
 protected void init(){
  accessSignature=AdvancedArtsClient.journal.toString();
  addRenderableWidget(Button.builder((art==0?net.minecraft.network.chat.Component.translatable("ui.metallicarts.8f50d6474101"):net.minecraft.network.chat.Component.translatable("ui.metallicarts.6c47cf82a8fc")),b->{art=1-art;rebuildWidgets();}).bounds(width/2-90,35,180,18).build());
  for(int i=0;i<16;i++){final int value=art*16+i;var metal=Metal.values()[i];var button=Button.builder(UiText.of(metal.id()),b->select.accept(value)).bounds(width/2-154+(i%4)*78,62+(i/4)*29,74,22).build();button.active=MetallicArtsClient.snapshot!=null&&AdvancedArtsClient.ownedAccess(MetallicArtsClient.snapshot,art==0?Art.ALLOMANCY:Art.FERUCHEMY,metal)>0;addRenderableWidget(button);}
 }
 public void tick(){if(!accessSignature.equals(AdvancedArtsClient.journal.toString()))rebuildWidgets();}
 public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float d){super.extractRenderState(g,x,y,d);g.centeredText(font,title,width/2,15,0xFFFFFFFF);g.centeredText(font,net.minecraft.network.chat.Component.translatable("ui.metallicarts.ce60a29687f4"),width/2,200,0xFFCCCCCC);}
}
