package dev.metallicarts.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.animal.wolf.Wolf;
/** Render-only surrogate models; no fake players, gameplay entities, inventory or authentication changes. */
public final class ConstructAppearance {
 public interface Data{void metallicarts$appearance(int kind,int shape,boolean collapsed,Visual visual);int metallicarts$kind();Visual metallicarts$visual();}
 public record Visual(EntityRenderer renderer,EntityRenderState state,float factor){}
 private static Object level;private static Wolf wolf;private static Slime slime;
 @SuppressWarnings({"rawtypes","unchecked"}) public static void extract(LivingEntity entity,LivingEntityRenderState state){
  if(!(state instanceof Data data))return;var appearance=entity instanceof dev.metallicarts.hemalurgy.ConstructMob mob?new dev.metallicarts.network.PublicConstruct(entity.getUUID(),dev.metallicarts.hemalurgy.NpcConstructs.kind(mob).ordinal(),mob.bodyShape(),mob.collapsed()):SystemsClient.APPEARANCE.get(entity.getUUID());if(appearance==null){data.metallicarts$appearance(0,0,false,null);return;}
  Visual visual=null;
  if(appearance.kind()==3&&(appearance.collapsed()||appearance.shape()!=0)){
   var client=Minecraft.getInstance();if(client.level!=level){level=client.level;wolf=null;slime=null;}
   LivingEntity surrogate;
   if(!appearance.collapsed()&&appearance.shape()==1){if(wolf==null)wolf=EntityType.WOLF.create(client.level,EntitySpawnReason.COMMAND);surrogate=wolf;}
   else {if(slime==null){slime=EntityType.SLIME.create(client.level,EntitySpawnReason.COMMAND);if(slime!=null)slime.setSize(1,false);}surrogate=slime;}
   if(surrogate!=null){surrogate.setPos(entity.position());surrogate.setYRot(entity.getYRot());surrogate.setXRot(entity.getXRot());surrogate.yBodyRot=entity.yBodyRot;surrogate.yHeadRot=entity.yHeadRot;
    EntityRenderer renderer=client.getEntityRenderDispatcher().getRenderer(surrogate);EntityRenderState body=renderer.createRenderState(surrogate,0);float factor=state.boundingBoxHeight/Math.max(.1f,body.boundingBoxHeight);
    body.x=state.x;body.y=state.y;body.z=state.z;body.ageInTicks=state.ageInTicks;body.lightCoords=state.lightCoords;body.isInvisible=state.isInvisible;body.outlineColor=state.outlineColor;body.nameTag=state.nameTag;body.nameTagAttachment=state.nameTagAttachment;
    if(body instanceof LivingEntityRenderState living){living.bodyRot=state.bodyRot;living.xRot=state.xRot;living.yRot=state.yRot;living.walkAnimationPos=state.walkAnimationPos;living.walkAnimationSpeed=state.walkAnimationSpeed;}
    visual=new Visual(renderer,body,factor);
   }
  }
  data.metallicarts$appearance(appearance.kind(),appearance.shape(),appearance.collapsed(),visual);
 }
 @SuppressWarnings({"rawtypes","unchecked"}) public static boolean submit(LivingEntityRenderState state,PoseStack poses,SubmitNodeCollector collector,CameraRenderState camera){
  if(!(state instanceof Data data)||data.metallicarts$visual()==null)return false;var visual=data.metallicarts$visual();poses.pushPose();poses.scale(visual.factor(),visual.factor(),visual.factor());visual.renderer().submit(visual.state(),poses,collector,camera);poses.popPose();return true;
 }
 public static int tint(LivingEntityRenderState state,int original){return state instanceof Data data&&data.metallicarts$kind()==1?net.minecraft.util.ARGB.multiply(original,0xFF6688FF):original;}
 private ConstructAppearance(){}
}
