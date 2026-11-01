package dev.metallicarts.feruchemy;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.allomancy.AllomancyRuntime;
import dev.metallicarts.domain.*;
import dev.metallicarts.equipment.*;
import dev.metallicarts.network.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import java.util.*;

/** Paid physical Feruchemy. Called before Allomancy for every player, so force calculations share current masses. */
public final class FeruchemyRuntime {
    private static final Map<UUID,Session> SESSIONS=new HashMap<>();
    private static final class Session {
        final ServerPlayer player;final Object level;final FeruchemicalEngine engine=new FeruchemicalEngine();
        Map<FeruchemicalAttribute,Double> effects=Map.of();
        int window,requests,lastTaste=-1000;String tasteName="",tasteDetail="",cue="";boolean dirty=true;
        SenseStatus lastSenses;
        Session(ServerPlayer player){this.player=player;this.level=player.level();}
    }
    private static Session session(ServerPlayer p){
        var old=SESSIONS.get(p.getUUID());
        if(old!=null&&(old.player!=p||old.level!=p.level())){clear(old);SESSIONS.remove(p.getUUID());old=null;}
        if(old==null){old=new Session(p);SESSIONS.put(p.getUUID(),old);}
        return old;
    }
    public static void initialize(){
        PhysicalCargo.initialize();
        PayloadTypeRegistry.serverboundPlay().register(CargoIntent.TYPE,CargoIntent.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ContactIntent.TYPE,ContactIntent.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ContactSnapshot.TYPE,ContactSnapshot.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SenseStatus.TYPE,SenseStatus.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ContactIntent.TYPE,(payload,context)->act(context.player(),payload,context.server().getTickCount()));
        ServerPlayNetworking.registerGlobalReceiver(CargoIntent.TYPE,(payload,context)->{
            var s=session(context.player());int tick=context.server().getTickCount();
            if(tick-s.window>=20){s.window=tick;s.requests=0;}
            if(++s.requests<=30)PhysicalCargo.toggle(context.player());
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->stop(handler.player));
        ServerPlayConnectionEvents.JOIN.register((handler,sender,server)->handler.player.removeAttached(ModRegistries.PHYSICAL_CARGO));
        ServerLifecycleEvents.SERVER_STOPPING.register(server->{SESSIONS.values().forEach(FeruchemyRuntime::clear);SESSIONS.clear();});
    }
    public static boolean act(ServerPlayer p,ContactIntent intent,int tick){
        var s=session(p);
        if(tick-s.window>=20){s.window=tick;s.requests=0;}
        if(++s.requests>30||intent.action()<0||intent.action()>4||intent.slot()<0||intent.slot()>=12||
            intent.sense()<0||intent.sense()>31||intent.mode()<0||intent.mode()>2||intent.rate()<1||intent.rate()>5)return false;
        if(intent.action()==3){send(p,s,"");return true;}
        if(!p.isAlive()||p.isSpectator()||!dev.metallicarts.hemalurgy.ConstructRuntime.canUsePowers(p))return false;
        try{
            if(intent.action()==0){ContactService.equip(p,intent.slot(),intent.sense());s.engine.stop(intent.slot());}
            else if(intent.action()==1){ContactService.unequip(p,intent.slot());s.engine.stop(intent.slot());}
            else if(intent.action()==4)dev.metallicarts.arts.AdvancedFeruchemy.unsealHeld(p);
            else if(!s.engine.request(intent.slot(),FeruchemicalEngine.Mode.values()[intent.mode()],intent.rate(),
                ContactService.tokens(ContactService.equipment(p)),MetalmindLedger.get(p.level().getServer()),p.getUUID(),p.getAttachedOrCreate(ModRegistries.SPIRITWEB),dev.metallicarts.arts.AdvancedFeruchemy.requestContext(p,s.engine)))
                throw new IllegalArgumentException("Check ability, Identity, contact, rate or opposite active action");
            s.dirty=true;send(p,s,"");return true;
        }catch(IllegalArgumentException|ArithmeticException e){send(p,s,e.getMessage());return false;}
    }
    private static void send(ServerPlayer p,Session s,String message){
        if(!ServerPlayNetworking.canSend(p,ContactSnapshot.TYPE))return;
        var modes=new ArrayList<Integer>();var rates=new ArrayList<Integer>();
        for(int i=0;i<12;i++){modes.add(s.engine.mode(i).ordinal());rates.add(s.engine.rate(i));}
        ServerPlayNetworking.send(p,new ContactSnapshot(ContactService.preview(p),modes,rates,message==null?"Action refused":message));
    }
    public static void tick(ServerPlayer p,int tick){
        var s=session(p);
        if(!p.isAlive()||p.isSpectator()){
            clear(s);sendSenses(p,s,SenseStatus.NORMAL);
            if(s.dirty){send(p,s,"");s.dirty=false;}return;
        }
        var before=new ArrayList<FeruchemicalEngine.Mode>();for(int i=0;i<12;i++)before.add(s.engine.mode(i));
        var advanced=dev.metallicarts.arts.AdvancedFeruchemy.context(p,s.engine);
        s.effects=s.engine.step(ContactService.tokens(ContactService.equipment(p)),MetalmindLedger.get(p.level().getServer()),
            p.getUUID(),p.getAttachedOrCreate(ModRegistries.SPIRITWEB),advanced);
        advanced.finish(s.effects,tick);
        for(int i=0;i<12;i++)if(before.get(i)!=s.engine.mode(i))s.dirty=true;
        apply(p,Attributes.MOVEMENT_SPEED,"feruchemical_speed",delta(s,FeruchemicalAttribute.SPEED),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        apply(p,Attributes.ATTACK_SPEED,"feruchemical_actions",delta(s,FeruchemicalAttribute.SPEED),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        apply(p,Attributes.BLOCK_BREAK_SPEED,"feruchemical_mining",miningMultiplier(p)-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        apply(p,Attributes.ATTACK_DAMAGE,"feruchemical_strength",2*delta(s,FeruchemicalAttribute.STRENGTH),AttributeModifier.Operation.ADD_VALUE);
        apply(p,Attributes.ATTACK_KNOCKBACK,"feruchemical_force",.5*delta(s,FeruchemicalAttribute.STRENGTH),AttributeModifier.Operation.ADD_VALUE);
        apply(p,Attributes.SCALE,"feruchemical_bulk",.15*delta(s,FeruchemicalAttribute.STRENGTH),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        double innate=.25*dev.metallicarts.hemalurgy.HemalurgyRuntime.passive(p,dev.metallicarts.hemalurgy.StolenPower.Kind.SENSES);
        double sight=1+delta(s,FeruchemicalAttribute.SIGHT)+innate,hearing=1+delta(s,FeruchemicalAttribute.HEARING)+innate,
            touch=1+delta(s,FeruchemicalAttribute.TOUCH)+innate,smell=1+delta(s,FeruchemicalAttribute.SMELL)+innate,taste=1+delta(s,FeruchemicalAttribute.TASTE)+innate;
        boolean overload=Math.max(sight,hearing)>3&&(p.level().getMaxLocalRawBrightness(p.blockPosition())>=14||
            p.level().getEntities(p,p.getBoundingBox().inflate(5)).size()>12);
        apply(p,Attributes.MOVEMENT_SPEED,"feruchemical_overload",overload?-.015:0,AttributeModifier.Operation.ADD_VALUE);
        if(tick%10==0)s.cue=cue(p,s,smell,touch,taste,tick)+(overload?" • sensory overload":"");
        sendSenses(p,s,new SenseStatus(sight,hearing,touch,smell,taste,s.cue));
        if(s.dirty||tick%10==0&&!s.effects.isEmpty()){send(p,s,"");s.dirty=false;}
    }
    private static double delta(Session s,FeruchemicalAttribute attribute){return s.effects.getOrDefault(attribute,0.0);}
    public static double mass(ServerPlayer p){
        var s=SESSIONS.get(p.getUUID());
        return (s!=null&&s.player==p&&s.level==p.level()?80*(1+delta(s,FeruchemicalAttribute.WEIGHT))*(1+.2*delta(s,FeruchemicalAttribute.STRENGTH)):80)+PhysicalCargo.carriedMass(p);
    }
    public static double carryCapacity(ServerPlayer p){
        var s=SESSIONS.get(p.getUUID());
        double innate=dev.metallicarts.hemalurgy.HemalurgyRuntime.passive(p,dev.metallicarts.hemalurgy.StolenPower.Kind.STRENGTH);
        return 40*(1+innate+(s!=null&&s.player==p&&s.level==p.level()?delta(s,FeruchemicalAttribute.STRENGTH):0));
    }
    public static float miningMultiplier(ServerPlayer p){
        var s=SESSIONS.get(p.getUUID());if(s==null||s.player!=p||s.level!=p.level())return 1;
        return (float)Math.min(8,(1+delta(s,FeruchemicalAttribute.SPEED))*(1+.25*delta(s,FeruchemicalAttribute.STRENGTH)));
    }
    private static void apply(ServerPlayer p,Holder<Attribute> attribute,String path,double amount,AttributeModifier.Operation operation){
        var value=p.getAttribute(attribute);if(value==null)return;var id=MetallicArts.id(path);
        if(amount==0){value.removeModifier(id);return;}
        var old=value.getModifier(id);
        if(old==null||old.amount()!=amount)value.addOrUpdateTransientModifier(new AttributeModifier(id,amount,operation));
    }
    private static void clear(Session s){
        dev.metallicarts.arts.AdvancedFeruchemy.stop(s.player);
        boolean active=!s.effects.isEmpty();s.engine.stopAll();s.effects=Map.of();s.cue="";
        apply(s.player,Attributes.MOVEMENT_SPEED,"feruchemical_speed",0,AttributeModifier.Operation.ADD_VALUE);
        apply(s.player,Attributes.ATTACK_SPEED,"feruchemical_actions",0,AttributeModifier.Operation.ADD_VALUE);
        apply(s.player,Attributes.BLOCK_BREAK_SPEED,"feruchemical_mining",0,AttributeModifier.Operation.ADD_VALUE);
        apply(s.player,Attributes.ATTACK_DAMAGE,"feruchemical_strength",0,AttributeModifier.Operation.ADD_VALUE);
        apply(s.player,Attributes.ATTACK_KNOCKBACK,"feruchemical_force",0,AttributeModifier.Operation.ADD_VALUE);
        apply(s.player,Attributes.SCALE,"feruchemical_bulk",0,AttributeModifier.Operation.ADD_VALUE);
        apply(s.player,Attributes.MOVEMENT_SPEED,"feruchemical_overload",0,AttributeModifier.Operation.ADD_VALUE);
        s.dirty|=active;
    }
    public static void stop(ServerPlayer p){var s=SESSIONS.remove(p.getUUID());if(s!=null)clear(s);}
    private static void sendSenses(ServerPlayer p,Session s,SenseStatus status){
        if(status.equals(s.lastSenses))return;s.lastSenses=status;
        if(ServerPlayNetworking.canSend(p,SenseStatus.TYPE))ServerPlayNetworking.send(p,status);
    }
    public static void taste(ServerPlayer p,ItemStack consumed){
        var food=consumed.get(DataComponents.FOOD);var metal=consumed.get(ModRegistries.METAL_DATA);
        if(food==null&&metal==null)return;
        var s=session(p);s.lastTaste=p.level().getServer().getTickCount();
        var name=consumed.getHoverName().getString();s.tasteName=name.length()>64?name.substring(0,64):name;
        s.tasteDetail=food!=null?"nutrition "+food.nutrition()+", saturation "+food.saturation():
            metal.metal().id()+", purity "+Math.round(metal.purity()*100)+"%";
    }
    private static String cue(ServerPlayer p,Session s,double smell,double touch,double taste,int tick){
        double allomantic=1+.5*AllomancyRuntime.tinStrength(p);
        var parts=new ArrayList<String>();
        double cargo=PhysicalCargo.carriedMass(p);
        if(cargo>0)parts.add("Cargo "+(int)cargo+"/"+(int)carryCapacity(p)+" kg • B: drop");
        double radius=Math.min(24,4*smell*allomantic);
        int examined=0;
        for(var entity:p.level().getEntities(p,p.getBoundingBox().inflate(radius))){
            if(examined++>=32)break;
            if(entity instanceof Animal&&entity.distanceToSqr(p)<=radius*radius){parts.add("Scent: nearby animal");break;}
        }
        boolean smoke=false;
        for(var direction:Direction.values()){
            for(int distance=1;distance<=Math.min(8,(int)radius);distance++){
                var pos=p.blockPosition().relative(direction,distance);
                if(!p.level().hasChunkAt(pos))continue;
                var state=p.level().getBlockState(pos);
                if(state.is(Blocks.FIRE)||state.is(Blocks.SOUL_FIRE)||state.is(Blocks.CAMPFIRE)&&state.getValue(net.minecraft.world.level.block.CampfireBlock.LIT)){smoke=true;break;}
            }
            if(smoke)break;
        }
        if(smoke)parts.add("Scent: smoke");
        if(touch*allomantic>1)parts.add(p.hurtTime>0?"Touch: pain":p.isOnFire()?"Touch: heat":p.isInWater()?"Touch: water":"Touch: ground contact");
        if(tick-s.lastTaste<=100)parts.add(taste*allomantic<1?"Taste muted":
            "Taste: "+s.tasteName+(taste*allomantic>1?" ("+s.tasteDetail+")":""));
        return String.join(" • ",parts);
    }
    private FeruchemyRuntime(){}
}
