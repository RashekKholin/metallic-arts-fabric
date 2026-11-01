package dev.metallicarts.arts;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.allomancy.AllomancyRuntime;
import dev.metallicarts.domain.Metal;
import dev.metallicarts.hemalurgy.*;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import java.util.*;

/** One frame of combined influences; no order-dependent clamp or client-selected victim. */
public final class EmotionService {
    private record Pending(Map<EmotionProfile.Emotion,Double> changes,LivingEntity caster){}
    private static final Map<LivingEntity,Pending> PENDING=new HashMap<>();
    private static final Map<LivingEntity,LivingEntity> AFFECTED=new HashMap<>();
    public static boolean susceptible(LivingEntity target){return target instanceof ServerPlayer||target.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE,MetallicArts.id("emotion_susceptible")));}
    public static boolean influence(ServerPlayer caster,LivingEntity target,EmotionProfile.Emotion emotion,double amount){
        if(!dev.metallicarts.api.ArtsProtection.allows(caster,target,dev.metallicarts.api.ArtsProtection.Action.EMOTION))return false;
        if(!susceptible(target)||!target.isAlive()||target.isSpectator()||caster.distanceToSqr(target)>Math.pow(dev.metallicarts.config.AdvancedConfig.get().emotions().range(),2)||!caster.hasLineOfSight(target))return false;
        double resistance=1+Math.max(0,NpcFeruchemy.effect(target,dev.metallicarts.feruchemy.FeruchemicalAttribute.IDENTITY))*2;
        if(target instanceof ServerPlayer other){
            if(!caster.canHarmPlayer(other)||other.isCreative()||AllomancyRuntime.power(other,Metal.COPPER)>0)return false;
            resistance=Math.max(.25,resistance+2*AdvancedFeruchemy.effect(other,dev.metallicarts.feruchemy.FeruchemicalAttribute.DETERMINATION)+AdvancedFeruchemy.effect(other,dev.metallicarts.feruchemy.FeruchemicalAttribute.IDENTITY));
            resistance+=HemalurgyRuntime.passive(other,StolenPower.Kind.EMOTIONAL_FORTITUDE);
            resistance+=Math.max(0,other.getAttachedOrCreate(ModRegistries.EMOTIONS).value(EmotionProfile.Emotion.DETERMINATION)-.5)*2;
        }
        var pending=PENDING.computeIfAbsent(target,t->new Pending(new EnumMap<>(EmotionProfile.Emotion.class),caster));
        pending.changes().merge(emotion,Math.clamp(amount/resistance,-dev.metallicarts.config.AdvancedConfig.get().emotions().frameCap(),dev.metallicarts.config.AdvancedConfig.get().emotions().frameCap()),Double::sum);return true;
    }
    public static boolean influenceMob(Mob caster,LivingEntity target,EmotionProfile.Emotion emotion,double amount){
        if(!susceptible(target)||!dev.metallicarts.api.ArtsProtection.npcAllows(caster,target,dev.metallicarts.api.ArtsProtection.Action.EMOTION)||caster.distanceToSqr(target)>144||!caster.hasLineOfSight(target))return false;
        if(target instanceof ServerPlayer p&&AllomancyRuntime.power(p,Metal.COPPER)>0||target instanceof Mob m&&NpcArts.powers(m).getOrDefault(Metal.COPPER,0.0)>0)return false;
        double resistance=1+Math.max(0,NpcFeruchemy.effect(target,dev.metallicarts.feruchemy.FeruchemicalAttribute.IDENTITY))*2+Math.max(0,NpcFeruchemy.effect(target,dev.metallicarts.feruchemy.FeruchemicalAttribute.DETERMINATION));
        var pending=PENDING.computeIfAbsent(target,t->new Pending(new EnumMap<>(EmotionProfile.Emotion.class),caster));pending.changes().merge(emotion,Math.clamp(amount/resistance,-dev.metallicarts.config.AdvancedConfig.get().emotions().frameCap(),dev.metallicarts.config.AdvancedConfig.get().emotions().frameCap()),Double::sum);return true;
    }
    public static void finishFrame(){
        for(var entry:PENDING.entrySet()){
            var target=entry.getKey();if(AFFECTED.size()>=1024&&!AFFECTED.containsKey(target))continue;
            var profile=target.getAttachedOrCreate(ModRegistries.EMOTIONS).influence(entry.getValue().changes(),target.level().getGameTime());
            target.setAttached(ModRegistries.EMOTIONS,profile);AFFECTED.put(target,entry.getValue().caster());
        }
        PENDING.clear();
        for(var iterator=AFFECTED.entrySet().iterator();iterator.hasNext();){
            var entry=iterator.next();var target=entry.getKey();
            if(target.isRemoved()||!target.isAlive()){clearModifiers(target);iterator.remove();continue;}
            var profile=target.getAttachedOrCreate(ModRegistries.EMOTIONS).settle(target.level().getGameTime());target.setAttached(ModRegistries.EMOTIONS,profile);
            double fear=profile.value(EmotionProfile.Emotion.FEAR),anger=profile.value(EmotionProfile.Emotion.ANGER),trust=profile.value(EmotionProfile.Emotion.TRUST);
            modifier(target,Attributes.MOVEMENT_SPEED,"emotional_fear",fear>.6?-.02*(fear-.6)/.4:0);
            modifier(target,Attributes.ATTACK_DAMAGE,"emotional_anger",anger>.5?2*(anger-.5):anger<.15?-.5:0);
            if(target instanceof Mob mob&&target.tickCount%20==0){
                var threat=mob.getTarget()!=null?mob.getTarget():mob.getLastHurtByMob();
                if(fear>.65&&threat!=null){var away=mob.position().subtract(threat.position()).normalize().scale(6);mob.getNavigation().moveTo(mob.getX()+away.x,mob.getY(),mob.getZ()+away.z,1.1);}
                if(trust>.75&&mob.getTarget()==entry.getValue())mob.setTarget(null);
            }
            if(profile.awareness()==0&&Arrays.stream(EmotionProfile.Emotion.values()).allMatch(e->Math.abs(profile.value(e)-e.baseline())<.0001)){clearModifiers(target);iterator.remove();}
        }
    }
    private static void modifier(LivingEntity entity,net.minecraft.core.Holder<Attribute> attribute,String key,double value){
        var instance=entity.getAttribute(attribute);if(instance==null)return;var id=MetallicArts.id(key);
        if(value==0)instance.removeModifier(id);else if(instance.getModifier(id)==null||instance.getModifier(id).amount()!=value)instance.addOrUpdateTransientModifier(new AttributeModifier(id,value,AttributeModifier.Operation.ADD_VALUE));
    }
    private static void clearModifiers(LivingEntity target){modifier(target,Attributes.MOVEMENT_SPEED,"emotional_fear",0);modifier(target,Attributes.ATTACK_DAMAGE,"emotional_anger",0);}
    public static void clear(){AFFECTED.keySet().forEach(EmotionService::clearModifiers);AFFECTED.clear();PENDING.clear();}
    private EmotionService(){}
}
