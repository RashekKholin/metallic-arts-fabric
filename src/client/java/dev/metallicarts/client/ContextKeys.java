package dev.metallicarts.client;
import dev.metallicarts.MetallicArts;
import net.minecraft.client.KeyMapping;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import org.lwjgl.glfw.GLFW;
/** UI-only bindings. They do not consume movement/offhand inputs while the world screen is active. */
public final class ContextKeys {
 private static final KeyMapping.Category CATEGORY=KeyMapping.Category.register(MetallicArts.id("context"));
 public static final KeyMapping LAYOUT=key("layout",GLFW.GLFW_KEY_W),F=key("feruchemy_menu",GLFW.GLFW_KEY_F),H=key("hemalurgy_menu",GLFW.GLFW_KEY_H),A=key("allomancy_menu",GLFW.GLFW_KEY_A),N=key("power_type",GLFW.GLFW_KEY_N),K=key("memory_menu",GLFW.GLFW_KEY_K),U=key("unseal",GLFW.GLFW_KEY_U),PREVIOUS=key("previous_page",GLFW.GLFW_KEY_LEFT_BRACKET),NEXT=key("next_page",GLFW.GLFW_KEY_RIGHT_BRACKET);
 private static KeyMapping key(String name,int code){return KeyMappingHelper.registerKeyMapping(new KeyMapping("key.metallicarts."+name,com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,code,CATEGORY));}
 public static void initialize(){}
 private ContextKeys(){}
}
