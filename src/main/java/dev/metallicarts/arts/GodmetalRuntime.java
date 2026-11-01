package dev.metallicarts.arts;

import dev.metallicarts.domain.*;
import dev.metallicarts.network.ArtsFeedback;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.*;
import java.util.*;

/** Separate god-metal burn windows; never extends the stable sixteen-metal ordinal contracts.
 * These are bounded, uncertain trajectories/actual-history echoes, not perfect knowledge of future input. */
public final class GodmetalRuntime {
 public enum Kind{ATIUM,NALATIUM,MALATIUM}
 private record Burn(ServerPlayer owner,Object level,Kind kind,int until){}
 private static final Map<UUID,Burn> BURNS=new HashMap<>();
 public static boolean burn(ServerPlayer p,Kind kind){if(!p.isAlive()||p.isSpectator()||!dev.metallicarts.hemalurgy.ConstructRuntime.canUsePowers(p)||BURNS.containsKey(p.getUUID()))return false;
  if(kind==Kind.NALATIUM&&EffectivePower.strength(p,Art.ALLOMANCY,Metal.ELECTRUM)<=0||kind==Kind.MALATIUM&&EffectivePower.strength(p,Art.ALLOMANCY,Metal.GOLD)<=0)return false;
  BURNS.put(p.getUUID(),new Burn(p,p.level(),kind,p.level().getServer().getTickCount()+dev.metallicarts.config.WorldConfig.get().godmetalBurnTicks()));return true;
 }
 public static Optional<Kind> active(ServerPlayer p){var b=BURNS.get(p.getUUID());return b!=null&&b.owner()==p&&b.level()==p.level()&&p.isAlive()&&p.level().getServer().getTickCount()<b.until()?Optional.of(b.kind()):Optional.empty();}
 public static List<String> signatures(ServerPlayer seeker,double strength){var result=new ArrayList<String>();for(var b:BURNS.values())if(b.owner()!=seeker&&b.level()==seeker.level()&&b.owner().isAlive()&&seeker.distanceToSqr(b.owner())<=Math.pow(Math.min(32,12+3*strength),2)&&AdvancedAllomancy.concealment(b.owner())<strength)result.add("Pulse "+b.kind().name().toLowerCase(Locale.ROOT)+" • "+(int)seeker.distanceTo(b.owner())+"m");return result.stream().limit(4).toList();}
 public static boolean canOpen(ServerPlayer p){return active(p).filter(k->k!=Kind.MALATIUM).isPresent();}
 public static boolean payOpening(ServerPlayer p){var b=BURNS.get(p.getUUID());if(!canOpen(p)||b.until()-p.level().getServer().getTickCount()<10)return false;BURNS.put(p.getUUID(),new Burn(p,p.level(),b.kind(),b.until()-10));return true;}
 public static void initialize(){
  ServerPlayConnectionEvents.DISCONNECT.register((h,s)->BURNS.remove(h.player.getUUID()));ServerLifecycleEvents.SERVER_STOPPING.register(s->BURNS.clear());
  ServerTickEvents.END_SERVER_TICK.register(server->{BURNS.values().removeIf(b->!b.owner().isAlive()||b.owner().isRemoved()||b.owner().level()!=b.level()||server.getTickCount()>=b.until());if(server.getTickCount()%5!=0)return;for(var b:BURNS.values())tick(b);});
 }
 private static void tick(Burn burn){var p=burn.owner();int horizon=burn.kind()==Kind.ATIUM?40:20;String cue;
  if(burn.kind()==Kind.MALATIUM){var target=AdvancedAllomancy.aimed(p,12).orElse(null);var history=target==null?List.<MemoryEntry>of():target instanceof ServerPlayer other&&!other.getAttachedOrCreate(ModRegistries.PAST_VISION_CONSENT)?List.<MemoryEntry>of():target.getAttachedOrCreate(ModRegistries.JOURNAL).history();
   if(history.isEmpty())cue="Malatium: no consenting recorded alternative is available";
   else{var memory=history.getLast();if(memory.dimension().equals(p.level().dimension().identifier().toString())){var delta=new Vec3(memory.x()+.5,memory.y(),memory.z()+.5).subtract(target.position());var echo=target.position().add(delta.length()>4?delta.normalize().scale(4):delta);for(int i=0;i<8;i++)point(p,echo.add(Math.sin(i*Math.PI/4)*.3,i%4*.5,Math.cos(i*Math.PI/4)*.3),0xCCAAFF);}cue="Malatium: a recorded alternative; no private coordinates or biography transmitted";}
  }else{int checked=0;for(var target:p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(16),e->e!=p&&e.isAlive()&&p.hasLineOfSight(e))){if(++checked>16)break;var velocity=target.getDeltaMovement();for(int t=5;t<=horizon;t+=5){var end=target.getBoundingBox().getCenter().add(velocity.scale(t));var hit=p.level().clip(new net.minecraft.world.level.ClipContext(target.getBoundingBox().getCenter(),end,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,target));point(p,hit.getLocation(),burn.kind()==Kind.ATIUM?0xDDEEFF:0xAAAACC);if(hit.getType()!=HitResult.Type.MISS)break;}}
   cue=burn.kind()+": uncertain "+horizon+"-tick trajectories • J follows a paid opening";
  }
  if(ServerPlayNetworking.canSend(p,ArtsFeedback.TYPE))ServerPlayNetworking.send(p,new ArtsFeedback(cue,""));
 }
 private static void point(ServerPlayer p,Vec3 pos,int color){if(p.level().hasChunkAt(net.minecraft.core.BlockPos.containing(pos)))p.level().sendParticles(p,new dev.metallicarts.registry.ModPresentation.Dust(color,.6f),true,false,pos.x,pos.y,pos.z,1,0,0,0,0);}
 private GodmetalRuntime(){}
}
