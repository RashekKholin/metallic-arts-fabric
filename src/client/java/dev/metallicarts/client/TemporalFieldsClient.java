package dev.metallicarts.client;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.network.TemporalFields;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import java.util.*;
/** Continuous latitude/meridian shell, visible as a full sphere from outside and a boundary from inside. */
public final class TemporalFieldsClient {
 public static List<TemporalFields.Field> fields=List.of();private static List<Edge> edges=List.of();private static Object level;
 private record Edge(Vec3 a,Vec3 b,int color){}
 public static void initialize(){ClientPlayNetworking.registerGlobalReceiver(TemporalFields.TYPE,(payload,context)->{fields=payload.fields();level=context.client().level;var mesh=new ArrayList<Edge>();for(var field:fields)build(field,mesh);edges=List.copyOf(mesh);});ClientPlayConnectionEvents.DISCONNECT.register((h,c)->{fields=List.of();edges=List.of();level=null;});HudElementRegistry.addFirst(MetallicArts.id("temporal_surfaces"),(g,dt)->{var client=Minecraft.getInstance();if(client.player==null||client.level!=level||client.screen!=null||client.options.hideGui||!ClientPreferences.value(4))return;var camera=client.gameRenderer.getMainCamera();if(!camera.isInitialized())return;var matrix=camera.getViewRotationProjectionMatrix(new Matrix4f());int w=client.getWindow().getGuiScaledWidth(),h=client.getWindow().getGuiScaledHeight();g.enableScissor(0,0,w,h);for(var edge:edges){var a=project(matrix,edge.a().subtract(camera.position()));var b=project(matrix,edge.b().subtract(camera.position()));if(a.w<=.02f||b.w<=.02f)continue;float ax=(a.x/a.w+1)*w/2,ay=(1-a.y/a.w)*h/2,bx=(b.x/b.w+1)*w/2,by=(1-b.y/b.w)*h/2;if(!Float.isFinite(ax+ay+bx+by))continue;HudLines.draw(g,ax,ay,bx,by,ClientPreferences.value(6)?0x99FFFFFF:edge.color());}g.disableScissor();});}
 private static Vector4f project(Matrix4f m,Vec3 p){return m.transform(new Vector4f((float)p.x,(float)p.y,(float)p.z,1));}
 private static Vec3 latitude(TemporalFields.Field f,double phi,double a){return f.center().add(f.radius()*Math.cos(phi)*Math.cos(a),f.radius()*Math.sin(phi),f.radius()*Math.cos(phi)*Math.sin(a));}
 private static void build(TemporalFields.Field field,List<Edge> mesh){int color=field.fast()?0x88E3C875:0xCC3377FF;for(int ring=-2;ring<=2;ring++)for(int n=0;n<32;n++)mesh.add(new Edge(latitude(field,ring*Math.PI/6,n*Math.PI/16),latitude(field,ring*Math.PI/6,(n+1)*Math.PI/16),color));for(int meridian=0;meridian<4;meridian++)for(int n=0;n<32;n++)mesh.add(new Edge(latitude(field,n*Math.PI/16,meridian*Math.PI/4),latitude(field,(n+1)*Math.PI/16,meridian*Math.PI/4),color));}
 private TemporalFieldsClient(){}
}
