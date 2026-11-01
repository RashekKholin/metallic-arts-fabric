package dev.metallicarts.client;
import dev.metallicarts.allomancy.AllomancyConfig;
import dev.metallicarts.domain.Art;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
/** Compact owned-metal constellation; the M screen supplies full names, fuel and controls. */
final class ArtsHud {
 private static final String[] LABELS={"St","Fe","Pw","Sn","Zn","Br","Cu","Bz","Al","Du","Cr","Ni","Au","El","Cd","Bd"};
 static void draw(GuiGraphicsExtractor g){var c=Minecraft.getInstance();var state=MetallicArtsClient.snapshot();if(state==null||!ClientPreferences.value(5))return;int cx=c.getWindow().getGuiScaledWidth()-64,cy=67;g.fill(cx-59,8,cx+59,128,0xB00B1523);g.fill(cx-59,8,cx+59,10,0xCC3AAAC8);int owned=0,active=0;for(int i=0;i<16;i++){var m=AllomancyConfig.SUPPORTED.get(i);boolean a=AdvancedArtsClient.access(state,Art.ALLOMANCY,m)>0,f=AdvancedArtsClient.access(state,Art.FERUCHEMY,m)>0;if(!a&&!f)continue;owned++;boolean burning=AllomancyClient.status.mode(i)>0;if(burning)active++;double angle=-Math.PI/2+i*Math.PI/8;int x=cx+(int)(Math.cos(angle)*46),y=cy+(int)(Math.sin(angle)*46);int color=burning?AllomancyClient.status.mode(i)==2?0xFFFFCF70:0xFF66FFFF:a?0xFF87B3D8:0xFFD6A9F1;HudLines.draw(g,cx,cy,x,y,burning?0x993AAAC8:0x4034506A);g.fill(x-9,y-6,x+10,y+7,0xE0142435);g.centeredText(c.font,Component.literal(LABELS[i]),x,y-4,color);}g.centeredText(c.font,Component.literal(active+" burning"),cx,cy-6,0xFFB8DFED);g.centeredText(c.font,Component.literal(owned+" • M"),cx,cy+5,0xFFE8D6A6);if((AllomancyClient.status.flags()&8)!=0)g.centeredText(c.font,Component.literal("AUTO METALS"),cx,132,0xFF66FFFF);}
 private ArtsHud(){}
}
