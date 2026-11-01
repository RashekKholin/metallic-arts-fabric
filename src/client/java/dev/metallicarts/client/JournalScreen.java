package dev.metallicarts.client;

import com.google.gson.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Read actual recorded knowledge/history; Gold selection requests a server-authorized alternate-path vision. */
public class JournalScreen extends Screen {
    protected int page;private String signature="";
    public JournalScreen(){super(Component.literal("The Metallic Arts — Journal"));}
    protected JsonArray entries(){var value=AdvancedArtsClient.journal.get("history");return value!=null&&value.isJsonArray()?value.getAsJsonArray():new JsonArray();}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        var entries=entries();signature=AdvancedArtsClient.journal.toString();
        for(int n=0;n<6;n++){
            final int index=page*6+n;if(n>=entries.size())break;var entry=entries.get(n).getAsJsonObject();String label=entry.get("label").getAsString();
            if(label.length()>43)label=label.substring(0,43);
            addRenderableWidget(Button.builder(Component.literal((index+1)+" "+label),b->AdvancedArtsClient.action(2,index)).bounds(width/2-170,55+n*23,340,20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Previous"),b->{page=Math.max(0,page-1);AdvancedArtsClient.action(3,page);}).bounds(width/2-100,201,94,18).build());
        addRenderableWidget(Button.builder(Component.literal("Next"),b->{if(AdvancedArtsClient.journal.has("historyTotal")&&(page+1)*6<AdvancedArtsClient.journal.get("historyTotal").getAsInt())page++;AdvancedArtsClient.action(3,page);}).bounds(width/2+6,201,94,18).build());
    }
    @Override public void tick(){if(!signature.equals(AdvancedArtsClient.journal.toString()))rebuildWidgets();}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float delta){
        super.extractRenderState(g,x,y,delta);g.centeredText(font,title,width/2,15,0xFFFFFFFF);
        g.centeredText(font,Component.literal("Actual recorded paths • burn Gold to view an alternative"),width/2,32,0xFFDDCC99);
        if(entries().isEmpty())g.centeredText(font,Component.literal("Explore to record discoveries and paths"),width/2,90,0xFFCCCCCC);
        g.centeredText(font,Component.literal("Visions interpret recorded history; they do not invent a biography"),width/2,230,0xFFAAAAAA);
    }
}
