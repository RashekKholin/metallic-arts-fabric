package dev.metallicarts.client;
import dev.metallicarts.network.TemporalFields;
import net.fabricmc.fabric.api.client.networking.v1.*;
/** Public geometry is retained for UI/tests; shells themselves are real world-space particles. */
public final class TemporalFieldsClient {
 public static java.util.List<TemporalFields.Field> fields=java.util.List.of();
 private static int ticks;private static Object level;
 public static void initialize(){ClientPlayNetworking.registerGlobalReceiver(TemporalFields.TYPE,(payload,context)->{fields=payload.fields();level=context.client().level;});ClientPlayConnectionEvents.DISCONNECT.register((h,c)->{fields=java.util.List.of();level=null;});net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(c->{if(c.level==null||c.level!=level||c.player==null||++ticks%6!=0||!ClientPreferences.value(4))return;int budget=2048;for(var field:fields){int count=Math.min(budget,field.fast()?320:640);budget-=count;if(count==0)break;var dust=new dev.metallicarts.registry.ModPresentation.Dust(field.fast()?0xFFD477:0x66CCFF,1.15f);for(var point:dev.metallicarts.arts.TemporalEngine.particleShell(field.center(),field.radius(),count,ticks*.03))c.level.addParticle(dust,point.x,point.y,point.z,0,0,0);
   // Extra particles on the true perspective silhouette make the spherical extent readable.
   var toViewer=c.gameRenderer.getMainCamera().position().subtract(field.center());double distance=toViewer.length();if(distance>field.radius()+.1){var normal=toViewer.normalize();var u=normal.cross(Math.abs(normal.y)>.9?new net.minecraft.world.phys.Vec3(1,0,0):new net.minecraft.world.phys.Vec3(0,1,0)).normalize();var v=normal.cross(u);double offset=field.radius()*field.radius()/distance,rim=Math.sqrt(field.radius()*field.radius()-offset*offset);var center=field.center().add(normal.scale(offset));int points=Math.min(96,budget);budget-=points;for(int i=0;i<points;i++){double a=2*Math.PI*i/points;var p=center.add(u.scale(rim*Math.cos(a))).add(v.scale(rim*Math.sin(a)));c.level.addParticle(dust,p.x,p.y,p.z,0,0,0);}}
  }});}
 private TemporalFieldsClient(){}
}
