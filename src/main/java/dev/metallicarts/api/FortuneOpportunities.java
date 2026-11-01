package dev.metallicarts.api;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.event.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import java.util.*;
/** Explicit loaded-world opportunity providers. No chunk generation, inventory inspection or promised outcome. */
public final class FortuneOpportunities {
 public record Opportunity(BlockPos pos,String label){public Opportunity{Objects.requireNonNull(pos);Objects.requireNonNull(label);if(label.length()>96||label.chars().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Invalid opportunity label");}}
 @FunctionalInterface public interface Provider{Optional<Opportunity> find(ServerPlayer player,int budget,int radius);}
 public static final Event<Provider> EVENT=EventFactory.createArrayBacked(Provider.class,listeners->(p,budget,radius)->{int each=Math.max(1,budget/Math.max(1,listeners.length));for(var listener:listeners)try{var found=listener.find(p,each,radius);if(found.isPresent()&&valid(p,found.get().pos(),radius))return found;}catch(RuntimeException ignored){}return Optional.empty();});
 public static boolean valid(ServerPlayer p,BlockPos pos,int radius){return p.level().hasChunkAt(pos)&&p.blockPosition().distSqr(pos)<=(double)radius*radius&&dev.metallicarts.compat.OpenPacAdapter.temporalAllowed(p,p.level(),pos);}
 public static Optional<String> cue(ServerPlayer p){var policy=dev.metallicarts.config.AdvancedConfig.get().body();return EVENT.invoker().find(p,policy.opportunityProbes(),policy.opportunityRadius()).map(o->{int x=o.pos().getX()-p.getBlockX(),z=o.pos().getZ()-p.getBlockZ();String direction=Math.abs(x)>Math.abs(z)?x>0?"east":"west":z>0?"south":"north";return "Fortune: "+o.label()+" may lie "+direction+(o.pos().getY()<p.getBlockY()?" and below":"");});}
 public static void initialize(){
  EVENT.register((p,budget,radius)->{if(!p.level().isRaining()&&!p.isOnFire())return Optional.empty();int checked=0;for(int x=-radius;x<=radius;x+=2)for(int z=-radius;z<=radius;z+=2){if(++checked>budget)return Optional.empty();var pos=p.blockPosition().offset(x,0,z);if(!valid(p,pos,radius))continue;if(p.isOnFire()&&p.level().getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER))return Optional.of(new Opportunity(pos,"cool water"));if(p.level().isRaining()&&!p.level().canSeeSky(pos)&&p.level().getBlockState(pos).isAir()&&p.level().getBlockState(pos.above()).isAir()&&p.level().getBlockState(pos.below()).isSolidRender())return Optional.of(new Opportunity(pos,"dry shelter"));}return Optional.empty();});
  EVENT.register((p,budget,radius)->{int checked=0;for(var memory:p.getAttachedOrCreate(ModRegistries.JOURNAL).memories()){if(++checked>budget)break;if(!memory.kind().equals("rest")||!memory.dimension().equals(p.level().dimension().identifier().toString()))continue;var pos=new BlockPos(memory.x(),memory.y(),memory.z());if(valid(p,pos,radius))return Optional.of(new Opportunity(pos,"a remembered refuge"));}return Optional.empty();});
  EVENT.register((p,budget,radius)->{var ores=TagKey.create(Registries.BLOCK,MetallicArts.id("fortune_opportunities"));int checked=0;for(int y=-6;y<=6;y+=2)for(int x=-radius;x<=radius;x+=2)for(int z=-radius;z<=radius;z+=2){if(++checked>budget)return Optional.empty();var pos=p.blockPosition().offset(x,y,z);if(valid(p,pos,radius)&&p.level().getBlockState(pos).is(ores))return Optional.of(new Opportunity(pos,"an ore opportunity"));}return Optional.empty();});
 }
 private FortuneOpportunities(){}
}
