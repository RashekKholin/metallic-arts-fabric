package dev.metallicarts.client;

import dev.metallicarts.hemalurgy.*;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.Locale;

/** Anatomy layout is an explicitly labelled game interpretation, with separate lethal-extraction Intent. */
public final class HemalurgyScreen extends Screen {
    private int selected,power;private boolean intent;
    private static final int[][] POS={{-42,91},{42,91},{-20,51},{20,51},{-12,90},{12,90},{-42,123},{42,123},{-12,153},{12,153},{0,123},{0,71}};
    public HemalurgyScreen(){super(net.minecraft.network.chat.Component.translatable("ui.metallicarts.22b245f84a27"));}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        for(int i=0;i<12;i++){
            final int slot=i;
            addRenderableWidget(Button.builder(UiText.of(Integer.toString(i+1)),b->{selected=slot;rebuildWidgets();})
                .bounds(width/2+POS[i][0]-9,POS[i][1],18,16).build());
        }
        int side=Math.min(114,(width-124)/2),right=width-side-8;
        addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.c4dd3c8cdd8d"),b->{power=Math.floorMod(power-1,StolenPower.choices().size());}).bounds(right,84,24,16).build());
        addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.091385be99b4"),b->{power=(power+1)%StolenPower.choices().size();}).bounds(right+28,84,24,16).build());
        addRenderableWidget(Button.builder((intent?net.minecraft.network.chat.Component.translatable("ui.metallicarts.bc0349b58dd9"):net.minecraft.network.chat.Component.translatable("ui.metallicarts.79250bc37812")),b->{intent=!intent;rebuildWidgets();}).bounds(right,106,side,18).build());
        int x=(width-304)/2;
        addRenderableWidget(Button.builder((BindingPoints.ALL.get(selected).critical()?net.minecraft.network.chat.Component.translatable("ui.metallicarts.b8b34939b10a"):net.minecraft.network.chat.Component.translatable("ui.metallicarts.9ed83504bc4a")),b->send(BindingPoints.ALL.get(selected).critical()?6:1)).bounds(x,188,96,18).build());
        addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.e963907dac5c"),b->send(2)).bounds(x+100,188,96,18).build());
        var extract=addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.21985b13381c"),b->send(3)).bounds(x+200,188,104,18).build());extract.active=intent;
        addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.f43fa0e58626"),b->send(7)).bounds(x,210,96,18).build());
        addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.62faf27549e2"),b->send(4)).bounds(x+100,210,96,18).build());
        addRenderableWidget(Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.28afd7b36a83"),b->send(5)).bounds(x+200,210,104,18).build());
    }
    private void send(int action){HemalurgyClient.send(action,selected,power);}
    @Override public boolean keyPressed(KeyEvent event){
        if(ContextKeys.A.matches(event)){minecraft.setScreen(new SpiritwebScreen());return true;}
        return super.keyPressed(event);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){
        int center=width/2,side=Math.min(114,(width-124)/2),right=width-side-8;
        g.fill(center-12,47,center+12,73,0xAA657988);g.fill(center-23,76,center+23,139,0xAA657988);
        g.fill(center-44,81,center-27,142,0xAA657988);g.fill(center+27,81,center+44,142,0xAA657988);
        g.fill(center-21,139,center-2,178,0xAA657988);g.fill(center+2,139,center+21,178,0xAA657988);
        super.extractRenderState(g,mouseX,mouseY,delta);
        g.centeredText(font,title,center,12,0xFFFFFFFF);
        g.centeredText(font,net.minecraft.network.chat.Component.translatable("ui.metallicarts.ecc8b50daf94"),center,28,0xFFFFAA77);
        var point=BindingPoints.ALL.get(selected);int y=48;
        for(var line:font.split(net.minecraft.network.chat.Component.translatable("ui.metallicarts.265fc525514e", ((selected+1)), (point.name())),side)){g.text(font,line,8,y,0xFFCCDDFF);y+=10;}
        g.text(font,point.region(),8,y+2,0xFFBBBBBB);y+=16;
        if(point.critical()){g.text(font,"Dangerous removal",8,y,0xFFFF7777);y+=12;}
        var snapshot=HemalurgyClient.snapshot;
        if(snapshot!=null){
            var data=snapshot.equipment().item(selected).get(ModRegistries.SPIKE);
            var detail=data==null?"Empty binding point":data.power().label()+" • "+String.format(Locale.ROOT,"%.3f",data.potency())+(data.drained()?" • spent":"");
            for(var line:font.split(UiText.of(detail),side)){g.text(font,line,8,y,0xFFBBDDCC);y+=10;}
            g.text(font,"Vulnerability: "+snapshot.vulnerability(),8,151,0xFFCCAAAA);
            g.text(font,"Consent: "+(snapshot.consent()?"ON":"OFF"),8,163,0xFFCCAAAA);
            int messageY=132;
            for(var line:font.split(UiText.of(snapshot.message()),side)){if(messageY>174)break;g.text(font,line,right,messageY,0xFFFFCC99);messageY+=10;}
        }
        y=49;
        for(var line:font.split(UiText.of(StolenPower.choices().get(power).label()),side)){g.text(font,line,right,y,0xFFCCDDEE);y+=10;}
        for(var line:font.split(UiText.of(StolenPower.choices().get(power).implementation()),side)){g.text(font,line,right,y,0xFFCCAA88);y+=10;}
        g.centeredText(font,net.minecraft.network.chat.Component.translatable("ui.metallicarts.e362cad7730d"),center,231,0xFFAAAAAA);
    }
}
