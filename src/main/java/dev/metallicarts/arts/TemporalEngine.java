package dev.metallicarts.arts;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.allomancy.AllomancyRuntime;
import dev.metallicarts.domain.Metal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Bounded local clocks: non-player entities and explicitly supported processing machines. */
public final class TemporalEngine {
    public record Bubble(ServerPlayer owner,Object level,Vec3 center,double radius,double rate,Metal metal){}
    private static final Map<UUID,Bubble> BUBBLES=new HashMap<>();
    private record NpcBubble(Mob owner,Vec3 center,double radius,double rate,Metal metal){}
    private static final Map<UUID,NpcBubble> NPC_BUBBLES=new HashMap<>();
    public static void updateNpc(Mob owner,Map<Metal,Double> powers){Metal metal=powers.getOrDefault(Metal.BENDALLOY,0.0)>0?Metal.BENDALLOY:Metal.CADMIUM;double power=powers.getOrDefault(metal,0.0);var old=NPC_BUBBLES.get(owner.getUUID());if(power<=0||!owner.isAlive()||owner.isRemoved()){NPC_BUBBLES.remove(owner.getUUID());return;}var policy=dev.metallicarts.config.AdvancedConfig.get().time();double radius=Math.min(policy.maximumRadius(),policy.baseRadius()+power);if(old!=null&&owner.position().distanceToSqr(old.center())>radius*radius){NpcArts.request(owner,metal,dev.metallicarts.allomancy.BurnMode.OFF);NPC_BUBBLES.remove(owner.getUUID());return;}double rate=metal==Metal.BENDALLOY?Math.min(policy.maximum(),1+policy.speedFactor()*power):Math.max(policy.minimum(),1/(1+policy.slowFactor()*power));NPC_BUBBLES.put(owner.getUUID(),new NpcBubble(owner,old==null||old.metal()!=metal?owner.position():old.center(),radius,rate,metal));}
    private static final Map<UUID,Double> CLOCKS=new HashMap<>(),PREVIOUS=new HashMap<>();
    private static final Map<UUID,Integer> STEPS=new HashMap<>();
    private record Machine(Object level,BlockPos pos,String type){}
    private static final Map<Machine,FractionalClock> MACHINES=new HashMap<>();
    private static final ThreadLocal<Boolean> EXTRA=ThreadLocal.withInitial(()->false);
    public static void update(ServerPlayer p,Map<Metal,Double> powers,int tick){
        Metal metal=powers.getOrDefault(Metal.BENDALLOY,0.0)>0?Metal.BENDALLOY:Metal.CADMIUM;
        double power=powers.getOrDefault(metal,0.0);var old=BUBBLES.get(p.getUUID());
        if(power<=0||!p.isAlive()||p.isSpectator()){stop(p);return;}
        var policy=dev.metallicarts.config.AdvancedConfig.get().time();double radius=Math.min(policy.maximumRadius(),policy.baseRadius()+power),rate=metal==Metal.BENDALLOY?Math.min(policy.maximum(),1+policy.speedFactor()*power):Math.max(policy.minimum(),1/(1+policy.slowFactor()*power));
        if(old==null||old.level()!=p.level()||old.metal()!=metal)old=new Bubble(p,p.level(),p.position(),radius,rate,metal);
        if(p.position().distanceToSqr(old.center())>radius*radius){AllomancyRuntime.stopMetal(p,metal);stop(p);return;}
        var bubble=new Bubble(p,p.level(),old.center(),radius,rate,metal);BUBBLES.put(p.getUUID(),bubble);
        if(tick%20==0)for(int n=0;n<32;n++){double a=n*Math.PI/16;var point=bubble.center().add(Math.cos(a)*radius,.2,Math.sin(a)*radius);p.level().sendParticles(new dev.metallicarts.registry.ModPresentation.Dust(metal==Metal.CADMIUM?0x7777DD:0xDDCC77,.8f),point.x,point.y,point.z,1,0,0,0,0);}
    }
    private static boolean valid(Bubble b){return b.owner().isAlive()&&!b.owner().isSpectator()&&b.owner().level()==b.level()&&AllomancyRuntime.power(b.owner(),b.metal())>0&&b.owner().position().distanceToSqr(b.center())<=b.radius()*b.radius();}
    public static double rate(ServerLevel level,Vec3 position){
        double rate=1;for(var b:BUBBLES.values())if(b.level()==level&&valid(b)&&position.distanceToSqr(b.center())<=b.radius()*b.radius()&&dev.metallicarts.compat.OpenPacAdapter.temporalAllowed(b.owner(),level,BlockPos.containing(position)))rate*=b.rate();for(var b:NPC_BUBBLES.values())if(b.owner().level()==level&&b.owner().isAlive()&&!b.owner().isRemoved()&&NpcArts.powers(b.owner()).getOrDefault(b.metal(),0.0)>0&&position.distanceToSqr(b.center())<=b.radius()*b.radius()&&dev.metallicarts.compat.OpenPacAdapter.temporalAllowed(b.owner(),level,BlockPos.containing(position)))rate*=b.rate();return Math.clamp(rate,dev.metallicarts.config.AdvancedConfig.get().time().minimum(),dev.metallicarts.config.AdvancedConfig.get().time().maximum());
    }
    public static int entitySteps(Entity entity){
        if(entity instanceof net.minecraft.world.entity.player.Player||entity.isVehicle()||!(entity.level() instanceof ServerLevel level))return 1;
        if(BUBBLES.isEmpty()&&NPC_BUBBLES.isEmpty()&&!PREVIOUS.containsKey(entity.getUUID()))return 1;
        double rate=rate(level,entity.position());Double previous=PREVIOUS.put(entity.getUUID(),rate);
        if(entity instanceof Projectile&&previous!=null&&Math.abs(previous-rate)>.001){
            double angle=Math.toRadians((entity.getUUID().hashCode()%2==0?1:-1)*dev.metallicarts.config.AdvancedConfig.get().time().boundaryDegrees());var velocity=entity.getDeltaMovement();entity.setDeltaMovement(velocity.yRot((float)angle));entity.hurtMarked=true;
        }
        double total=CLOCKS.getOrDefault(entity.getUUID(),0.0)+rate;int steps=(int)Math.floor(total);CLOCKS.put(entity.getUUID(),total-steps);STEPS.put(entity.getUUID(),steps);
        return steps;
    }
    public static int extraSteps(Entity e){return STEPS.getOrDefault(e.getUUID(),1)-1;}
    public static boolean extra(){return EXTRA.get();}
    public static void runExtra(Runnable operation){EXTRA.set(true);try{operation.run();}finally{EXTRA.remove();}}
    public static int machineSteps(ServerLevel level,BlockPos pos,String type){
        if(!dev.metallicarts.config.AdvancedConfig.get().time().machines().contains(type))return 1;
        double rate=rate(level,Vec3.atCenterOf(pos));var key=new Machine(level,pos.immutable(),type);
        if(rate==1&&!MACHINES.containsKey(key))return 1;
        if(MACHINES.size()>=4096&&!MACHINES.containsKey(key))return 1;
        return MACHINES.computeIfAbsent(key,k->new FractionalClock()).advance(level.getGameTime(),rate);
    }
    public static void cleanup(){
        MACHINES.entrySet().removeIf(e->e.getKey().level() instanceof ServerLevel l&&l.getGameTime()-e.getValue().last()>1200);
        BUBBLES.values().removeIf(b->!valid(b));NPC_BUBBLES.values().removeIf(b->!b.owner().isAlive()||b.owner().isRemoved()||NpcArts.powers(b.owner()).getOrDefault(b.metal(),0.0)<=0);
        if(CLOCKS.size()>4096){CLOCKS.clear();PREVIOUS.clear();STEPS.clear();}
    }
    public static void stop(ServerPlayer p){BUBBLES.remove(p.getUUID());}
    public static Collection<Bubble> bubbles(){return List.copyOf(BUBBLES.values());}
    public static void clear(){BUBBLES.clear();NPC_BUBBLES.clear();MACHINES.clear();CLOCKS.clear();PREVIOUS.clear();STEPS.clear();EXTRA.remove();}
    private TemporalEngine(){}
}
