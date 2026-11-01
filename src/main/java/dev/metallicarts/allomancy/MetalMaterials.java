package dev.metallicarts.allomancy;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.domain.Metal;
import dev.metallicarts.economy.EconomyContent;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.core.registries.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;
/** Known physical composition plus explicit extension/immune tags. Internal reserves are never targets. */
public final class MetalMaterials {
 public static final TagKey<net.minecraft.world.item.Item> IMMUNE_ITEMS=TagKey.create(Registries.ITEM,MetallicArts.id("pushpull_immune"));
 public static final TagKey<net.minecraft.world.level.block.Block> IMMUNE_BLOCKS=TagKey.create(Registries.BLOCK,MetallicArts.id("pushpull_immune"));
 private static final List<String> METALS=List.of("iron","steel","tin","pewter","zinc","brass","copper","bronze","chromium","nicrosil","gold","electrum","cadmium","bendalloy","silver","nickel","chromite","lead","bismuth","netherite","golden","gilded","ancient_debris","atium","nalatium","malatium","lerasium","harmonium","trellium");
 private static boolean immune(String path){return path.contains("aluminum")||path.contains("aluminium")||path.contains("duralumin")||path.contains("bauxite");}
 private static boolean known(String path){for(String part:path.split("_"))if(METALS.contains(part))return true;return path.equals("ancient_debris")||Set.of("mace","trident","crossbow","brush","spyglass","lantern","soul_lantern","lodestone","piston","sticky_piston","piston_head","moving_piston","water_cauldron","lava_cauldron","powder_snow_cauldron","anvil","chipped_anvil","damaged_anvil","chain","chains","heavy_core","bell","bucket","water_bucket","lava_bucket","milk_bucket","shield","compass","recovery_compass","clock","shears","flint_and_steel","tripwire_hook","hopper","cauldron","rail","powered_rail","detector_rail","activator_rail","lightning_rod","blast_furnace","brewing_stand","medallion").contains(path);}
 public static boolean item(ItemStack stack){if(stack.isEmpty()||stack.is(IMMUNE_ITEMS))return false;var id=BuiltInRegistries.ITEM.getKey(stack.getItem());if(immune(id.getPath()))return false;var data=stack.get(ModRegistries.METAL_DATA);if(data!=null&&(data.metal()==Metal.ALUMINUM||data.metal()==Metal.DURALUMIN))return false;var batch=stack.get(EconomyContent.COMPOSITION);if(batch!=null&&batch.parts().containsKey("aluminum"))return false;var form=EconomyContent.form(stack.getItem());return form!=null?!Set.of("aluminum","duralumin","carbon","silicon").contains(form.material()):stack.is(MetalPhysics.ITEMS)||known(id.getPath());}
 public static boolean block(BlockState state){var id=BuiltInRegistries.BLOCK.getKey(state.getBlock());return !state.is(IMMUNE_BLOCKS)&&!immune(id.getPath())&&(state.is(MetalPhysics.ANCHORS)||known(id.getPath()));}
 private MetalMaterials(){}
}
