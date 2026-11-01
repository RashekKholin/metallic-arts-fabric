package dev.metallicarts.client;
import dev.metallicarts.hemalurgy.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.Locale;
public final class SystemsScreen extends Screen {
 private int page,slot,preparation,kind=1,shape,power;private boolean intent;private int refresh;
 public SystemsScreen(){super(net.minecraft.network.chat.Component.translatable("ui.metallicarts.29d768017e3f"));}
 public boolean isPauseScreen(){return false;}
 protected void init(){int panel=Math.min(340,width-16),left=(width-panel)/2,choice=panel-84;
  addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.c4dd3c8cdd8d"),b->{page=Math.floorMod(page-1,3);rebuildWidgets();}).bounds(12,30,24,18).build());
  addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.091385be99b4"),b->{page=(page+1)%3;rebuildWidgets();}).bounds(width-36,30,24,18).build());
  addRenderableWidget(Button.builder((intent?net.minecraft.network.chat.Component.translatable("ui.metallicarts.bc0349b58dd9"):net.minecraft.network.chat.Component.translatable("ui.metallicarts.79250bc37812")),b->{intent=!intent;rebuildWidgets();}).bounds(width/2-64,55,128,18).build());
  if(page==0){
   addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.c48703d68e88", ((slot+1))),b->{slot=(slot+1)%12;rebuildWidgets();}).bounds(width/2-120,86,240,20).build());
   var burn=addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.efcba891deec"),b->send(1,0)).bounds(width/2-120,118,240,20).build());burn.active=intent;
  }else if(page==1){
   addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.750e723549db", (ConstructLedger.Purpose.values()[preparation].name().toLowerCase(Locale.ROOT))),b->{preparation=(preparation+1)%4;rebuildWidgets();}).bounds(left,81,choice,18).build());var prep=addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.e71e84af642c"),b->send(2,preparation)).bounds(left+choice+4,81,80,18).build());prep.active=intent;
   addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.cc64c308d6b4", (ConstructState.Kind.values()[kind].name().toLowerCase(Locale.ROOT))),b->{kind=kind%3+1;rebuildWidgets();}).bounds(left,107,choice,18).build());var body=addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.dea51bb13a44"),b->send(3,kind)).bounds(left+choice+4,107,80,18).build());body.active=intent;
   addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.7be1b141f430", (ConstructState.Shape.values()[shape].name().toLowerCase(Locale.ROOT))),b->{shape=(shape+1)%3;rebuildWidgets();}).bounds(left,133,choice,18).build());var form=addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.00465a3100e6"),b->send(4,shape)).bounds(left+choice+4,133,80,18).build());form.active=intent;
  }else{
   var compel=addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.31df0aa0843b"),b->send(5,0)).bounds(width/2-150,84,300,20).build());compel.active=intent;
   addRenderableWidget(Button.builder(UiText.of(StolenPower.choices().get(power).label()),b->{power=(power+1)%StolenPower.choices().size();rebuildWidgets();}).bounds(width/2-150,112,300,20).build());
   var extraction=addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.93ffc6e528aa"),b->send(6,power)).bounds(width/2-150,140,300,20).build());extraction.active=intent;
  }
 }
 private void send(int action,int selection){SystemsClient.send(action,selection,slot,intent);}
 public void tick(){if(++refresh%20==0)SystemsClient.send(0,0,slot,false);}
 public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float dt){super.extractRenderState(g,x,y,dt);g.centeredText(font,title,width/2,12,0xFFFFFFFF);g.centeredText(font,UiText.of(new String[]{"Compounding • whole source is consumed","Constructs • prepared canonical dependencies","Spiritual vulnerability • server policy applies"}[page]),width/2,34,0xFFFFCC88);int row=170;for(var line:font.split(UiText.of(SystemsClient.status),width-24)){if(row>218)break;g.text(font,line,12,row,0xFFCCDDDD);row+=10;}g.centeredText(font,net.minecraft.network.chat.Component.translatable("ui.metallicarts.9e7f60635d38"),width/2,231,0xFFAAAAAA);}
}
