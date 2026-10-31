package dev.metallicarts.client;

import dev.metallicarts.equipment.ContactEquipment;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.Locale;

/** Twelve dedicated contact slots. Buttons request atomic server inventory transactions. */
public final class MetalmindScreen extends Screen {
    private int selected,rate=1,sense;private boolean wheel;
    private final String[] senses={"sight","hearing","touch","smell","taste"};
    private int gridWidth,left,cell;
    public MetalmindScreen(){super(Component.literal("The Metallic Arts — Metalminds"));}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        gridWidth=Math.min(372,width-16);left=(width-gridWidth)/2;cell=gridWidth/3;
        for(int i=0;i<12;i++){
            final int slot=i;
            double angle=-Math.PI/2+i*Math.PI/6;
            int x=wheel?width/2+(int)Math.round(Math.cos(angle)*70)-11:left+(i%3)*cell;
            int y=wheel?121+(int)Math.round(Math.sin(angle)*70)-9:46+(i/3)*38;
            addRenderableWidget(Button.builder(Component.literal(wheel?Integer.toString(i+1):(i+1)+" "+ContactEquipment.NAMES.get(i)),button->{
                if(selected!=slot&&FeruchemyClient.snapshot!=null)rate=FeruchemyClient.snapshot.rates().get(slot);
                selected=slot;
            }).bounds(x,y,wheel?22:cell-4,18).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Tin: sight"),button->{
            sense=(sense+1)%5;button.setMessage(Component.literal("Tin: "+senses[sense]));
        }).bounds(width-120,26,112,16).build());
        int x=(width-288)/2;
        addRenderableWidget(Button.builder(Component.literal("Equip held"),b->send(0,0)).bounds(x,208,60,18).build());x+=64;
        addRenderableWidget(Button.builder(Component.literal("Unequip"),b->send(1,0)).bounds(x,208,60,18).build());x+=64;
        addRenderableWidget(Button.builder(Component.literal("Store"),b->{rate=Math.min(3,rate);send(2,1);}).bounds(x,208,42,18).build());x+=46;
        addRenderableWidget(Button.builder(Component.literal("Off"),b->send(2,0)).bounds(x,208,28,18).build());x+=32;
        addRenderableWidget(Button.builder(Component.literal("Tap"),b->send(2,2)).bounds(x,208,34,18).build());x+=38;
        addRenderableWidget(Button.builder(Component.literal("-"),b->rate=Math.max(1,rate-1)).bounds(x,208,20,18).build());x+=24;
        addRenderableWidget(Button.builder(Component.literal("+"),b->rate=Math.min(5,rate+1)).bounds(x,208,20,18).build());
    }
    private void send(int action,int mode){FeruchemyClient.action(action,selected,sense,mode,rate);}
    @Override public boolean keyPressed(KeyEvent event){
        if(event.key()==GLFW.GLFW_KEY_W){wheel=!wheel;rebuildWidgets();return true;}
        if(event.key()==GLFW.GLFW_KEY_A){minecraft.setScreen(new SpiritwebScreen());return true;}
        return super.keyPressed(event);
    }
    private String compact(long value){
        if(value>=1_000_000_000)return String.format(Locale.ROOT,"%.1fB",value/1_000_000_000.0);
        if(value>=1_000_000)return String.format(Locale.ROOT,"%.1fM",value/1_000_000.0);
        if(value>=1000)return String.format(Locale.ROOT,"%.1fk",value/1000.0);
        return Long.toString(value);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTick){
        if(!wheel)for(int i=0;i<12;i++)g.fill(left+(i%3)*cell-1,45+(i/3)*38,left+(i%3+1)*cell-5,84+(i/3)*38,i==selected?0xAA334455:0x99181820);
        else {
            for(int n=0;n<90;n++){
                double a=n*Math.PI/45;int x=width/2+(int)Math.round(Math.cos(a)*70),y=121+(int)Math.round(Math.sin(a)*70);
                g.fill(x-1,y-1,x+1,y+1,0xFF77AACC);
            }
            g.fill(width/2-60,98,width/2+60,155,0xCC17212A);
        }
        super.extractRenderState(g,mouseX,mouseY,partialTick);
        g.centeredText(font,title,width/2,10,0xFFFFFFFF);
        g.text(font,"Slot "+(selected+1)+" | rate "+new String[]{"25%","50%","75%/100%","200%","400%"}[rate-1],8,30,0xFFCCDDEE);
        var state=FeruchemyClient.snapshot;
        if(state==null){g.centeredText(font,Component.literal("Waiting for server equipment"),width/2,100,0xFFFFFFFF);return;}
        for(int i=0;i<12;i++){
            if(wheel&&i!=selected)continue;
            var stack=state.equipment().item(i);var data=stack.get(ModRegistries.METALMIND);
            int x=wheel?width/2-55:left+(i%3)*cell+3,y=wheel?116:65+(i/3)*38;
            if(wheel)g.centeredText(font,Component.literal(ContactEquipment.NAMES.get(i)),width/2,102,0xFFFFFFFF);
            if(data==null){g.text(font,"empty",x,y,0xFF999999);continue;}
            String mode=switch(state.modes().get(i)){case 1->"store";case 2->"tap";default->"off";};
            g.text(font,data.attribute().id()+" • "+mode,x,y,0xFFFFFFFF);
            g.text(font,compact(data.charge())+"/"+compact(data.capacity()),x,y+9,0xFF88CCAA);
            int bar=wheel?110:cell-10;
            g.fill(x,y+18,x+bar,y+19,0xFF333B40);
            g.fill(x,y+18,x+(int)(bar*data.charge()/data.capacity()),y+19,0xFF66CC99);
        }
        String message=state.message().isEmpty()?"A: Allomancy • W: grid/wheel • hold jewelry to equip • Esc: close":state.message();
        if(font.width(message)>width-16)message=font.plainSubstrByWidth(message,width-28)+"…";
        g.centeredText(font,Component.literal(message),width/2,231,state.message().isEmpty()?0xFFAAAAAA:0xFFFFBB77);
    }
}
