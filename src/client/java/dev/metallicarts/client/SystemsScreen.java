package dev.metallicarts.client;
import dev.metallicarts.hemalurgy.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.Locale;
public final class SystemsScreen extends Screen {
 private int page,slot,preparation,kind=1,shape,power;private boolean intent;private int refresh;
 public SystemsScreen(){super(Component.literal("The Metallic Arts — Advanced systems"));}
 public boolean isPauseScreen(){return false;}
 protected void init(){
  addRenderableWidget(Button.builder(Component.literal("<"),b->{page=Math.floorMod(page-1,3);rebuildWidgets();}).bounds(12,30,24,18).build());
  addRenderableWidget(Button.builder(Component.literal(">"),b->{page=(page+1)%3;rebuildWidgets();}).bounds(width-36,30,24,18).build());
  addRenderableWidget(Button.builder(Component.literal(intent?"Intent: ON":"Intent: OFF"),b->{intent=!intent;rebuildWidgets();}).bounds(width/2-64,55,128,18).build());
  if(page==0){
   addRenderableWidget(Button.builder(Component.literal("Destination slot "+(slot+1)),b->{slot=(slot+1)%12;rebuildWidgets();}).bounds(width/2-120,86,240,20).build());
   var burn=addRenderableWidget(Button.builder(Component.literal("Compound held mind"),b->send(1,0)).bounds(width/2-120,118,240,20).build());burn.active=intent;
  }else if(page==1){
   addRenderableWidget(Button.builder(Component.literal("Prepare: "+ConstructLedger.Purpose.values()[preparation].name().toLowerCase(Locale.ROOT)),b->{preparation=(preparation+1)%4;rebuildWidgets();}).bounds(width/2-160,81,250,18).build());var prep=addRenderableWidget(Button.builder(Component.literal("Prepare held"),b->send(2,preparation)).bounds(width/2+94,81,76,18).build());prep.active=intent;
   addRenderableWidget(Button.builder(Component.literal("Body: "+ConstructState.Kind.values()[kind].name().toLowerCase(Locale.ROOT)),b->{kind=kind%3+1;rebuildWidgets();}).bounds(width/2-160,107,250,18).build());var body=addRenderableWidget(Button.builder(Component.literal("Transform"),b->send(3,kind)).bounds(width/2+94,107,76,18).build());body.active=intent;
   addRenderableWidget(Button.builder(Component.literal("Shape: "+ConstructState.Shape.values()[shape].name().toLowerCase(Locale.ROOT)),b->{shape=(shape+1)%3;rebuildWidgets();}).bounds(width/2-160,133,250,18).build());var form=addRenderableWidget(Button.builder(Component.literal("Assume form"),b->send(4,shape)).bounds(width/2+94,133,76,18).build());form.active=intent;
  }else{
   var compel=addRenderableWidget(Button.builder(Component.literal("Compel aimed spiked target"),b->send(5,0)).bounds(width/2-150,84,300,20).build());compel.active=intent;
   addRenderableWidget(Button.builder(Component.literal(StolenPower.choices().get(power).label()),b->{power=(power+1)%StolenPower.choices().size();rebuildWidgets();}).bounds(width/2-150,112,300,20).build());
   var extraction=addRenderableWidget(Button.builder(Component.literal("Exceptional nonlethal extraction"),b->send(6,power)).bounds(width/2-150,140,300,20).build());extraction.active=intent;
  }
 }
 private void send(int action,int selection){SystemsClient.send(action,selection,slot,intent);}
 public void tick(){if(++refresh%20==0)SystemsClient.send(0,0,slot,false);}
 public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float dt){super.extractRenderState(g,x,y,dt);g.centeredText(font,title,width/2,12,0xFFFFFFFF);g.centeredText(font,Component.literal(new String[]{"Compounding • whole source is consumed","Constructs • prepared canonical dependencies","Spiritual vulnerability • server policy applies"}[page]),width/2,34,0xFFFFCC88);int row=170;for(var line:font.split(Component.literal(SystemsClient.status),width-24)){if(row>218)break;g.text(font,line,12,row,0xFFCCDDDD);row+=10;}g.centeredText(font,Component.literal("Explicit Intent is required • Esc: close"),width/2,231,0xFFAAAAAA);}
}
