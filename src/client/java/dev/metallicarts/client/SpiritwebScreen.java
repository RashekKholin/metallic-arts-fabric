package dev.metallicarts.client;
import dev.metallicarts.domain.*;
import dev.metallicarts.allomancy.*;
import dev.metallicarts.network.SelectionIntent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
/** All sixteen metal controls stay on one page; a compact grid replaces the wheel on narrow screens. */
public final class SpiritwebScreen extends Screen {
 private boolean wheel=true;private int refresh,zinc,brass;private final int[] xs=new int[16],ys=new int[16];private int cellWidth;
 public SpiritwebScreen(){super(Component.translatable("screen.metallicarts.title"));}
 private boolean radial(){return wheel&&width>=560&&height>=340;}
 @Override protected void init(){AdvancedArtsClient.action(3,0);var journal=AdvancedArtsClient.journal;if(journal.has("zincEmotion"))zinc=journal.get("zincEmotion").getAsInt();if(journal.has("brassEmotion"))brass=journal.get("brassEmotion").getAsInt();
  addRenderableWidget(Button.builder(Component.translatable("metallicarts.guide.title"),b->minecraft.setScreen(new GuideScreen())).bounds(8,8,65,18).build());cellWidth=radial()?76:Math.min(120,(width-32)/4-4);
  for(int i=0;i<16;i++){final int index=i;var metal=AllomancyConfig.SUPPORTED.get(i);if(radial()){double a=-Math.PI/2+i*Math.PI/8;xs[i]=width/2+(int)(Math.cos(a)*Math.min(210,width/2-48))-cellWidth/2;ys[i]=(height-46)/2+(int)(Math.sin(a)*Math.min(125,(height-140)/2))-9;}else{int left=(width-4*(cellWidth+4)+4)/2;xs[i]=left+i%4*(cellWidth+4);ys[i]=40+i/4*31;}
   var button=Button.builder(UiText.of(metal.id()),b->AllomancyClient.cycle(index)).bounds(xs[i],ys[i],cellWidth,18).build();button.setTooltip(Tooltip.create(Component.translatable("metal.metallicarts."+metal.id()).copy().append(" — click: off / burn / flare. A = Allomancy, F = Feruchemy.")));addRenderableWidget(button);
  }
  int half=(width-24)/2;addRenderableWidget(Button.builder(Component.literal("Zinc: "+emotion(zinc)),b->{zinc=(zinc+1)%4;AdvancedArtsClient.action(0,zinc);b.setMessage(Component.literal("Zinc: "+emotion(zinc)));}).bounds(8,height-57,half,17).build());
  addRenderableWidget(Button.builder(Component.literal("Brass: "+emotion(brass)),b->{brass=(brass+1)%4;AdvancedArtsClient.action(4,brass);b.setMessage(Component.literal("Brass: "+emotion(brass)));}).bounds(width-half-8,height-57,half,17).build());
  int bw=(width-32)/3;addRenderableWidget(Button.builder(Component.literal("Auto metals: "+((AllomancyClient.status.flags()&8)!=0?"ON":"OFF")),b->{if(ClientPlayNetworking.canSend(SelectionIntent.TYPE))ClientPlayNetworking.send(new SelectionIntent(2));}).bounds(8,height-36,bw,18).build());
  addRenderableWidget(Button.builder(Component.literal(radial()?"Grid / W":"Wheel / W"),b->{wheel=!wheel;rebuildWidgets();}).bounds(16+bw,height-36,bw,18).build());
  addRenderableWidget(Button.builder(Component.literal("Controls / I"),b->minecraft.setScreen(new ControlsScreen(this))).bounds(24+2*bw,height-36,bw,18).build());
 }
 private static String emotion(int i){return dev.metallicarts.arts.EmotionProfile.Emotion.values()[Math.floorMod(i,4)].name().toLowerCase(java.util.Locale.ROOT);}
 @Override public void tick(){if(++refresh%20==0){AdvancedArtsClient.action(3,0);for(var child:children())if(child instanceof Button b&&b.getMessage().getString().startsWith("Auto metals:"))b.setMessage(Component.literal("Auto metals: "+((AllomancyClient.status.flags()&8)!=0?"ON":"OFF")));}}
 @Override public boolean isPauseScreen(){return false;}
 @Override public Component getNarrationMessage(){var text=title.copy().append(". All sixteen metals. A Allomancy, F Feruchemy. Zinc riots "+emotion(zinc)+". Brass soothes "+emotion(brass)+". ");var state=MetallicArtsClient.snapshot();if(state!=null)for(var m:AllomancyConfig.SUPPORTED)text.append(Component.translatable("metal.metallicarts."+m.id())).append(" A "+AdvancedArtsClient.access(state,Art.ALLOMANCY,m)+" F "+AdvancedArtsClient.access(state,Art.FERUCHEMY,m)+". ");return text;}
 @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent e){if(ContextKeys.LAYOUT.matches(e)){wheel=!wheel;rebuildWidgets();return true;}if(ContextKeys.F.matches(e)){FeruchemyClient.open();return true;}if(ContextKeys.H.matches(e)){HemalurgyClient.open();return true;}if(net.minecraft.client.KeyMapping.get("key.metallicarts.systems").matches(e)){SystemsClient.open();return true;}return super.keyPressed(e);}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float dt){g.fill(3,3,width-3,height-3,0xF20A111F);g.fill(3,3,width-3,5,0xFF49B9D1);var state=MetallicArtsClient.snapshot();
  if(radial()){int cx=width/2,cy=(height-46)/2;for(int i=0;i<96;i++){double a=i*Math.PI/48,b=(i+1)*Math.PI/48;HudLines.draw(g,cx+(float)Math.cos(a)*80,cy+(float)Math.sin(a)*80,cx+(float)Math.cos(b)*80,cy+(float)Math.sin(b)*80,0x805BBACC);}for(int i=0;i<16;i++)HudLines.draw(g,cx,cy,xs[i]+cellWidth/2,ys[i]+9,AllomancyClient.status.mode(i)>0?0xAA55FFFF:0x303A617D);g.centeredText(font,Component.literal("THE METALLIC ARTS"),cx,cy-9,0xFFEAD9AE);g.centeredText(font,Component.literal("A / F • 16 metals"),cx,cy+5,0xFF83B4CE);}
  for(int i=0;i<16;i++){int color=AllomancyClient.status.mode(i)>0?0xFF5AEADB:0xFF294051;g.fill(xs[i]-1,ys[i]-1,xs[i]+cellWidth+1,ys[i]+19,color);}
  super.extractRenderState(g,x,y,dt);g.centeredText(font,title,width/2,13,0xFFEEDDB8);g.centeredText(font,Component.literal("All sixteen • A Allomancy / F Feruchemy"),width/2,27,0xFF88AEC6);
  if(state!=null)for(int i=0;i<16;i++){var m=AllomancyConfig.SUPPORTED.get(i);String access=(AdvancedArtsClient.access(state,Art.ALLOMANCY,m)>0?"A":"–")+"/"+(AdvancedArtsClient.access(state,Art.FERUCHEMY,m)>0?"F":"–");long fuel=ReserveLedger.valid(state)?ReserveLedger.ordinary(state,m):0;String amount=fuel>=1000000?String.format(java.util.Locale.ROOT,"%.1fg",fuel/1000000.0):fuel>=1000?fuel/1000+"mg":fuel+"µg";g.centeredText(font,Component.literal(access+" "+(AllomancyClient.status.mode(i)==2?"✦ ":AllomancyClient.status.mode(i)==1?"• ":"")+amount),xs[i]+cellWidth/2,ys[i]+20,AllomancyClient.status.mode(i)>0?0xFF77FFFF:0xFFAAC2D6);}
  g.centeredText(font,Component.literal("F gear • H spikes • P systems • Esc close"),width/2,height-12,0xFF9DB5C9);
 }
}
