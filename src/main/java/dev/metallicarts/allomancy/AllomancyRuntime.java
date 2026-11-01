package dev.metallicarts.allomancy;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.domain.*;
import dev.metallicarts.network.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;
import java.util.*;

/** Single server-owned update loop. Active modes never persist across player/world replacement. */
public final class AllomancyRuntime {
    public static AllomancyConfig config;
    private static final Map<UUID,Session> SESSIONS=new HashMap<>();
    private static final class Session {
        final ServerPlayer player;final Object level;
        final BurnEngine engine=new BurnEngine(Set.copyOf(AllomancyConfig.SUPPORTED));
        final LinkedHashMap<String,MetalPhysics.Target> selected=new LinkedHashMap<>();
        final Map<String,Integer> missingSince=new HashMap<>();
        List<MetalPhysics.Target> nearby=List.of();
        Map<Metal,Double> lastPowers=Map.of();
        int preparedTick=Integer.MIN_VALUE;
        int requests,window,held,heartbeat,pewterTicks,fatigue,lastModes=-1,lastFlags=-1,lastToss=-20;boolean overload,dirty,explicitSelection,autoSelect;
        Session(ServerPlayer p){player=p;level=p.level();}
    }
    private static Session session(ServerPlayer p) {
        Session previous=SESSIONS.get(p.getUUID());
        if(previous!=null&&(previous.player!=p||previous.level!=p.level())){
            clear(previous);SESSIONS.remove(p.getUUID());previous=null;
            ModNetworking.send(p);
        }
        if(previous==null){previous=new Session(p);SESSIONS.put(p.getUUID(),previous);}
        return previous;
    }
    public static void initialize() {
        config=AllomancyConfig.load();
        CoinProjectile.initialize();
        PayloadTypeRegistry.serverboundPlay().register(BurnIntent.TYPE,BurnIntent.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ForceIntent.TYPE,ForceIntent.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(TossCoin.TYPE,TossCoin.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SelectionIntent.TYPE,SelectionIntent.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(AllomancyStatus.TYPE,AllomancyStatus.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(MetalSight.TYPE,MetalSight.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TemporalFields.TYPE,TemporalFields.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BurnIntent.TYPE,(payload,context)->{
            request(context.player(),payload.metal(),payload.mode(),context.server().getTickCount());
        });
        ServerPlayNetworking.registerGlobalReceiver(ForceIntent.TYPE,(payload,context)->{
            var s=session(context.player());
            if(!allow(s,context.server().getTickCount())||payload.flags()<0||payload.flags()>3)return;
            s.held=payload.flags();s.heartbeat=context.server().getTickCount();
        });
        ServerPlayNetworking.registerGlobalReceiver(TossCoin.TYPE,(payload,context)->{
            var s=session(context.player());int tick=context.server().getTickCount();
            if(!allow(s,tick)||tick-s.lastToss<4)return;
            s.lastToss=tick;CoinProjectile.toss(context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(SelectionIntent.TYPE,(payload,context)->
            select(context.player(),payload.action(),context.server().getTickCount()));
        ServerTickEvents.END_SERVER_TICK.register(server->{
            for(var player:server.getPlayerList().getPlayers())dev.metallicarts.hemalurgy.HemalurgyRuntime.tick(player,server.getTickCount());
            for(var player:server.getPlayerList().getPlayers())dev.metallicarts.hemalurgy.ConstructRuntime.tick(player,server.getTickCount());
            for(var player:server.getPlayerList().getPlayers())dev.metallicarts.feruchemy.FeruchemyRuntime.tick(player,server.getTickCount());
            for(var player:server.getPlayerList().getPlayers())prepare(player,server.getTickCount());
            dev.metallicarts.arts.NpcFeruchemy.frame(server);
            dev.metallicarts.arts.NpcArts.prepare(server);
            dev.metallicarts.arts.AdvancedAllomancy.frame(server);
            try(var frame=MetalPhysics.beginFrame()){
                dev.metallicarts.arts.NpcArts.apply();
                for(var player:server.getPlayerList().getPlayers())tick(player,server.getTickCount());
            }
            dev.metallicarts.arts.AdvancedAllomancy.finishFrame();
            dev.metallicarts.arts.TemporalPlayers.frame(server);
            dev.metallicarts.hemalurgy.ConstructRuntime.controlFrame(server.getTickCount());
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->stop(handler.player));
        ServerLifecycleEvents.SERVER_STOPPING.register(server->{SESSIONS.values().forEach(AllomancyRuntime::clear);SESSIONS.clear();ConnectedMetals.clear();dev.metallicarts.arts.NpcArts.clear();});
    }
    private static boolean allow(Session s,int tick){
        if(tick-s.window>=20){s.window=tick;s.requests=0;}
        return ++s.requests<=30;
    }
    public static boolean request(ServerPlayer player,int metal,int mode,int tick) {
        var s=session(player);
        if(!allow(s,tick)||metal<0||metal>=AllomancyConfig.SUPPORTED.size()||mode<0||mode>2||!player.isAlive()||player.isSpectator()||!dev.metallicarts.hemalurgy.ConstructRuntime.canUsePowers(player))return false;
        var type=AllomancyConfig.SUPPORTED.get(metal);
        if(mode>0&&(type==Metal.CADMIUM&&(s.engine.mode(Metal.BENDALLOY)!=BurnMode.OFF||s.lastPowers.containsKey(Metal.BENDALLOY))||type==Metal.BENDALLOY&&(s.engine.mode(Metal.CADMIUM)!=BurnMode.OFF||s.lastPowers.containsKey(Metal.CADMIUM))))return false;
        if(mode==0){stopMetal(player,type);return true;}
        if(ReserveLedger.ordinary(player.getAttachedOrCreate(ModRegistries.SPIRITWEB),type)==0&&s.lastPowers.containsKey(type)){stopMetal(player,type);return true;}
        return s.engine.request(player.getAttachedOrCreate(ModRegistries.SPIRITWEB),type,BurnMode.from(mode),m->dev.metallicarts.arts.EffectivePower.strength(player,Art.ALLOMANCY,m));
    }
    public static boolean select(ServerPlayer player,int action,int tick) {
        var s=session(player);
        if(!allow(s,tick)||action<0||action>2||!player.isAlive()||player.isSpectator())return false;
        if(action==1){s.selected.clear();s.missingSince.clear();s.explicitSelection=false;s.autoSelect=false;return true;}
        if(action==2){s.autoSelect=!s.autoSelect;s.explicitSelection=true;return true;}
        if(s.engine.mode(Metal.STEEL)==BurnMode.OFF&&s.engine.mode(Metal.IRON)==BurnMode.OFF&&!s.lastPowers.containsKey(Metal.STEEL)&&!s.lastPowers.containsKey(Metal.IRON))return false;
        var target=MetalPhysics.aimed(player,config);
        if(target.isEmpty())return false;
        var key=MetalPhysics.key(target.get());
        if(s.selected.remove(key)!=null)return true;
        if(s.selected.size()>=(s.autoSelect?64:8))return false;
        s.selected.put(key,target.get());s.explicitSelection=true;return true;
    }
    /** Push and Pull are separate intents; holding both applies their signed difference once. */
    public static double netForce(int held,Map<Metal,Double> powers){return ((held&1)!=0?powers.getOrDefault(Metal.STEEL,0.0):0)-((held&2)!=0?powers.getOrDefault(Metal.IRON,0.0):0);}
    public static void autoSelectCoin(ServerPlayer p,CoinProjectile coin){if(power(p,Metal.STEEL)<=0&&power(p,Metal.IRON)<=0)return;var s=session(p);var target=MetalPhysics.refresh(p,new MetalPhysics.Target(coin.position(),coin,1,false),config);if(target.isEmpty())return;if(s.selected.size()>=8){var key=s.selected.keySet().iterator().next();s.selected.remove(key);s.missingSince.remove(key);}s.selected.put(MetalPhysics.key(target.get()),target.get());s.explicitSelection=true;}
    public static void recoverCoin(CoinProjectile coin,net.minecraft.world.entity.item.ItemEntity dropped){if(dropped==null)return;for(var s:SESSIONS.values()){String old="entity:"+coin.getUUID();if(s.selected.remove(old)!=null){s.missingSince.remove(old);var target=new MetalPhysics.Target(dropped.getBoundingBox().getCenter(),dropped,1,false);s.selected.put(MetalPhysics.key(target),target);}}}
    public static List<MetalPhysics.Target> selections(ServerPlayer p){return List.copyOf(session(p).selected.values());}
    public static void stop(ServerPlayer p) {var s=SESSIONS.remove(p.getUUID());if(s!=null)clear(s);}
    private static void clear(Session s){dev.metallicarts.arts.TemporalPlayers.clear(s.player);s.engine.stopAll();s.lastPowers=Map.of();s.selected.clear();s.missingSince.clear();s.nearby=List.of();s.explicitSelection=false;s.autoSelect=false;s.held=0;s.pewterTicks=0;s.fatigue=0;s.overload=false;removeModifiers(s.player);dev.metallicarts.arts.AdvancedAllomancy.stop(s.player);}
    public static double power(ServerPlayer p,Metal metal){var s=SESSIONS.get(p.getUUID());return s!=null&&s.player==p&&s.level==p.level()?s.lastPowers.getOrDefault(metal,0.0):0;}
    public static Map<Metal,Double> powers(ServerPlayer p){var s=SESSIONS.get(p.getUUID());return s!=null&&s.player==p&&s.level==p.level()?s.lastPowers:Map.of();}
    public static void stopMetal(ServerPlayer p,Metal metal){var s=SESSIONS.get(p.getUUID());if(s!=null){s.engine.stop(metal);var next=new EnumMap<Metal,Double>(Metal.class);next.putAll(s.lastPowers);next.remove(metal);s.lastPowers=Map.copyOf(next);dev.metallicarts.arts.AdvancedAllomancy.cancelBurst(p,metal);}}
    public static double tinStrength(ServerPlayer p){
        var s=SESSIONS.get(p.getUUID());
        return s!=null&&s.player==p&&s.level==p.level()?s.lastPowers.getOrDefault(Metal.TIN,0.0):0;
    }
    public static void tick(ServerPlayer p,int tick) {
        var s=session(p);
        if(!p.isAlive()||p.isSpectator()||!dev.metallicarts.hemalurgy.ConstructRuntime.canUsePowers(p)){clear(s);send(s);return;}
        if(s.preparedTick!=tick)prepare(p,tick);
        var powers=s.lastPowers;
        double pewter=powers.getOrDefault(Metal.PEWTER,0.0);
        if(pewter>0) {
            s.pewterTicks=Math.min(12000,s.pewterTicks+1);
            modifier(p,Attributes.ATTACK_DAMAGE,"pewter_attack",3*pewter);
            modifier(p,Attributes.ARMOR,"pewter_armor",4*pewter);
            modifier(p,Attributes.KNOCKBACK_RESISTANCE,"pewter_knockback",Math.min(.8,.25*pewter));
            modifier(p,Attributes.MOVEMENT_SPEED,"pewter_speed",.015*pewter);
            modifier(p,Attributes.JUMP_STRENGTH,"pewter_jump",.1*pewter);
            modifier(p,Attributes.SAFE_FALL_DISTANCE,"pewter_fall",4*pewter);
            if(tick%20==0)p.causeFoodExhaustion((float)(.1*pewter*(p.isSprinting()?2:1)));
        } else {
            removePewter(p);
            if(s.pewterTicks>=200)s.fatigue=Math.max(s.fatigue,200);
            s.pewterTicks=0;
        }
        if(s.fatigue>0){s.fatigue--;modifier(p,Attributes.MOVEMENT_SPEED,"pewter_fatigue",-.02);}
        else remove(p,Attributes.MOVEMENT_SPEED,"pewter_fatigue");
        double tin=powers.getOrDefault(Metal.TIN,0.0);
        s.overload=tin>0&&s.engine.mode(Metal.TIN)==BurnMode.FLARE&&(p.level().getMaxLocalRawBrightness(p.blockPosition())>=14||
            p.level().getEntities(p,p.getBoundingBox().inflate(5)).size()>12);
        if(s.overload)modifier(p,Attributes.MOVEMENT_SPEED,"tin_overload",-.015);
        else remove(p,Attributes.MOVEMENT_SPEED,"tin_overload");
        if(tick-s.heartbeat>10)s.held=0;
        if(powers.containsKey(Metal.STEEL)||powers.containsKey(Metal.IRON)) {
            var active=new ArrayList<MetalPhysics.Target>();
            for(var iterator=s.selected.entrySet().iterator();iterator.hasNext();){var entry=iterator.next();var current=MetalPhysics.refresh(p,entry.getValue(),config);if(current.isPresent()){entry.setValue(current.get());s.missingSince.remove(entry.getKey());active.add(current.get());}else{int missing=s.missingSince.computeIfAbsent(entry.getKey(),k->tick);if(tick-missing>=200){s.missingSince.remove(entry.getKey());iterator.remove();}}}
            if(tick%20==0||s.nearby.isEmpty())s.nearby=MetalPhysics.nearby(p,config);
            if(s.autoSelect)s.explicitSelection=true;
            if(s.autoSelect)for(var target:s.nearby)if(s.selected.size()<64)s.selected.putIfAbsent(MetalPhysics.key(target),target);
            var normalized=new LinkedHashMap<String,MetalPhysics.Target>();for(var target:s.selected.values())normalized.put(MetalPhysics.key(target),target);s.selected.clear();s.selected.putAll(normalized);s.missingSince.keySet().retainAll(s.selected.keySet());var unique=new LinkedHashMap<String,MetalPhysics.Target>();for(var target:active)unique.put(MetalPhysics.key(target),target);
            var aimed=MetalPhysics.aimed(p,config);var targets=s.explicitSelection?List.copyOf(unique.values()):aimed.stream().toList();
            double net=netForce(s.held,powers);
            if(net!=0)MetalPhysics.applyMany(p,targets,Math.abs(net),net>0,config);
            if(tick%5==0&&ServerPlayNetworking.canSend(p,MetalSight.TYPE)){var visible=new LinkedHashMap<String,MetalPhysics.Target>();for(var source:active)visible.put(MetalPhysics.key(source),source);aimed.ifPresent(t->visible.put(MetalPhysics.key(t),t));for(var source:s.nearby)MetalPhysics.refresh(p,source,config).ifPresent(t->visible.putIfAbsent(MetalPhysics.key(t),t));var dots=visible.values().stream().limit(64).map(t->new MetalSight.Dot(t.entity()==null?-1:t.entity().getId(),t.position(),s.selected.containsKey(MetalPhysics.key(t)))).toList();ServerPlayNetworking.send(p,new MetalSight(dots));}
        } else {s.selected.clear();s.missingSince.clear();s.nearby=List.of();s.explicitSelection=false;}
        dev.metallicarts.arts.AdvancedAllomancy.tick(p,tick,powers);
        send(s);
        if(tick%10==0&&s.dirty){ModNetworking.send(p);s.dirty=false;}
    }
    public static void prepare(ServerPlayer p,int tick){
        var s=session(p);if(s.preparedTick==tick)return;
        if(!p.isAlive()||p.isSpectator()){clear(s);s.preparedTick=tick;return;}
        var before=p.getAttachedOrCreate(ModRegistries.SPIRITWEB);
        dev.metallicarts.arts.AdvancedAllomancy.beforeBurn(p,tick,s.engine);
        var step=s.engine.step(before,config,m->dev.metallicarts.arts.EffectivePower.strength(p,Art.ALLOMANCY,m));
        p.setAttached(ModRegistries.SPIRITWEB,step.state());
        s.lastPowers=dev.metallicarts.arts.AdvancedAllomancy.prepare(p,tick,step.strengths(),s.engine);
        if(!p.getAttachedOrCreate(ModRegistries.SPIRITWEB).equals(before))s.dirty=true;s.preparedTick=tick;
    }
    private static void modifier(ServerPlayer p,Holder<Attribute> attribute,String id,double value) {
        var instance=p.getAttribute(attribute);if(instance==null)return;
        var key=MetallicArts.id(id);var old=instance.getModifier(key);
        if(old==null||old.amount()!=value)instance.addOrUpdateTransientModifier(new AttributeModifier(key,value,AttributeModifier.Operation.ADD_VALUE));
    }
    private static void remove(ServerPlayer p,Holder<Attribute> attribute,String id){var instance=p.getAttribute(attribute);if(instance!=null)instance.removeModifier(MetallicArts.id(id));}
    private static void removePewter(ServerPlayer p) {
        remove(p,Attributes.ATTACK_DAMAGE,"pewter_attack");remove(p,Attributes.ARMOR,"pewter_armor");
        remove(p,Attributes.KNOCKBACK_RESISTANCE,"pewter_knockback");remove(p,Attributes.MOVEMENT_SPEED,"pewter_speed");
        remove(p,Attributes.JUMP_STRENGTH,"pewter_jump");remove(p,Attributes.SAFE_FALL_DISTANCE,"pewter_fall");
    }
    private static void removeModifiers(ServerPlayer p){removePewter(p);remove(p,Attributes.MOVEMENT_SPEED,"pewter_fatigue");remove(p,Attributes.MOVEMENT_SPEED,"tin_overload");}
    private static void send(Session s) {
        int modes=0;for(int i=0;i<AllomancyConfig.SUPPORTED.size();i++){var metal=AllomancyConfig.SUPPORTED.get(i);int mode=s.engine.mode(metal).ordinal();if(mode==0&&s.lastPowers.containsKey(metal))mode=1;modes|=mode<<(i*2);}
        int flags=(s.overload?1:0)|(s.fatigue>0?2:0)|(s.explicitSelection?4:0)|(s.autoSelect?8:0)|(s.selected.size()<<8);
        if(modes==s.lastModes&&flags==s.lastFlags)return;
        if(ServerPlayNetworking.canSend(s.player,AllomancyStatus.TYPE))ServerPlayNetworking.send(s.player,new AllomancyStatus(modes,flags));
        s.lastModes=modes;s.lastFlags=flags;
    }
    private AllomancyRuntime(){}
}
