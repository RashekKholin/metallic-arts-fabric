package dev.metallicarts.arts;

import com.google.gson.*;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.allomancy.*;
import dev.metallicarts.domain.*;
import dev.metallicarts.network.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import java.util.*;

/** Remaining standard Allomancy, with explicit bounded Minecraft prediction/time/social adaptations. */
public final class AdvancedAllomancy {
    private static final Gson GSON=new Gson();
    private static final class Session {
        final ServerPlayer player;final Object level;int emotion,brassEmotion,history,journalPage,actionWindow,actions,dodge=-1000,contact=-1000,journalUntil,burstUntil;
        final Map<Metal,Double> burstPowers=new EnumMap<>(Metal.class);
        boolean burst,selfBurst;String notice="";
        Session(ServerPlayer p){player=p;level=p.level();}
    }
    private static final Map<UUID,Session> SESSIONS=new HashMap<>();
    private static Session session(ServerPlayer p){var s=SESSIONS.get(p.getUUID());if(s==null||s.player!=p||s.level!=p.level()){s=new Session(p);SESSIONS.put(p.getUUID(),s);}return s;}
    public static void initialize(){
        PayloadTypeRegistry.serverboundPlay().register(ArtAction.TYPE,ArtAction.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ArtsFeedback.TYPE,ArtsFeedback.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ArtAction.TYPE,(payload,context)->action(context.player(),payload));
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->{stop(handler.player);EffectivePower.stop(handler.player);});
        ServerLifecycleEvents.SERVER_STOPPING.register(server->{SESSIONS.clear();PulseHistory.clear();TemporalEngine.clear();EmotionService.clear();EffectivePower.clear();});
    }
    public static void amplify(ServerPlayer p){session(p).burst=true;}
    public static void beforeBurn(ServerPlayer p,int tick,BurnEngine engine){var s=SESSIONS.get(p.getUUID());if(s!=null&&s.selfBurst&&s.burstUntil>0&&tick>=s.burstUntil)engine.stop(Metal.DURALUMIN);}
    public static void cancelBurst(ServerPlayer p,Metal metal){var s=SESSIONS.get(p.getUUID());if(s!=null){if(metal==Metal.DURALUMIN){s.burstPowers.clear();s.burstUntil=0;}else s.burstPowers.remove(metal);}}
    public static Spiritweb drain(Spiritweb state,Set<Metal> metals){
        var reserves=new EnumMap<Metal,Long>(Metal.class);state.reserves().forEach((m,v)->{if(!metals.contains(m))reserves.put(m,v);});
        return new Spiritweb(state.schema(),state.identity(),state.grants(),reserves,state.investedReserves().stream().filter(d->!metals.contains(d.metal())).toList());
    }
    public static Map<Metal,Double> prepare(ServerPlayer p,int tick,Map<Metal,Double> paid,BurnEngine engine){
        var s=session(p);var powers=new EnumMap<Metal,Double>(Metal.class);powers.putAll(paid);
        if(powers.containsKey(Metal.ALUMINUM)){
            p.setAttached(ModRegistries.SPIRITWEB,drain(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),EnumSet.allOf(Metal.class)));
            engine.stopAll();s.burst=false;s.burstPowers.clear();s.burstUntil=0;s.notice="Aluminum consumed all internal metal reserves";return Map.of();
        }
        if(s.burstUntil>0&&tick>=s.burstUntil){if(s.selfBurst){engine.stop(Metal.DURALUMIN);powers.remove(Metal.DURALUMIN);}s.burstPowers.clear();s.burstUntil=0;s.burst=false;}
        if(s.selfBurst&&s.burstUntil>tick&&!powers.containsKey(Metal.DURALUMIN)){s.burstPowers.clear();s.burstUntil=0;}
        if((s.burstUntil==0||s.selfBurst)&&(powers.containsKey(Metal.DURALUMIN)||s.burst)){
            s.selfBurst=powers.containsKey(Metal.DURALUMIN);
            s.burst=false;var exhausted=EnumSet.noneOf(Metal.class);
            for(var m:List.copyOf(powers.keySet()))if(!s.burstPowers.containsKey(m)&&!Set.of(Metal.DURALUMIN,Metal.ALUMINUM,Metal.CHROMIUM,Metal.NICROSIL).contains(m)){
                long mass=ReserveLedger.ordinary(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),m);
                double boost=Math.clamp(1+Math.sqrt(mass/(double)dev.metallicarts.config.AdvancedConfig.get().enhancement().massScale()),1,dev.metallicarts.config.AdvancedConfig.get().enhancement().maximumBoost());s.burstPowers.put(m,Math.min(dev.metallicarts.config.AdvancedConfig.get().enhancement().maximumPower(),powers.get(m)*boost));exhausted.add(m);engine.exhaust(m);
            }
            if(!exhausted.isEmpty()||s.selfBurst){p.setAttached(ModRegistries.SPIRITWEB,drain(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),exhausted));if(s.burstUntil==0)s.burstUntil=tick+100;s.notice="Amplified burst: five seconds; active metal fuel consumed";}
        }
        if(tick<s.burstUntil)for(var entry:s.burstPowers.entrySet())if(EffectivePower.strength(p,Art.ALLOMANCY,entry.getKey())>0)powers.put(entry.getKey(),entry.getValue());
        return Map.copyOf(powers);
    }
    /** Resolve simultaneous contact intents after every caster has paid and before any caster applies effects. */
    public static void frame(MinecraftServer server){
        PulseHistory.record(server);
        var drained=new HashSet<ServerPlayer>();var amplified=new HashSet<ServerPlayer>();
        for(var p:server.getPlayerList().getPlayers()){
            var s=session(p);if(server.getTickCount()-s.contact<dev.metallicarts.config.AdvancedConfig.get().enhancement().contactCooldown())continue;
            double chromium=AllomancyRuntime.power(p,Metal.CHROMIUM),nicrosil=AllomancyRuntime.power(p,Metal.NICROSIL);
            if(chromium<=0&&nicrosil<=0)continue;
            var target=aimed(p,2.5).orElse(null);
            if(target instanceof Mob mob&&NpcArts.active(mob)){
                if(!dev.metallicarts.api.ArtsProtection.allows(p,mob,chromium>0?dev.metallicarts.api.ArtsProtection.Action.LEECH:dev.metallicarts.api.ArtsProtection.Action.AMPLIFY))continue;
                s.contact=server.getTickCount();if(chromium>0){NpcArts.leech(mob);s.notice="Chromium: depleted actual NPC reserves";}else{NpcArts.boost(mob);s.notice="Nicrosil: forced next paid NPC burst";}continue;
            }
            if(!(target instanceof ServerPlayer other)||!p.canHarmPlayer(other)||other.isCreative()||
                !dev.metallicarts.api.ArtsProtection.allows(p,other,chromium>0?dev.metallicarts.api.ArtsProtection.Action.LEECH:dev.metallicarts.api.ArtsProtection.Action.AMPLIFY))continue;
            s.contact=server.getTickCount();
            if(chromium>0){drained.add(other);s.notice="Chromium: contact depleted target's internal metals";}
            else {amplified.add(other);s.notice="Nicrosil: next paid active-metal burst amplified";}
        }
        for(var p:drained){p.setAttached(ModRegistries.SPIRITWEB,drain(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),EnumSet.allOf(Metal.class)));AllomancyRuntime.stop(p);ModNetworking.send(p);}
        for(var p:amplified)if(!drained.contains(p))session(p).burst=true;
        for(var p:server.getPlayerList().getPlayers())TemporalEngine.update(p,AllomancyRuntime.powers(p),server.getTickCount());
    }
    public static void tick(ServerPlayer p,int tick,Map<Metal,Double> powers){
        var s=session(p);var cues=new ArrayList<String>();
        if(tick%100==0){
            var biome=p.level().getBiome(p.blockPosition()).unwrapKey().map(k->k.identifier().toString()).orElse("unknown");
            var journal=p.getAttachedOrCreate(ModRegistries.JOURNAL);p.setAttached(ModRegistries.JOURNAL,journal.observe(p,"discovery","Visited "+biome,"region:"+p.level().dimension().identifier()+":"+(p.getBlockX()>>6)+":"+(p.getBlockZ()>>6)));
        }
        double time=TemporalEngine.rate(p.level(),p.position());
        // TemporalPlayers owns these modifiers for casters and visitors alike.
        if(Math.abs(time-1)>.001)cues.add(String.format(Locale.ROOT,"Local time %.2fx",time));
        double zinc=powers.getOrDefault(Metal.ZINC,0.0),brass=powers.getOrDefault(Metal.BRASS,0.0);
        if(zinc>0||brass>0){
            double range=dev.metallicarts.config.AdvancedConfig.get().emotions().range();int affected=0;
            for(var target:p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(range),t->t!=p&&t.distanceToSqr(p)<=range*range).stream().sorted(Comparator.comparingDouble(p::distanceToSqr)).limit(64).toList()){
                boolean changed=false;double rate=dev.metallicarts.config.AdvancedConfig.get().emotions().rate();
                if(zinc>0)changed|=EmotionService.influence(p,target,EmotionProfile.Emotion.values()[s.emotion],zinc*rate);
                if(brass>0)changed|=EmotionService.influence(p,target,EmotionProfile.Emotion.values()[s.brassEmotion],-brass*rate);
                if(changed)affected++;
            }
            cues.add("Emotions: "+affected+" nearby • Zinc "+EmotionProfile.Emotion.values()[s.emotion].name().toLowerCase(Locale.ROOT)+" / Brass "+EmotionProfile.Emotion.values()[s.brassEmotion].name().toLowerCase(Locale.ROOT));
        }
        double bronze=powers.getOrDefault(Metal.BRONZE,0.0);
        if(bronze>0&&tick%dev.metallicarts.config.AdvancedConfig.get().detection().interval()==0){
            var pulses=PulseHistory.detectable(p,bronze);
            for(var pulse:pulses){cues.add("Pulse "+String.join("/",pulse.metals().stream().map(Metal::id).sorted().toList())+" • "+(int)p.position().distanceTo(pulse.position())+"m"+(pulse.concealment()>0?" (pierced cloud)":""));point(p,pulse.position().add(0,1,0),0xDD8833);}
            var godPulses=GodmetalRuntime.signatures(p,bronze);cues.addAll(godPulses);if(pulses.isEmpty()&&godPulses.isEmpty())cues.add("Bronze: no detectable Allomantic pulse");
            else if(!pulses.isEmpty()&&dev.metallicarts.config.AdvancedConfig.get().detection().sound()){var metal=pulses.getFirst().metals().stream().min(Comparator.naturalOrder()).orElse(Metal.IRON);p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(net.minecraft.core.Holder.direct(dev.metallicarts.registry.ModPresentation.BRONZE_PULSE),net.minecraft.sounds.SoundSource.PLAYERS,p.getX(),p.getY(),p.getZ(),.15f,.6f+metal.ordinal()*.04f,p.getRandom().nextLong()));}
        }
        if(powers.containsKey(Metal.COPPER))cues.add("Coppercloud • emotional immunity");
        if(powers.containsKey(Metal.GOLD))gold(p,s,cues,tick,powers.get(Metal.GOLD));
        if(powers.containsKey(Metal.ELECTRUM))electrum(p,cues,tick,powers.get(Metal.ELECTRUM));
        var ownMood=p.getAttachedOrCreate(ModRegistries.EMOTIONS).settle(p.level().getGameTime());
        if(ownMood.awareness()>.2)cues.add("Emotional influence noticed • awareness "+Math.round(ownMood.awareness()*100)+"%");
        if(!s.notice.isEmpty()){cues.add(s.notice);if(tick%100==0)s.notice="";}
        if(tick%10==0)feedback(p,String.join(" • ",cues),s.journalUntil>=tick||powers.containsKey(Metal.GOLD));
    }
    public static double concealment(ServerPlayer target){
        double strength=NpcArts.cloudAt(target);for(var smoker:target.level().getServer().getPlayerList().getPlayers()){
            if(smoker.level()!=target.level())continue;double power=AllomancyRuntime.power(smoker,Metal.COPPER);
            if(power>0&&smoker.distanceToSqr(target)<=Math.pow(Math.min(18,6+3*power),2))strength=Math.max(strength,power);
        }return strength;
    }
    public static Optional<LivingEntity> aimed(ServerPlayer p,double range){
        Vec3 eye=p.getEyePosition(),look=p.getLookAngle();
        return p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(range),e->e!=p&&e.isAlive()&&!e.isSpectator()&&p.hasLineOfSight(e)).stream().filter(e->{
            var delta=e.getBoundingBox().getCenter().subtract(eye);double along=delta.dot(look);return along>0&&delta.length()<=range&&delta.subtract(look.scale(along)).length()<.9;
        }).min(Comparator.comparingDouble(p::distanceToSqr));
    }
    private static void gold(ServerPlayer p,Session s,List<String> cues,int tick,double strength){
        var history=p.getAttachedOrCreate(ModRegistries.JOURNAL).history();
        if(history.isEmpty()){cues.add("Gold: explore to record a real alternate path");return;}
        var entry=history.get(Math.floorMod(s.history,history.size()));
        cues.add("Gold alternate path: "+entry.label()+" at "+entry.x()+", "+entry.y()+", "+entry.z());
        if(tick%5==0&&entry.dimension().equals(p.level().dimension().identifier().toString())){
            var delta=new Vec3(entry.x()+.5,entry.y(),entry.z()+.5).subtract(p.position());
            var echo=p.position().add(delta.length()>8?delta.normalize().scale(8):delta);
            for(int n=0;n<8;n++)point(p,echo.add(Math.sin(n*Math.PI/4)*.3,n%4*.5,Math.cos(n*Math.PI/4)*.3),0xFFD555);
        }
    }
    private static void electrum(ServerPlayer p,List<String> cues,int tick,double strength){
        double cognition=AdvancedFeruchemy.effect(p,dev.metallicarts.feruchemy.FeruchemicalAttribute.MENTAL_SPEED)+.25*dev.metallicarts.hemalurgy.HemalurgyRuntime.passive(p,dev.metallicarts.hemalurgy.StolenPower.Kind.COGNITION);
        int horizon=(int)Math.clamp(10+10*strength*(1+.25*cognition),5,40);var danger=threat(p,p.getBoundingBox(),horizon);
        if(danger.isPresent())cues.add("Electrum: incoming projectile in ~"+danger.get()+" ticks • J follows a clear opening");
        else cues.add("Electrum: "+horizon+"-tick movement branches; uncertain after collisions");
        if(tick%5==0){
            var base=p.getDeltaMovement().horizontalDistance()>.03?p.getDeltaMovement().multiply(1,0,1):p.getLookAngle().multiply(1,0,1).normalize().scale(.15);
            for(int branch=-1;branch<=1;branch++)for(int n=5;n<=horizon;n+=5){
                var end=p.getEyePosition().add(base.yRot((float)Math.toRadians(branch*30)).scale(n));
                var hit=p.level().clip(new ClipContext(p.getEyePosition(),end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));point(p,hit.getLocation(),branch==0?0xEEEEDD:0xAABBCC);
                if(hit.getType()!=HitResult.Type.MISS)break;
            }
        }
    }
    public static Optional<Integer> threat(ServerPlayer p,AABB body,int horizon){
        int checked=0,best=Integer.MAX_VALUE;
        for(var projectile:p.level().getEntitiesOfClass(Projectile.class,p.getBoundingBox().inflate(16))){
            if(checked++>=32)break;if(projectile.getOwner()==p)continue;
            Vec3 position=projectile.position(),motion=projectile.getDeltaMovement();
            for(int n=1;n<=horizon;n++){
                var next=position.add(motion);
                if(body.inflate(.25).clip(position,next).isPresent()){best=Math.min(best,n);break;}
                if(p.level().clip(new ClipContext(position,next,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()!=HitResult.Type.MISS)break;
                position=next;double drag=projectile instanceof net.minecraft.world.entity.projectile.arrow.AbstractArrow?(projectile.isInWater()?.6:.99):.99;motion=motion.scale(drag);if(!projectile.isNoGravity())motion=motion.add(0,-projectile.getGravity(),0);
            }
        }
        return best==Integer.MAX_VALUE?Optional.empty():Optional.of(best);
    }
    public static boolean action(ServerPlayer p,ArtAction action){
        var s=session(p);int tick=p.level().getServer().getTickCount();
        if(tick-s.actionWindow>=20){s.actionWindow=tick;s.actions=0;}
        if(++s.actions>20||!p.isAlive()||p.isSpectator()||action.action()<0||action.action()>4||action.value()<0||action.value()>127)return false;
        switch(action.action()){
            case 0 -> {if(action.value()>=EmotionProfile.Emotion.values().length)return false;s.emotion=action.value();s.notice="Selected "+EmotionProfile.Emotion.values()[s.emotion].name().toLowerCase(Locale.ROOT);}
            case 1 -> {return dodge(p,s,tick);}
            case 2 -> {if(AllomancyRuntime.power(p,Metal.GOLD)<=0)return false;s.history=action.value();s.journalUntil=tick+200;}
            case 3 -> {s.journalUntil=tick+200;s.journalPage=Math.min(42,action.value());}
            case 4 -> {if(action.value()>=EmotionProfile.Emotion.values().length)return false;s.brassEmotion=action.value();s.notice="Brass soothes "+EmotionProfile.Emotion.values()[s.brassEmotion].name().toLowerCase(Locale.ROOT);}
        }
        feedback(p,s.notice,true);return true;
    }
    private static boolean dodge(ServerPlayer p,Session s,int tick){
        if(AllomancyRuntime.power(p,Metal.ELECTRUM)<=0&&!GodmetalRuntime.canOpen(p)||tick-s.dodge<Math.clamp((int)(dev.metallicarts.config.AdvancedConfig.get().enhancement().dodgeCooldown()/(1+AdvancedFeruchemy.effect(p,dev.metallicarts.feruchemy.FeruchemicalAttribute.MENTAL_SPEED)*.5)),4,20)||threat(p,p.getBoundingBox(),15).isEmpty())return false;
        var state=p.getAttachedOrCreate(ModRegistries.SPIRITWEB);boolean god=GodmetalRuntime.canOpen(p);if(!god&&ReserveLedger.ordinary(state,Metal.ELECTRUM)<dev.metallicarts.config.AdvancedConfig.get().enhancement().dodgeCost())return false;
        var right=p.getLookAngle().multiply(1,0,1).normalize().yRot((float)(Math.PI/2));
        for(double side:new double[]{1,-1}){
            var change=right.scale(side*.65);var destination=p.getBoundingBox().move(change);
            if(!p.level().noCollision(p,destination)||threat(p,destination,15).isPresent())continue;
            if(god){if(!GodmetalRuntime.payOpening(p))return false;}else p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.debit(state,Metal.ELECTRUM,dev.metallicarts.config.AdvancedConfig.get().enhancement().dodgeCost()));
            p.setDeltaMovement(MetalPhysics.limited(p.getDeltaMovement().add(change),AllomancyRuntime.config.maximumPlayerSpeed()));p.hurtMarked=true;s.dodge=tick;s.notice="Followed a predicted opening";ModNetworking.send(p);return true;
        }return false;
    }
    public static void feedback(ServerPlayer p,String cue,boolean journal){
        if(!ServerPlayNetworking.canSend(p,ArtsFeedback.TYPE))return;
        cue=cue+(AdvancedFeruchemy.cue(p).isEmpty()?"":" • "+AdvancedFeruchemy.cue(p));
        String data=journal?journalData(p,session(p).journalPage).toString():"";
        ServerPlayNetworking.send(p,new ArtsFeedback(cue.length()>2048?cue.substring(0,2048):cue,data));
    }
    public static JsonObject journalData(ServerPlayer p,int page){
        page=Math.clamp(page,0,42);var j=p.getAttachedOrCreate(ModRegistries.JOURNAL);var json=new JsonObject();
        json.add("memories",GSON.toJsonTree(j.memories().stream().skip(page*6L).limit(6).toList()));
        json.add("history",GSON.toJsonTree(j.history().stream().skip(page*6L).limit(6).toList()));
        json.addProperty("memoryTotal",j.memories().size());json.addProperty("historyTotal",j.history().size());json.addProperty("page",page);
        var access=new JsonObject();for(var art:Art.values())for(var metal:Metal.values())access.addProperty(art.name()+":"+metal.name(),EffectivePower.strength(p,art,metal));json.add("access",access);var owned=new JsonObject();for(var art:Art.values())for(var metal:Metal.values())owned.addProperty(art.name()+":"+metal.name(),EffectivePower.base(p,art,metal));json.add("baseAccess",owned);json.addProperty("zincEmotion",session(p).emotion);json.addProperty("brassEmotion",session(p).brassEmotion);json.addProperty("burstTicks",Math.max(0,session(p).burstUntil-p.level().getServer().getTickCount()));return json;
    }
    private static void point(ServerPlayer p,Vec3 position,int color){if(p.level().hasChunkAt(net.minecraft.core.BlockPos.containing(position)))p.level().sendParticles(p,new dev.metallicarts.registry.ModPresentation.Dust(color,.6f),true,false,position.x,position.y,position.z,1,0,0,0,0);}
    private static void modifier(ServerPlayer p,net.minecraft.core.Holder<Attribute> attribute,String name,double amount,AttributeModifier.Operation operation){
        var instance=p.getAttribute(attribute);if(instance==null)return;var id=MetallicArts.id(name);if(amount==0)instance.removeModifier(id);
        else if(instance.getModifier(id)==null||instance.getModifier(id).amount()!=amount)instance.addOrUpdateTransientModifier(new AttributeModifier(id,amount,operation));
    }
    public static void stop(ServerPlayer p){
        SESSIONS.remove(p.getUUID());TemporalEngine.stop(p);
        modifier(p,Attributes.MOVEMENT_SPEED,"local_time_movement",0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        modifier(p,Attributes.ATTACK_SPEED,"local_time_actions",0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        modifier(p,Attributes.BLOCK_BREAK_SPEED,"local_time_mining",0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
    public static void finishFrame(){EmotionService.finishFrame();TemporalEngine.cleanup();}
    private AdvancedAllomancy(){}
}
