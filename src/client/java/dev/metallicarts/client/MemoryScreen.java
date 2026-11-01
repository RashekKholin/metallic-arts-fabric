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
 private final int slot;private int page;private String signature="",query="",alias="";private int mode;private net.minecraft.client.gui.components.EditBox search,label;
 public MemoryScreen(int slot){super(net.minecraft.network.chat.Component.translatable("ui.metallicarts.bcf01302a7c7"));this.slot=slot;AdvancedArtsClient.memoryJournal=new JsonObject();send(0,new UUID(0,0));}
 private void send(int action,UUID memory){if(ClientPlayNetworking.canSend(MemoryAction.TYPE))ClientPlayNetworking.send(new MemoryAction(action,slot,page,memory,action==3?alias:query));}
 public boolean isPauseScreen(){return false;}
 private JsonArray entries(String key){var a=AdvancedArtsClient.memoryJournal.get(key);return a!=null&&a.isJsonArray()?a.getAsJsonArray():new JsonArray();}
 protected void init(){int group=Math.min(352,width-24),left=(width-group)/2,half=(group-8)/2;signature=AdvancedArtsClient.memoryJournal.toString();
  search=addRenderableWidget(new net.minecraft.client.gui.components.EditBox(font,left,43,group-112,14,net.minecraft.network.chat.Component.translatable("ui.metallicarts.8ccc29b6aba2")));search.setMaxLength(64);search.setValue(query);search.setResponder(value->query=value);
  addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.bce06414177f"),b->{page=0;send(0,new UUID(0,0));}).bounds(left+group-108,42,108,16).build());
  label=addRenderableWidget(new net.minecraft.client.gui.components.EditBox(font,left,182,group-112,14,net.minecraft.network.chat.Component.translatable("ui.metallicarts.c191826d0c83")));label.setMaxLength(64);label.setValue(alias);label.setResponder(value->alias=value);
  addRenderableWidget(Button.builder((mode==1?net.minecraft.network.chat.Component.translatable("ui.metallicarts.b55382bbea1c"):(mode==2?net.minecraft.network.chat.Component.translatable("ui.metallicarts.0d210b36a1df"):net.minecraft.network.chat.Component.translatable("ui.metallicarts.6eeea18e16fc"))),b->{mode=(mode+1)%3;rebuildWidgets();}).bounds(left+group-108,181,108,16).build());
  for(int column=0;column<2;column++){String key=column==0?"memories":"stored";int action=column==0?1:2;var rows=entries(key);
   for(int n=0;n<rows.size();n++){var entry=rows.get(n).getAsJsonObject();String label=entry.get("label").getAsString();if(label.length()>23)label=label.substring(0,23);UUID id=UUID.fromString(entry.get("id").getAsString());addRenderableWidget(Button.builder(UiText.of(label),b->send(mode==2&&action==1?4:mode==1&&action==1?3:action,id)).bounds(left+column*(half+8),59+n*20,half,18).build());}}
  addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.50f94286ba30"),b->{page=Math.max(0,page-1);send(0,new UUID(0,0));}).bounds(width/2-100,204,94,18).build());
  addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.bc981983e7f5"),b->{page=Math.min(42,page+1);send(0,new UUID(0,0));}).bounds(width/2+6,204,94,18).build());
 }
 public void tick(){if(!signature.equals(AdvancedArtsClient.memoryJournal.toString()))rebuildWidgets();}
 public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float d){super.extractRenderState(g,x,y,d);g.centeredText(font,title,width/2,15,0xFFFFFFFF);g.centeredText(font,net.minecraft.network.chat.Component.translatable("ui.metallicarts.c680b84ba93d"),width/2,31,0xFFCCDDCC);g.centeredText(font,net.minecraft.network.chat.Component.translatable("ui.metallicarts.6b69d0a2d8a8", ((slot+1)), ((page+1))),width/2,230,0xFFAAAAAA);}
}
