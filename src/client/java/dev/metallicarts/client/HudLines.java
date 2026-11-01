package dev.metallicarts.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
/** One transformed quad per line, rather than hundreds of individual pixel draw commands. */
final class HudLines {
 static void draw(GuiGraphicsExtractor g,float x,float y,float tx,float ty,int color){float dx=tx-x,dy=ty-y;int length=(int)Math.ceil(Math.sqrt(dx*dx+dy*dy));if(length<=0)return;var pose=g.pose();pose.pushMatrix();pose.translate(x,y);pose.rotate((float)Math.atan2(dy,dx));g.fill(0,0,length,1,color);pose.popMatrix();}
 private HudLines(){}
}
