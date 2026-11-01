package dev.metallicarts.client;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.hemalurgy.ConstructMob;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.Identifier;
public final class ConstructMobRenderer extends HumanoidMobRenderer<ConstructMob,HumanoidRenderState,HumanoidModel<HumanoidRenderState>> {
 private final Identifier texture;
 public ConstructMobRenderer(EntityRendererProvider.Context context,String kind){super(context,new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)),.5f);texture=MetallicArts.id("textures/entity/"+kind+".png");}
 @Override public HumanoidRenderState createRenderState(){return new HumanoidRenderState();}
 @Override public Identifier getTextureLocation(HumanoidRenderState state){return texture;}
}
