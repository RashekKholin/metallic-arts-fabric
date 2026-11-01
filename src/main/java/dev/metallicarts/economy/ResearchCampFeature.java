package dev.metallicarts.economy;
import dev.metallicarts.MetallicArts;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Small original discovery camp generated only through normal new-chunk feature placement. */
public final class ResearchCampFeature extends Feature<NoneFeatureConfiguration> {
 public ResearchCampFeature(){super(NoneFeatureConfiguration.CODEC);}
 @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context){return build(context.level(),context.origin(),context.random());}
 public static boolean build(WorldGenLevel level,BlockPos origin,RandomSource random){
  if(!dev.metallicarts.config.WorldConfig.get().researchCamps()||origin.getY()<63||origin.getY()+4>=level.getMaxY())return false;
  for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){
   var floor=origin.offset(x,-1,z);if(!level.getBlockState(floor).isSolidRender()||!level.getFluidState(floor).isEmpty()||level.getBlockEntity(floor)!=null)return false;
   for(int y=0;y<=3;y++){var pos=origin.offset(x,y,z);if(!level.getBlockState(pos).canBeReplaced()||!level.getFluidState(pos).isEmpty()||level.getBlockEntity(pos)!=null)return false;}
  }
  for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){
   level.setBlock(origin.offset(x,-1,z),Blocks.MOSSY_COBBLESTONE.defaultBlockState(),2);
   level.setBlock(origin.offset(x,3,z),Blocks.WHITE_WOOL.defaultBlockState(),2);
  }
  for(int x:new int[]{-2,2})for(int z:new int[]{-2,2})for(int y=0;y<3;y++)level.setBlock(origin.offset(x,y,z),Blocks.OAK_LOG.defaultBlockState(),2);
  level.setBlock(origin,EconomyContent.TABLE.defaultBlockState(),2);
  level.setBlock(origin.above(2),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),2);
  var chest=origin.offset(1,0,1);level.setBlock(chest,Blocks.CHEST.defaultBlockState(),2);
  if(level.getBlockEntity(chest) instanceof RandomizableContainerBlockEntity container)container.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,MetallicArts.id("chests/research_camp")),random.nextLong());
  level.setBlock(origin.offset(-1,0,1),Blocks.CRAFTING_TABLE.defaultBlockState(),2);
  level.setBlock(origin.offset(1,0,-1),Blocks.SMITHING_TABLE.defaultBlockState(),2);
  var researcher=net.minecraft.world.entity.EntityType.VILLAGER.create(level.getLevel(),net.minecraft.world.entity.EntitySpawnReason.STRUCTURE);if(researcher!=null){researcher.setPos(origin.getX()+.5,origin.getY(),origin.getZ()-1.5);researcher.setPersistenceRequired();researcher.setCustomName(net.minecraft.network.chat.Component.translatable("entity.metallicarts.researcher"));researcher.setAttached(dev.metallicarts.registry.ModRegistries.RESEARCHER,true);var source=java.util.UUID.randomUUID();researcher.setAttached(dev.metallicarts.registry.ModRegistries.SPIRITWEB,dev.metallicarts.allomancy.ReserveLedger.grant(researcher.getAttachedOrCreate(dev.metallicarts.registry.ModRegistries.SPIRITWEB),new dev.metallicarts.domain.PowerGrant(source,dev.metallicarts.domain.Art.FERUCHEMY,dev.metallicarts.domain.Metal.COPPER,dev.metallicarts.domain.PowerGrant.Origin.INNATE,source,1)));level.addFreshEntity(researcher);}
  return true;
 }
}
