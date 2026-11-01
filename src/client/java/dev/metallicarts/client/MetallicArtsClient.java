package dev.metallicarts.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.domain.Spiritweb;
import dev.metallicarts.network.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import dev.metallicarts.economy.EconomyContent;
import net.minecraft.client.gui.screens.MenuScreens;

public final class MetallicArtsClient implements ClientModInitializer {
    static Spiritweb snapshot;
    public static Spiritweb snapshot() { return snapshot; }
    @Override public void onInitializeClient() {
        AllomancyClient.initialize();
        FeruchemyClient.initialize();
        HemalurgyClient.initialize();
        AdvancedArtsClient.initialize();
        SystemsClient.initialize();
        ContextKeys.initialize();
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(dev.metallicarts.hemalurgy.NpcConstructs.KOLOSS,c->new ConstructMobRenderer(c,"koloss"));
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(dev.metallicarts.hemalurgy.NpcConstructs.INQUISITOR,c->new ConstructMobRenderer(c,"inquisitor"));
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(dev.metallicarts.hemalurgy.NpcConstructs.KANDRA,c->new ConstructMobRenderer(c,"kandra"));
        net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.getInstance().register(dev.metallicarts.registry.ModPresentation.ART_DUST,sprites->{var provider=new net.minecraft.client.particle.DustParticle.Provider(sprites);return(options,level,x,y,z,vx,vy,vz,random)->{if(!ClientPreferences.value(4))return null;int color=ClientPreferences.value(6)?0xFFFF66:options.color();return provider.createParticle(new net.minecraft.core.particles.DustParticleOptions(color,options.scale()),level,x,y,z,vx,vy,vz,random);};});
        net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.register((player,level,hand)->{if(level.isClientSide()&&player.getItemInHand(hand).is(EconomyContent.ITEMS.get("field_guide"))){net.minecraft.client.Minecraft.getInstance().setScreen(new GuideScreen());return net.minecraft.world.InteractionResult.SUCCESS;}return net.minecraft.world.InteractionResult.PASS;});
        MenuScreens.register(EconomyContent.TABLE_MENU, MetallurgyScreen::new);
        var category = KeyMapping.Category.register(MetallicArts.id("general"));
        KeyMapping open = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.metallicarts.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, category));
        ClientPlayNetworking.registerGlobalReceiver(SpiritwebSnapshot.TYPE, (payload, context) -> snapshot = payload.state());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> snapshot = null);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (open.consumeClick()) {
                if (client.player == null || client.screen != null) continue;
                if (ClientPlayNetworking.canSend(RequestSpiritweb.TYPE))
                    ClientPlayNetworking.send(new RequestSpiritweb(RequestSpiritweb.PROTOCOL));
                client.setScreen(new SpiritwebScreen());
            }
        });
    }
}
