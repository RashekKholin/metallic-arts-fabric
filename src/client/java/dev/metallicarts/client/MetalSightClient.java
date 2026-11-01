package dev.metallicarts.client;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.allomancy.SightProjection;
import dev.metallicarts.network.MetalSight;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.*;
/** Stable camera-space rays, distance-sized diamonds, and explicitly labelled rear compass markers. */
public final class MetalSightClient {
 public static List<MetalSight.Dot> sources=List.of();private static Object level;
 public static void initialize(){ClientPlayNetworking.registerGlobalReceiver(MetalSight.TYPE,(payload,context)->{sources=payload.sources();level=context.client().level;});ClientPlayConnectionEvents.DISCONNECT.register((h,c)->{sources=List.of();level=null;});HudElementRegistry.addFirst(MetallicArts.id("metal_sight"),(g,dt)->render(g));}
 private static void render(GuiGraphicsExtractor g){var c=Minecraft.getInstance();if(c.player==null||c.level!=level||c.screen!=null||c.options.hideGui||AllomancyClient.status.mode(0)==0&&AllomancyClient.status.mode(1)==0)return;var camera=c.gameRenderer.getMainCamera();if(!camera.isInitialized())return;
  int w=c.getWindow().getGuiScaledWidth(),h=c.getWindow().getGuiScaledHeight(),ox=w/2,oy=(int)(h*.62);var inverse=new Quaternionf(camera.rotation()).conjugate();int labelled=0;
  var ordered=sources.stream().sorted(Comparator.comparingDouble(s->s.position().distanceToSqr(camera.position()))).toList();
  for(var source:ordered){var position=source.position();var entity=source.entityId()<0?null:c.level.getEntity(source.entityId());if(entity!=null){var offset=entity instanceof dev.metallicarts.feruchemy.PhysicalCargo?new net.minecraft.world.phys.Vec3(.5,.5,.5):entity.getBoundingBox().getCenter().subtract(entity.position());position=entity.getPosition(c.getDeltaTracker().getGameTimeDeltaPartialTick(false)).add(offset);}
   var delta=position.subtract(camera.position());double distance=delta.length();var local=inverse.transform(new Vector3f((float)delta.x,(float)delta.y,(float)delta.z));var p=SightProjection.project(local.x,local.y,-local.z,camera.getFov(),w/(double)h);
   int tx=(int)((p.x()+1)*w/2),ty=(int)((p.y()+1)*h/2);int alpha=(int)Math.clamp(240-distance*4,80,240),color=source.selected()?0xFF70FFFF:(alpha<<24)|(ClientPreferences.value(6)?0xFFFF44:0x3388FF);int r=(int)Math.clamp(6-distance/5,2,6);
   if(p.onScreen()){HudLines.draw(g,ox,oy,tx,ty,color);if(distance<6)HudLines.draw(g,ox+1,oy,tx+1,ty,color);}
   HudLines.draw(g,tx,ty-r,tx+r,ty,color);HudLines.draw(g,tx+r,ty,tx,ty+r,color);HudLines.draw(g,tx,ty+r,tx-r,ty,color);HudLines.draw(g,tx-r,ty,tx,ty-r,color);
   if(labelled<4||source.selected()&&labelled<12){labelled++;String label=String.format(Locale.ROOT,"%.1fm%s",distance,p.behind()?" behind":!p.onScreen()?" offscreen":"");int x=Math.clamp(tx-c.font.width(label)/2,3,Math.max(3,w-c.font.width(label)-3));int y=Math.clamp(ty+r+2,3,h-12);g.fill(x-2,y-1,x+c.font.width(label)+2,y+10,0xB0101824);g.text(c.font,Component.literal(label),x,y,source.selected()?0xFF88FFFF:0xFFBBDFFF);}
  }
 }
 private MetalSightClient(){}
}
