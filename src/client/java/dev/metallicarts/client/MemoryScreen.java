package dev.metallicarts.client;
import com.google.gson.*;
import dev.metallicarts.network.MemoryAction;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.UUID;
public final class MemoryScreen extends Screen {
 private final int slot;private int page;private String signature="";
 public MemoryScreen(int slot){super(Component.literal("Coppermind — move access to a recorded memory"));this.slot=slot;AdvancedArtsClient.memoryJournal=new JsonObject();send(0,new UUID(0,0));}
 private void send(int action,UUID memory){if(ClientPlayNetworking.canSend(MemoryAction.TYPE))ClientPlayNetworking.send(new MemoryAction(action,slot,page,memory));}
 public boolean isPauseScreen(){return false;}
 private JsonArray entries(String key){var a=AdvancedArtsClient.memoryJournal.get(key);return a!=null&&a.isJsonArray()?a.getAsJsonArray():new JsonArray();}
 protected void init(){signature=AdvancedArtsClient.memoryJournal.toString();
  for(int column=0;column<2;column++){String key=column==0?"memories":"stored";int action=column==0?1:2;var rows=entries(key);
   for(int n=0;n<rows.size();n++){var entry=rows.get(n).getAsJsonObject();String label=entry.get("label").getAsString();if(label.length()>23)label=label.substring(0,23);UUID id=UUID.fromString(entry.get("id").getAsString());addRenderableWidget(Button.builder(Component.literal(label),b->send(action,id)).bounds(width/2-176+column*180,58+n*23,172,20).build());}}
  addRenderableWidget(Button.builder(Component.literal("Previous"),b->{page=Math.max(0,page-1);send(0,new UUID(0,0));}).bounds(width/2-100,204,94,18).build());
  addRenderableWidget(Button.builder(Component.literal("Next"),b->{page=Math.min(42,page+1);send(0,new UUID(0,0));}).bounds(width/2+6,204,94,18).build());
 }
 public void tick(){if(!signature.equals(AdvancedArtsClient.memoryJournal.toString()))rebuildWidgets();}
 public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float d){super.extractRenderState(g,x,y,d);g.centeredText(font,title,width/2,15,0xFFFFFFFF);g.centeredText(font,Component.literal("Accessible → store                     Stored → restore"),width/2,38,0xFFCCDDCC);g.centeredText(font,Component.literal("Slot "+(slot+1)+" • page "+(page+1)+" • no generic charge or invented memories"),width/2,230,0xFFAAAAAA);}
}
