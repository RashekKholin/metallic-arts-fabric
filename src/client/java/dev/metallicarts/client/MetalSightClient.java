package dev.metallicarts.client;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.network.MetalSight;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import java.util.*;
/** Blue chest-to-source rays remain visible through terrain; off-screen sources point to the screen edge. */
public final class MetalSightClient {
 public static List<MetalSight.Dot> sources=List.of();private static Object level;
 public static void initialize(){ClientPlayNetworking.registerGlobalReceiver(MetalSight.TYPE,(payload,context)->{sources=payload.sources();level=context.client().level;});ClientPlayConnectionEvents.DISCONNECT.register((h,c)->{sources=List.of();level=null;});HudElementRegistry.addFirst(MetallicArts.id("metal_sight"),(g,dt)->render(g));}
 private static void render(GuiGraphicsExtractor g){var client=Minecraft.getInstance();if(client.player==null||client.level!=level||client.screen!=null||client.options.hideGui||AllomancyClient.status.mode(0)==0&&AllomancyClient.status.mode(1)==0)return;var camera=client.gameRenderer.getMainCamera();if(!camera.isInitialized())return;int w=client.getWindow().getGuiScaledWidth(),h=client.getWindow().getGuiScaledHeight(),ox=w/2,oy=(int)(h*.62);var matrix=camera.getViewRotationProjectionMatrix(new Matrix4f());
  for(var source:sources){var position=source.position();var entity=source.entityId()<0?null:client.level.getEntity(source.entityId());if(entity!=null){var offset=entity instanceof dev.metallicarts.feruchemy.PhysicalCargo?new net.minecraft.world.phys.Vec3(.5,.5,.5):entity.getBoundingBox().getCenter().subtract(entity.position());position=entity.getPosition(client.getDeltaTracker().getGameTimeDeltaPartialTick(false)).add(offset);}var p=position.subtract(camera.position());var projected=matrix.transform(new Vector4f((float)p.x,(float)p.y,(float)p.z,1));float divisor=Math.max(.001f,Math.abs(projected.w));float x=projected.x/divisor,y=-projected.y/divisor;if(projected.w<=0){x=-x;y=-y;float edge=Math.max(Math.abs(x)/.94f,Math.abs(y)/.90f);if(edge<.001f){x=.94f;y=0;}else{x/=edge;y/=edge;}}float scale=Math.max(1,Math.max(Math.abs(x)/.94f,Math.abs(y)/.90f));int tx=(int)((x/scale+1)*w/2),ty=(int)((y/scale+1)*h/2);int color=source.selected()?0xFF55FFFF:ClientPreferences.value(6)?0xFFFFFF44:0xB03388FF;line(g,ox,oy,tx,ty,color);g.fill(tx-1,ty-1,tx+2,ty+2,color);}
 }
 private static void line(GuiGraphicsExtractor g,int x,int y,int tx,int ty,int color){HudLines.draw(g,x,y,tx,ty,color);}
 private MetalSightClient(){}
}
