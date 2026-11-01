package dev.metallicarts.arts;

import com.google.gson.*;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.domain.*;
import dev.metallicarts.equipment.*;
import dev.metallicarts.economy.*;
import dev.metallicarts.feruchemy.*;
import dev.metallicarts.hemalurgy.*;
import dev.metallicarts.network.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import java.util.*;

/** Finite body-resource payments, typed Investiture access, memories and deliberate unsealed construction. */
public final class AdvancedFeruchemy {
    public static final long EMERGENCY_COST=200000,CURE_COST=50000;
    private static final ResourceKey<DamageType> STORAGE=ResourceKey.create(Registries.DAMAGE_TYPE,MetallicArts.id("feruchemical_storage"));
    private record Frame(ServerPlayer player,Object level,FeruchemicalEngine engine,List<Optional<AssetToken>> tokens,Map<FeruchemicalAttribute,Double> effects){}
    private static final Map<UUID,Frame> FRAMES=new HashMap<>();
    private static final Map<UUID,int[]> REQUESTS=new HashMap<>();
    private static final Map<UUID,String> CUES=new HashMap<>();
    public static void initialize(){
        PayloadTypeRegistry.serverboundPlay().register(MemoryAction.TYPE,MemoryAction.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(MemoryAction.TYPE,(payload,context)->memory(context.player(),payload));
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->stop(handler.player));
        ServerLifecycleEvents.SERVER_STOPPING.register(server->{FRAMES.clear();REQUESTS.clear();CUES.clear();SocialConnections.clear();});
    }
    private static BodyState natural(ServerPlayer p){
        var state=p.getAttachedOrCreate(ModRegistries.BODY);long now=p.level().getGameTime();
        if(state.updated()==now)return state;
        long fatigue=p.isSleeping()?Math.max(0,state.fatigue()-dev.metallicarts.config.AdvancedConfig.get().body().sleepRecovery()):Math.min(BodyState.MAX_FATIGUE,state.fatigue()+dev.metallicarts.config.AdvancedConfig.get().body().awakeFatigue());
        double climate=p.level().getBiome(p.blockPosition()).value().getBaseTemperature();
        long target=36500000+(long)((climate-.8)*1000000)-(p.isInWater()?4000000:0)-(p.level().isRainingAt(p.blockPosition())?2000000:0)+(p.isOnFire()?8000000:0);
        long temperature=Math.clamp(state.temperature()+Math.clamp((target-state.temperature())/1000,-dev.metallicarts.config.AdvancedConfig.get().body().thermalStep(),dev.metallicarts.config.AdvancedConfig.get().body().thermalStep()),BodyState.MIN_TEMP,BodyState.MAX_TEMP);
        state=state.fatigue(fatigue).temperature(temperature).updated(now);p.setAttached(ModRegistries.BODY,state);return state;
    }
    public static final class Context implements FeruchemicalEngine.Context {
        private final ServerPlayer p;private final FeruchemicalEngine engine;
        private BodyState body;private int identity;
        private final Map<EffectivePower.Key,Double> additions=new HashMap<>(),suppression=new HashMap<>();
        Context(ServerPlayer p,FeruchemicalEngine engine){this.p=p;this.engine=engine;body=natural(p);}
        private void snapshot(){var tokens=ContactService.tokens(ContactService.equipment(p));additions.putAll(typedAccess(p,engine,tokens,true));suppression.putAll(typedAccess(p,engine,tokens,false));identity=(int)Math.max(0,-1000*effect(p,FeruchemicalAttribute.IDENTITY));}
        @Override public double access(Spiritweb ignored,Art art,Metal metal){
            if(!p.isAlive())return 0;var key=new EffectivePower.Key(art,metal);
            double raw=EffectivePower.base(p,art,metal);return Math.min(1000,(raw*(1-suppression.getOrDefault(key,0.0))+additions.getOrDefault(key,0.0))*(1+.1*HemalurgyRuntime.passive(p,StolenPower.Kind.INVESTITURE)));
        }
        @Override public Set<UUID> keys(Metal metal){return HemalurgyRuntime.donorKeys(p,metal);}
        @Override public boolean blankIdentity(){return identity>=1000;}
        @Override public boolean allowStore(MetalmindAsset asset){
            if(asset.attribute()==FeruchemicalAttribute.MEMORY)return false;
            if(asset.unkeyed()&&asset.charge()>0&&!blankIdentity()&&asset.attribute()!=FeruchemicalAttribute.IDENTITY)return false;
            if(asset.attribute()==FeruchemicalAttribute.INVESTITURE)return asset.content().power().filter(power->EffectivePower.base(p,power.art(),power.metal())+1e-9>=power.strength()).isPresent();
            return true;
        }
        @Override public int storeLimit(MetalmindAsset asset,int units){
            return Math.max(0,switch(asset.attribute()){
                case HEALTH -> p.isCreative()?0:(int)Math.min(units,Math.max(0,(p.getHealth()-2)*20000));
                case WAKEFULNESS -> (int)Math.min(units,BodyState.MAX_FATIGUE-body.fatigue());
                case WARMTH -> (int)Math.min(units,(body.temperature()-BodyState.MIN_TEMP)/5);
                case BREATH -> (int)Math.min(units,body.air()+Math.max(0,p.getAirSupply()-10)*1000L);
                case NUTRITION -> (int)Math.min(units,body.nutrition()+(long)(p.getFoodData().getSaturationLevel()*BodyState.FOOD_POINT)+Math.max(0,p.getFoodData().getFoodLevel()-1)*BodyState.FOOD_POINT);
                case IDENTITY -> asset.unkeyed()&&asset.charge()>0&&!blankIdentity()&&units<1000?0:units;
                default -> units;
            });
        }
        @Override public int payStore(MetalmindAsset asset,int units){
            switch(asset.attribute()){
                case HEALTH -> {
                    float before=p.getHealth()+p.getAbsorptionAmount();
                    var source=new DamageSource(p.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(STORAGE));
                    if(!p.hurtServer(p.level(),source,units/20000.0f)||!p.isAlive())return 0;
                    return Math.min(units,Math.max(0,(int)Math.floor((before-p.getHealth()-p.getAbsorptionAmount())*20000)));
                }
                case WAKEFULNESS -> body=body.fatigue(body.fatigue()+units);
                case WARMTH -> body=body.temperature(body.temperature()-units*5L);
                case BREATH -> {
                    long bank=body.air();int air=p.getAirSupply();while(bank<units&&air>10){air--;bank+=1000;}
                    int amount=(int)Math.min(units,bank);p.setAirSupply(air);body=body.air(bank-amount);saveBody();return amount;
                }
                case NUTRITION -> {
                    long bank=body.nutrition();int amount=(int)Math.min(units,bank);bank-=amount;int remaining=units-amount;
                    if(remaining>0){
                        float saturation=p.getFoodData().getSaturationLevel();int take=(int)Math.min(remaining,Math.floor(saturation*BodyState.FOOD_POINT));
                        p.getFoodData().setSaturation(Math.max(0,saturation-take/(float)BodyState.FOOD_POINT));remaining-=take;
                        while(remaining>0&&p.getFoodData().getFoodLevel()>1){p.getFoodData().setFoodLevel(p.getFoodData().getFoodLevel()-1);bank+=BodyState.FOOD_POINT;int paid=(int)Math.min(remaining,bank);bank-=paid;remaining-=paid;}
                    }
                    body=body.nutrition(bank);saveBody();return units-remaining;
                }
                default -> {}
            }
            saveBody();return units;
        }
        @Override public int tapLimit(MetalmindAsset asset,int units){
            return Math.max(0,switch(asset.attribute()){
                case HEALTH -> (int)Math.min(units,Math.ceil(Math.max(0,p.getMaxHealth()-p.getHealth())*20000));
                case WAKEFULNESS -> (int)Math.min(units,body.fatigue());
                case WARMTH -> (int)Math.min(units,(BodyState.MAX_TEMP-body.temperature())/5);
                case BREATH -> (int)Math.min(units,Math.max(0,(p.getMaxAirSupply()-p.getAirSupply())*1000L-body.air()));
                case NUTRITION -> (int)Math.min(units,BodyState.MAX_NUTRITION-body.nutrition());
                default -> units;
            });
        }
        @Override public boolean waitWhenFull(MetalmindAsset asset){return asset.attribute()==FeruchemicalAttribute.HEALTH&&(asset.charge()>=dev.metallicarts.config.AdvancedConfig.get().body().emergencyCost()||asset.charge()>=dev.metallicarts.config.AdvancedConfig.get().body().cureCost()&&(p.hasEffect(MobEffects.POISON)||p.hasEffect(MobEffects.WITHER)||p.getAttachedOrCreate(ModRegistries.TRAUMA).total()>0));}
        @Override public void paid(MetalmindAsset asset,int slot,int units){
            if(asset.attribute()==FeruchemicalAttribute.IDENTITY&&units<0)identity-=units;
            if(asset.attribute()==FeruchemicalAttribute.INVESTITURE){
                var power=asset.content().power().orElseThrow();
                if(units<0)suppression.merge(power.key(),-units/1000.0,Double::sum);
                else additions.merge(power.key(),power.strength()*units/1000.0,Double::sum);
            }
            if(units>0)switch(asset.attribute()){
                case HEALTH -> p.heal(units/20000.0f);
                case WAKEFULNESS -> body=body.fatigue(Math.max(0,body.fatigue()-units));
                case WARMTH -> body=body.temperature(body.temperature()+units*5L);
                case BREATH -> body=body.air(body.air()+units);
                case NUTRITION -> body=body.nutrition(body.nutrition()+units);
                default -> {}
            }
            saveBody();
        }
        private void saveBody(){p.setAttached(ModRegistries.BODY,body);}
        public void finish(Map<FeruchemicalAttribute,Double> effects,int tick){
            if(!p.isAlive()){stop(p);return;}
            while(body.air()>=1000&&p.getAirSupply()<p.getMaxAirSupply()){p.setAirSupply(p.getAirSupply()+1);body=body.air(body.air()-1000);}
            boolean storingNutrition=false;
            for(int i=0;i<12;i++){var data=ContactService.equipment(p).item(i).get(ModRegistries.METALMIND);if(data!=null&&data.attribute()==FeruchemicalAttribute.NUTRITION&&engine.mode(i)==FeruchemicalEngine.Mode.STORE)storingNutrition=true;}
            if(!storingNutrition){
                while(body.nutrition()>=BodyState.FOOD_POINT&&p.getFoodData().getFoodLevel()<20){p.getFoodData().setFoodLevel(p.getFoodData().getFoodLevel()+1);body=body.nutrition(body.nutrition()-BodyState.FOOD_POINT);}
                if(p.getFoodData().getFoodLevel()==20&&p.getFoodData().getSaturationLevel()<20){
                    long use=Math.min(body.nutrition(),(long)((20-p.getFoodData().getSaturationLevel())*BodyState.FOOD_POINT));
                    p.getFoodData().setSaturation(p.getFoodData().getSaturationLevel()+use/(float)BodyState.FOOD_POINT);body=body.nutrition(body.nutrition()-use);
                }
            }
            saveBody();var tokens=ContactService.tokens(ContactService.equipment(p));FRAMES.put(p.getUUID(),new Frame(p,p.level(),engine,tokens,effects));
            EffectivePower.publish(p,()->typedAccess(p,engine,tokens,true),()->typedAccess(p,engine,tokens,false));
            apply(p,effects,tick,body);Injuries.tick(p);SocialConnections.tick(p,tick);
        }
    }
    private static Map<EffectivePower.Key,Double> typedAccess(ServerPlayer p,FeruchemicalEngine engine,List<Optional<AssetToken>> tokens,boolean tapping){
        var result=new HashMap<EffectivePower.Key,Double>();if(!p.isAlive())return result;
        for(int i=0;i<12;i++){
            double paid=engine.paid(i);if(paid==0||tapping!=(paid>0)||engine.mode(i)==FeruchemicalEngine.Mode.OFF)continue;
            var data=ContactService.equipment(p).item(i).get(ModRegistries.METALMIND);if(data==null||tokens.get(i).filter(data.token()::equals).isEmpty())continue;
            try{var asset=MetalmindLedger.get(p.level().getServer()).owned(data.token(),new AssetLease(p.getUUID(),i));if(asset.attribute()==FeruchemicalAttribute.INVESTITURE){var power=asset.content().power().orElseThrow();result.merge(power.key(),tapping?power.strength()*paid:-paid,Double::sum);}}catch(IllegalArgumentException ignored){}
        }return result;
    }
    public static Context requestContext(ServerPlayer p,FeruchemicalEngine engine){var context=new Context(p,engine);context.snapshot();return context;}
    public static Context context(ServerPlayer p,FeruchemicalEngine engine){return new Context(p,engine);}
    public static double effect(ServerPlayer p,FeruchemicalAttribute attr){
        var frame=FRAMES.get(p.getUUID());if(frame==null||frame.player()!=p||frame.level()!=p.level())return 0;
        double result=0;var equipment=ContactService.equipment(p);
        for(int i=0;i<12;i++){
            if(frame.engine().mode(i)==FeruchemicalEngine.Mode.OFF||frame.engine().paid(i)==0)continue;
            var data=equipment.item(i).get(ModRegistries.METALMIND);
            if(data!=null&&data.attribute()==attr&&frame.tokens().get(i).filter(data.token()::equals).isPresent())result+=frame.engine().paid(i);
        }return result;
    }
    private static void apply(ServerPlayer p,Map<FeruchemicalAttribute,Double> effects,int tick,BodyState body){
        double mental=effects.getOrDefault(FeruchemicalAttribute.MENTAL_SPEED,0.0),determination=effects.getOrDefault(FeruchemicalAttribute.DETERMINATION,0.0);
        modifier(p,Attributes.ATTACK_SPEED,"mental_actions",mental*.25+Math.min(0,determination)*.5,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        modifier(p,Attributes.BLOCK_BREAK_SPEED,"mental_precision",mental*.15,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        modifier(p,Attributes.MOVEMENT_SPEED,"determination",determination<0?determination*.015:0,AttributeModifier.Operation.ADD_VALUE);
        modifier(p,Attributes.MAX_HEALTH,"stored_health_capacity",Math.min(0,effects.getOrDefault(FeruchemicalAttribute.HEALTH,0.0))*.5,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        if(p.getHealth()>p.getMaxHealth())p.setHealth(p.getMaxHealth());
        double luck=effects.getOrDefault(FeruchemicalAttribute.FORTUNE,0.0)+HemalurgyRuntime.passive(p,StolenPower.Kind.FORTUNE);
        modifier(p,Attributes.LUCK,"fortune",luck,AttributeModifier.Operation.ADD_VALUE);
        boolean sleepy=body.fatigue()>48000000,cold=body.temperature()<34000000,hot=body.temperature()>39000000;
        modifier(p,Attributes.MOVEMENT_SPEED,"body_fatigue",sleepy?-.02:Math.min(0,effects.getOrDefault(FeruchemicalAttribute.WAKEFULNESS,0.0))*.02,AttributeModifier.Operation.ADD_VALUE);
        modifier(p,Attributes.MOVEMENT_SPEED,"body_temperature",cold||hot?-.015:0,AttributeModifier.Operation.ADD_VALUE);
        if(tick%40==0&&(cold||hot)&&!p.isCreative())p.hurtServer(p.level(),p.damageSources().freeze(),body.temperature()<32000000||body.temperature()>41000000?.5f:.1f);
        if(effects.getOrDefault(FeruchemicalAttribute.WARMTH,0.0)>0&&body.temperature()>35000000)p.setTicksFrozen(Math.max(0,p.getTicksFrozen()-4));
        if(effects.getOrDefault(FeruchemicalAttribute.WAKEFULNESS,0.0)!=0&&tick%20==0)p.getStats().setValue(p,Stats.CUSTOM.get(Stats.TIME_SINCE_REST),(int)(body.fatigue()/1000));
        if(tick%100==0)cure(p);
        if(tick%20==0){
            var cues=new ArrayList<String>();if(p.getAttachedOrCreate(ModRegistries.TRAUMA).total()>0)cues.add("Injury: arm "+p.getAttachedOrCreate(ModRegistries.TRAUMA).arm()+" / leg "+p.getAttachedOrCreate(ModRegistries.TRAUMA).leg()+" / head "+p.getAttachedOrCreate(ModRegistries.TRAUMA).head());if(!effects.isEmpty()||sleepy||cold||hot)cues.add(String.format(Locale.ROOT,"Body %.1f°C • fatigue %d%% • nutrition %.2f",body.temperature()/1000000.0,Math.round(body.fatigue()*100.0/BodyState.MAX_FATIGUE),body.nutrition()/(double)BodyState.FOOD_POINT));
            if(effects.getOrDefault(FeruchemicalAttribute.CONNECTION,0.0)!=0)cues.add("Local rapport "+Math.round(p.getAttachedOrCreate(ModRegistries.CONNECTIONS).familiarity(SocialConnections.region(p))*100)+"% • supported villager trade Connection");
            if(effect(p,FeruchemicalAttribute.IDENTITY)<=-1)cues.add("Identity suppressed • empty pools fill unkeyed");
            if(effect(p,FeruchemicalAttribute.INVESTITURE)!=0)cues.add("Typed ability access changes while nicrosilminds are active");
            if(mental>0||HemalurgyRuntime.passive(p,StolenPower.Kind.COGNITION)>0)AdvancedAllomancy.aimed(p,16).ifPresent(e->cues.add("Analysis: "+e.getName().getString()+" "+String.format(Locale.ROOT,"%.1f/%.1f health",e.getHealth(),e.getMaxHealth())));
            if(luck>0)opportunity(p).ifPresent(cues::add);if(mental<0)cues.add("Thinking slowed");if(determination<0)cues.add("Determination diminished");
            CUES.put(p.getUUID(),String.join(" • ",cues));
        }
    }
    private static Optional<String> opportunity(ServerPlayer p){
        int checked=0;var ores=TagKey.create(Registries.BLOCK,MetallicArts.id("fortune_opportunities"));
        for(int y=-6;y<=6;y+=2)for(int x=-dev.metallicarts.config.AdvancedConfig.get().body().opportunityRadius();x<=dev.metallicarts.config.AdvancedConfig.get().body().opportunityRadius();x+=2)for(int z=-dev.metallicarts.config.AdvancedConfig.get().body().opportunityRadius();z<=dev.metallicarts.config.AdvancedConfig.get().body().opportunityRadius();z+=2){
            if(checked++>=dev.metallicarts.config.AdvancedConfig.get().body().opportunityProbes())return Optional.empty();var pos=p.blockPosition().offset(x,y,z);if(!p.level().hasChunkAt(pos))continue;
            if(p.level().getBlockState(pos).is(ores)){String direction=Math.abs(x)>Math.abs(z)?x>0?"east":"west":z>0?"south":"north";return Optional.of("Fortune: an opportunity feels "+direction+(y<0?" and below":""));}
        }return Optional.empty();
    }
    private static UUID key(ServerPlayer p,MetalmindAsset a){
        var identity=p.getAttachedOrCreate(ModRegistries.SPIRITWEB).identity();if(a.charge()==0||a.accessible(identity))return identity;
        return a.identity().filter(HemalurgyRuntime.donorKeys(p,a.metal())::contains).orElseThrow(()->new IllegalArgumentException("Foreign Identity"));
    }
    public static boolean canEmergency(ServerPlayer p){return emergencyAsset(p).isPresent()&&p.getAttachedOrCreate(ModRegistries.BODY).rescueUntil()<=p.level().getGameTime();}
    private static Optional<Integer> emergencyAsset(ServerPlayer p){
        var frame=FRAMES.get(p.getUUID());if(frame==null||frame.player()!=p||frame.level()!=p.level()||p.isSpectator()||p.isCreative()||EffectivePower.strength(p,Art.FERUCHEMY,Metal.GOLD)<=0)return Optional.empty();
        var ledger=MetalmindLedger.get(p.level().getServer());
        for(int i=0;i<12;i++)try{
            if(frame.engine().mode(i)!=FeruchemicalEngine.Mode.TAP)continue;var data=ContactService.equipment(p).item(i).get(ModRegistries.METALMIND);if(data==null)continue;
            var a=ledger.owned(data.token(),new AssetLease(p.getUUID(),i));if(a.attribute()==FeruchemicalAttribute.HEALTH&&a.charge()>=dev.metallicarts.config.AdvancedConfig.get().body().emergencyCost()){key(p,a);return Optional.of(i);}
        }catch(IllegalArgumentException ignored){}
        return Optional.empty();
    }
    public static boolean rescue(ServerPlayer p,DamageSource source){
        if(source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)||p.getAttachedOrCreate(ModRegistries.BODY).rescueUntil()>p.level().getGameTime())return false;
        var slot=emergencyAsset(p);if(slot.isEmpty())return false;
        var data=ContactService.equipment(p).item(slot.get()).get(ModRegistries.METALMIND);var ledger=MetalmindLedger.get(p.level().getServer());var a=ledger.owned(data.token(),new AssetLease(p.getUUID(),slot.get()));
        ledger.change(a.token(),a.lease().orElseThrow(),key(p,a),-dev.metallicarts.config.AdvancedConfig.get().body().emergencyCost());p.setAttached(ModRegistries.BODY,p.getAttachedOrCreate(ModRegistries.BODY).rescueUntil(p.level().getGameTime()+dev.metallicarts.config.AdvancedConfig.get().body().emergencyCooldown()));p.setHealth(Math.min(4,p.getMaxHealth()));
        p.sendOverlayMessage(net.minecraft.network.chat.Component.literal("Your goldmind paid for emergency recovery"));return true;
    }
    private static void cure(ServerPlayer p){
        var frame=FRAMES.get(p.getUUID());if(frame==null||EffectivePower.strength(p,Art.FERUCHEMY,Metal.GOLD)<=0||!p.hasEffect(MobEffects.POISON)&&!p.hasEffect(MobEffects.WITHER)&&p.getAttachedOrCreate(ModRegistries.TRAUMA).total()==0)return;
        for(int i=0;i<12;i++)try{
            if(frame.engine().mode(i)!=FeruchemicalEngine.Mode.TAP)continue;var data=ContactService.equipment(p).item(i).get(ModRegistries.METALMIND);if(data==null)continue;var ledger=MetalmindLedger.get(p.level().getServer());var a=ledger.owned(data.token(),new AssetLease(p.getUUID(),i));
            if(a.attribute()!=FeruchemicalAttribute.HEALTH||a.charge()<dev.metallicarts.config.AdvancedConfig.get().body().cureCost())continue;ledger.change(a.token(),a.lease().orElseThrow(),key(p,a),-dev.metallicarts.config.AdvancedConfig.get().body().cureCost());
            if(p.hasEffect(MobEffects.POISON))p.removeEffect(MobEffects.POISON);else if(p.hasEffect(MobEffects.WITHER))p.removeEffect(MobEffects.WITHER);else p.setAttached(ModRegistries.TRAUMA,p.getAttachedOrCreate(ModRegistries.TRAUMA).heal());return;
        }catch(IllegalArgumentException ignored){}
    }
    public static boolean unsealHeld(ServerPlayer p){
        if(effect(p,FeruchemicalAttribute.IDENTITY)>-1||effect(p,FeruchemicalAttribute.CONNECTION)<=0||EffectivePower.strength(p,Art.FERUCHEMY,Metal.NICROSIL)<=0)throw new IllegalArgumentException("Need paid full Identity suppression, tapped Connection and nicrosil access");
        var held=p.getMainHandItem();var data=held.get(ModRegistries.METALMIND);var binder=p.getOffhandItem();var form=EconomyContent.form(binder.getItem());
        if(held.getCount()!=1||data==null||binder.getCount()<dev.metallicarts.config.AdvancedConfig.get().constructs().unsealingRings()||form==null||!form.material().equals("duralumin")||!form.kind().equals("ring")||binder.has(ModRegistries.METALMIND)||binder.has(ModRegistries.SPIKE))throw new IllegalArgumentException("Hold a free typed nicrosilmind and an ordinary Duralumin ring in the offhand");
        var legacy=binder.get(ModRegistries.METAL_DATA);var composition=binder.get(EconomyContent.COMPOSITION);
        if(legacy!=null&&(legacy.charge()>0||legacy.identity().isPresent())||composition!=null&&!composition.viable())throw new IllegalArgumentException("Binder must be ordinary viable metal");
        var next=MetalmindLedger.get(p.level().getServer()).unseal(data.token());binder.shrink(dev.metallicarts.config.AdvancedConfig.get().constructs().unsealingRings());held.set(ModRegistries.METALMIND,MetalmindData.of(next));return true;
    }
    public static boolean memory(ServerPlayer p,MemoryAction request){
        int tick=p.level().getServer().getTickCount();var throttle=REQUESTS.computeIfAbsent(p.getUUID(),u->new int[]{tick,0});if(tick-throttle[0]>=20){throttle[0]=tick;throttle[1]=0;}
        if(++throttle[1]>20||!dev.metallicarts.hemalurgy.ConstructRuntime.canUsePowers(p)||!p.isAlive()||p.isSpectator()||request.slot()<0||request.slot()>=12||request.page()<0||request.page()>42||request.action()<0||request.action()>3||request.text()==null||request.text().length()>64||request.text().chars().anyMatch(Character::isISOControl))return false;
        try{
            if(EffectivePower.strength(p,Art.FERUCHEMY,Metal.COPPER)<=0)throw new IllegalArgumentException("Need Copper Feruchemy");
            var data=ContactService.equipment(p).item(request.slot()).get(ModRegistries.METALMIND);if(data==null)throw new IllegalArgumentException("Choose a contacted coppermind");
            var ledger=MetalmindLedger.get(p.level().getServer());var lease=new AssetLease(p.getUUID(),request.slot());var a=ledger.owned(data.token(),lease);if(a.attribute()!=FeruchemicalAttribute.MEMORY)throw new IllegalArgumentException("Choose a coppermind");UUID key=key(p,a);
            var journal=p.getAttachedOrCreate(ModRegistries.JOURNAL);
            if(request.action()==1){var entry=journal.memories().stream().filter(m->m.id().equals(request.memory())).findFirst().orElseThrow(()->new IllegalArgumentException("Memory no longer accessible"));ledger.storeMemory(a.token(),lease,key,entry,effect(p,FeruchemicalAttribute.IDENTITY)<=-1);p.setAttached(ModRegistries.JOURNAL,journal.forget(entry.id()));}
            if(request.action()==2){
                if(journal.memories().size()>=256||journal.memories().stream().anyMatch(m->m.id().equals(request.memory())))throw new IllegalArgumentException("Memory already accessible or journal full");
                var restored=ledger.takeMemory(a.token(),lease,key,request.memory());p.setAttached(ModRegistries.JOURNAL,journal.remember(restored.memory()));
            }
            if(request.action()==3)p.setAttached(ModRegistries.JOURNAL,p.getAttachedOrCreate(ModRegistries.JOURNAL).rename(request.memory(),request.text()));
            String query=request.action()==3?"":request.text();
            a=ledger.owned(data.token(),lease);var json=AdvancedAllomancy.journalData(p,request.page());
            var available=p.getAttachedOrCreate(ModRegistries.JOURNAL).memories().stream().filter(m->ArtsJournal.matches(m,query)).toList();json.add("memories",new Gson().toJsonTree(available.stream().skip(request.page()*6L).limit(6).toList()));json.addProperty("memoryTotal",available.size());json.addProperty("query",query);var gson=new Gson();json.add("stored",gson.toJsonTree(a.content().memories().stream().filter(m->ArtsJournal.matches(m,query)).skip(request.page()*6L).limit(6).toList()));json.addProperty("storedTotal",a.content().memories().stream().filter(m->ArtsJournal.matches(m,query)).count());json.addProperty("memorySlot",request.slot());
            if(ServerPlayNetworking.canSend(p,ArtsFeedback.TYPE))ServerPlayNetworking.send(p,new ArtsFeedback("Copper memories: access moves with the canonical record",json.toString()));return true;
        }catch(IllegalArgumentException e){AdvancedAllomancy.feedback(p,e.getMessage(),false);return false;}
    }
    public static String cue(ServerPlayer p){return CUES.getOrDefault(p.getUUID(),"");}
    private static void modifier(ServerPlayer p,net.minecraft.core.Holder<Attribute> attribute,String key,double amount,AttributeModifier.Operation operation){var instance=p.getAttribute(attribute);if(instance==null)return;var id=MetallicArts.id(key);if(amount==0)instance.removeModifier(id);else if(instance.getModifier(id)==null||instance.getModifier(id).amount()!=amount)instance.addOrUpdateTransientModifier(new AttributeModifier(id,amount,operation));}
    public static void stop(ServerPlayer p){
        FRAMES.remove(p.getUUID());REQUESTS.remove(p.getUUID());CUES.remove(p.getUUID());EffectivePower.stop(p);SocialConnections.stop(p);
        modifier(p,Attributes.ATTACK_SPEED,"mental_actions",0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);modifier(p,Attributes.BLOCK_BREAK_SPEED,"mental_precision",0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        modifier(p,Attributes.MOVEMENT_SPEED,"determination",0,AttributeModifier.Operation.ADD_VALUE);modifier(p,Attributes.MAX_HEALTH,"stored_health_capacity",0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        modifier(p,Attributes.LUCK,"fortune",0,AttributeModifier.Operation.ADD_VALUE);modifier(p,Attributes.MOVEMENT_SPEED,"body_fatigue",0,AttributeModifier.Operation.ADD_VALUE);modifier(p,Attributes.MOVEMENT_SPEED,"body_temperature",0,AttributeModifier.Operation.ADD_VALUE);
    }
    private AdvancedFeruchemy(){}
}
