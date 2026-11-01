package dev.metallicarts.hemalurgy;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.*;
import dev.metallicarts.equipment.*;
import dev.metallicarts.network.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Main-thread physical transactions and source-specific grants. Advanced stolen types retain explicit hooks. */
public final class HemalurgyRuntime {
    private static final Set<UUID> CONSENT=new HashSet<>();
    private static final Map<UUID,int[]> REQUESTS=new HashMap<>();
    private static final Map<UUID,Map<StolenPower.Kind,Double>> PASSIVE=new HashMap<>();
    private static final Map<UUID,Integer> VULNERABILITY=new HashMap<>();
    public static ContactEquipment equipment(ServerPlayer p){return p.getAttachedOrCreate(ModRegistries.SPIKES);}
    private static long now(ServerPlayer p){return p.level().getServer().overworld().getGameTime();}
    public static void initialize(){
        BindingPoints.ALL.size();
        PayloadTypeRegistry.serverboundPlay().register(SpikeIntent.TYPE,SpikeIntent.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SpikeSnapshot.TYPE,SpikeSnapshot.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SpikeIntent.TYPE,(payload,context)->act(context.player(),payload));
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->{
            CONSENT.remove(handler.player.getUUID());REQUESTS.remove(handler.player.getUUID());PASSIVE.remove(handler.player.getUUID());VULNERABILITY.remove(handler.player.getUUID());
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server->{CONSENT.clear();REQUESTS.clear();PASSIVE.clear();VULNERABILITY.clear();});
        HemalurgyCommands.initialize();
    }
    public static boolean act(ServerPlayer p,SpikeIntent intent){
        int tick=p.level().getServer().getTickCount();var limit=REQUESTS.computeIfAbsent(p.getUUID(),u->new int[]{tick,0});
        if(tick-limit[0]>=20){limit[0]=tick;limit[1]=0;}
        if(++limit[1]>20||intent.action()<0||intent.action()>7||intent.point()<0||intent.point()>=12||intent.power()<0||intent.power()>=StolenPower.choices().size())return false;
        if(intent.action()==0){send(p,"");return true;}
        if(!p.isAlive()||p.isSpectator())return false;
        try{
            switch(intent.action()){
                case 1 -> implant(p,intent.point(),false);
                case 2 -> remove(p,intent.point());
                case 3 -> extract(p,aimedDonor(p).orElseThrow(()->new IllegalArgumentException("Aim at a living humanoid donor within three blocks")),StolenPower.choices().get(intent.power()));
                case 4 -> preserve(p);
                case 5 -> {if(!CONSENT.remove(p.getUUID()))CONSENT.add(p.getUUID());}
                case 6 -> implant(p,intent.point(),true);
                case 7 -> drawBlood(p);
                default -> throw new IllegalArgumentException("Unknown action");
            }
            tick(p,tick);send(p,"Action completed");return true;
        }catch(IllegalArgumentException|ArithmeticException e){send(p,e.getMessage());return false;}
    }
    public static void implant(ServerPlayer p,int point,boolean criticalIntent){
        var equipment=equipment(p);var binding=BindingPoints.ALL.get(point);var held=p.getMainHandItem();
        if(!equipment.item(point).isEmpty())throw new IllegalArgumentException("Binding point occupied");
        if(binding.critical()&&!criticalIntent)throw new IllegalArgumentException("Critical point: use the explicit dangerous insertion action");
        var preview=held.get(ModRegistries.SPIKE);var form=EconomyContent.form(held.getItem());
        if(held.getCount()!=1||preview==null||form==null||!List.of("spike","earring").contains(form.kind()))throw new IllegalArgumentException("Hold one canonically charged spike or earring");
        var ledger=SpikeLedger.get(p.level().getServer());var asset=ledger.resolve(preview.token());
        if(!asset.metal().id().equals(form.material())||!binding.accepts(asset.metal()))throw new IllegalArgumentException("Material incompatible with this binding point");
        if(asset.drained()||asset.power().kind()==StolenPower.Kind.NULLIFICATION)throw new IllegalArgumentException("This spike is spent");
        long existing=p.getAttachedOrCreate(ModRegistries.SPIRITWEB).grants().stream().filter(g->g.origin()!=PowerGrant.Origin.HEMALURGIC||!ledger.known(g.source())).count();
        if(existing+12>128)throw new IllegalArgumentException("Spiritweb grant capacity reached");
        var worn=ledger.claim(asset.token(),new AssetLease(p.getUUID(),point),now(p));
        var stack=held.copyWithCount(1);stack.set(ModRegistries.SPIKE,SpikeData.of(worn));
        p.setAttached(ModRegistries.SPIKES,equipment.with(point,stack));p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
    }
    public static void remove(ServerPlayer p,int point){
        var equipment=equipment(p);var stack=equipment.item(point);var data=stack.get(ModRegistries.SPIKE);
        if(data==null)throw new IllegalArgumentException("Binding point empty");
        int slot=-1;for(int i=0;i<36;i++)if(p.getInventory().getItem(i).isEmpty()){slot=i;break;}
        if(slot<0)throw new IllegalArgumentException("Make inventory space first");
        boolean critical=BindingPoints.ALL.get(point).critical()&&equipment.items().stream().filter(s->s.has(ModRegistries.SPIKE)).count()>1;
        var free=SpikeLedger.get(p.level().getServer()).release(data.token(),new AssetLease(p.getUUID(),point),now(p));
        stack.set(ModRegistries.SPIKE,SpikeData.of(free));p.getInventory().setItem(slot,stack);
        p.setAttached(ModRegistries.SPIKES,equipment.with(point,ItemStack.EMPTY));
        p.setAttached(ModRegistries.SPIRITWEB,p.getAttachedOrCreate(ModRegistries.SPIRITWEB).withoutSource(data.token().instance()));
        tick(p,p.level().getServer().getTickCount());
        // Explicit experimental linchpin policy, once per removal; never a serialized repeated death action.
        if(critical)p.hurtServer(p.level(),p.damageSources().magic(),Float.MAX_VALUE);
    }
    public static Optional<LivingEntity> aimedDonor(ServerPlayer p){
        Vec3 eye=p.getEyePosition(),look=p.getLookAngle();
        return p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(3),e->e!=p&&e.isAlive()&&!e.isSpectator()&&p.hasLineOfSight(e)).stream()
            .filter(e->{var delta=e.position().add(0,e.getBbHeight()*.55,0).subtract(eye);double along=delta.dot(look);return along>0&&delta.length()<=3&&delta.subtract(look.scale(along)).length()<.5;})
            .min(Comparator.comparingDouble(p::distanceToSqr));
    }
    public static void extract(ServerPlayer p,LivingEntity donor,StolenPower power){
        if(!p.isAlive()||p.isSpectator()||donor==p||!donor.isAlive()||donor.level()!=p.level()||p.distanceToSqr(donor)>9||!p.hasLineOfSight(donor))
            throw new IllegalArgumentException("Invalid living donor or reach");
        if(!(donor instanceof ServerPlayer)&&!donor.getType().builtInRegistryHolder().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,MetallicArts.id("hemalurgic_donors"))))
            throw new IllegalArgumentException("This entity is not a supported humanoid donor");
        if(donor instanceof ServerPlayer other&&(!MetallicArts.config.allowPlayerHemalurgy()||!CONSENT.contains(other.getUUID())||!p.canHarmPlayer(other)||other.isCreative()))
            throw new IllegalArgumentException("Player extraction requires server policy, donor consent and PvP permission");
        if(donor instanceof ServerPlayer goldDonor&&dev.metallicarts.arts.AdvancedFeruchemy.canEmergency(goldDonor))throw new IllegalArgumentException("Donor has active paid Gold emergency recovery");
        if(donor.getMainHandItem().is(Items.TOTEM_OF_UNDYING)||donor.getOffhandItem().is(Items.TOTEM_OF_UNDYING))throw new IllegalArgumentException("Donor has a death-prevention item");
        var held=p.getMainHandItem();var form=EconomyContent.form(held.getItem());
        if(held.getCount()!=1||form==null||!List.of("spike","earring").contains(form.kind())||held.has(ModRegistries.SPIKE)||held.has(ModRegistries.METALMIND))
            throw new IllegalArgumentException("Hold one uncharged spike or earring");
        var legacy=held.get(ModRegistries.METAL_DATA);
        if(legacy!=null&&(legacy.charge()>0||legacy.identity().isPresent()))throw new IllegalArgumentException("Invested material cannot be repurposed");
        var composition=held.get(EconomyContent.COMPOSITION);
        if(composition!=null&&!composition.viable())throw new IllegalArgumentException("Use correctly composed viable material");
        var metal=Materials.metal(form.material()).orElseThrow();if(!power.compatible(metal))throw new IllegalArgumentException("Spike material cannot steal this capability");
        var before=donor.getAttachedOrCreate(ModRegistries.SPIRITWEB);PowerGrant chosen=null;
        if(power.art().isPresent())chosen=before.grants().stream().filter(g->g.art()==power.art().get()&&g.metal()==power.metal()).max(Comparator.comparingDouble(PowerGrant::strength)).orElseThrow(()->new IllegalArgumentException("Donor does not own this power"));
        if(power.kind()==StolenPower.Kind.NULLIFICATION&&before.grants().isEmpty())throw new IllegalArgumentException("Donor has no powers to remove");
        UUID source=chosen==null?UUID.nameUUIDFromBytes((donor.getUUID()+":"+power.kind()).getBytes(StandardCharsets.UTF_8)):chosen.id();
        var ledger=SpikeLedger.get(p.level().getServer());if(!ledger.canCreate(source))throw new IllegalArgumentException("Donor source already extracted or world limit reached");
        if(AttackEntityCallback.EVENT.invoker().interact(p,p.level(),InteractionHand.MAIN_HAND,donor,null)!=InteractionResult.PASS)
            throw new IllegalArgumentException("Extraction blocked by attack protection");
        var escrow=held.copy();double strength=chosen==null?1:chosen.strength();PowerGrant selected=chosen;
        if(!before.equals(donor.getAttachedOrCreate(ModRegistries.SPIRITWEB))||!ItemStack.matches(escrow,p.getMainHandItem()))
            throw new IllegalArgumentException("Donor or held item changed during protection checks");
        // Reserve the unique source before damage callbacks can reenter extraction. No item exposes this token yet.
        var asset=ledger.create(metal,power,donor.getUUID(),before.identity(),source,strength,now(p));
        var remaining=power.kind()==StolenPower.Kind.NULLIFICATION?List.<PowerGrant>of():before.grants().stream().filter(g->selected==null||!g.id().equals(selected.id())).toList();
        var after=new Spiritweb(before.schema(),before.identity(),remaining,before.reserves(),before.investedReserves());
        float health=donor.getHealth();p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);donor.setAttached(ModRegistries.SPIRITWEB,after);
        boolean died;
        try{donor.hurtServer(p.level(),p.damageSources().playerAttack(p),Float.MAX_VALUE);died=!donor.isAlive();}
        catch(RuntimeException e){ledger.cancelUnpublished(asset.token());donor.setAttached(ModRegistries.SPIRITWEB,before);p.setItemInHand(InteractionHand.MAIN_HAND,escrow);throw e;}
        if(!died){ledger.cancelUnpublished(asset.token());donor.setAttached(ModRegistries.SPIRITWEB,before);donor.setHealth(health);p.setItemInHand(InteractionHand.MAIN_HAND,escrow);throw new IllegalArgumentException("Donor death prevented; power and spike restored");}
        if(chosen!=null&&chosen.origin()==PowerGrant.Origin.HEMALURGIC)ledger.drainSource(chosen.source());
        if(power.kind()==StolenPower.Kind.NULLIFICATION)before.grants().stream().filter(g->g.origin()==PowerGrant.Origin.HEMALURGIC).forEach(g->ledger.drainSource(g.source()));
        if(power.kind()==StolenPower.Kind.NULLIFICATION){ledger.drainSource(asset.token().instance());asset=ledger.resolve(asset.token());}
        escrow.set(ModRegistries.SPIKE,SpikeData.of(asset));p.setItemInHand(InteractionHand.MAIN_HAND,escrow);
        if(donor instanceof ServerPlayer other){CONSENT.remove(other.getUUID());ModNetworking.send(other);}
    }
    private static void preserve(ServerPlayer p){
        var held=p.getMainHandItem();var data=held.get(ModRegistries.SPIKE);if(held.getCount()!=1||data==null)throw new IllegalArgumentException("Hold one charged spike");
        var ledger=SpikeLedger.get(p.level().getServer());var asset=ledger.resolve(data.token());
        if(!asset.blood()&&!p.getOffhandItem().is(HemalurgyCommands.BLOOD_VIAL))throw new IllegalArgumentException("Hold a blood vial in the offhand");
        var next=ledger.preserve(asset.token(),!asset.blood(),now(p));
        if(next.blood())p.getOffhandItem().shrink(1);
        held.set(ModRegistries.SPIKE,SpikeData.of(next));
    }
    private static void drawBlood(ServerPlayer p){
        if(!p.getMainHandItem().is(Items.GLASS_BOTTLE)||p.getMainHandItem().getCount()!=1||p.getHealth()<=6||p.isCreative())throw new IllegalArgumentException("Hold one empty bottle; need more than three hearts in survival");
        if(!p.hurtServer(p.level(),p.damageSources().magic(),4))throw new IllegalArgumentException("Blood collection damage prevented");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(HemalurgyCommands.BLOOD_VIAL));
    }
    public static void tick(ServerPlayer p,int tick){
        if(!p.isAlive())CONSENT.remove(p.getUUID());
        var ledger=SpikeLedger.get(p.level().getServer());var state=p.getAttachedOrCreate(ModRegistries.SPIRITWEB);
        var grants=new ArrayList<>(state.grants().stream().filter(g->g.origin()!=PowerGrant.Origin.HEMALURGIC||!ledger.known(g.source())).toList());
        var passive=new EnumMap<StolenPower.Kind,Double>(StolenPower.Kind.class);int count=0;
        for(int slot=0;slot<12;slot++){
            var data=equipment(p).item(slot).get(ModRegistries.SPIKE);if(data==null)continue;
            try{
                var a=ledger.owned(data.token(),new AssetLease(p.getUUID(),slot));
                if(!BindingPoints.ALL.get(slot).accepts(a.metal())||a.drained())continue;
                a=ledger.advance(a.token(),now(p));count++;
                if(a.power().art().isPresent()){
                    UUID id=UUID.nameUUIDFromBytes(("spike:"+a.token().instance()).getBytes(StandardCharsets.UTF_8));
                    if(grants.size()<128)grants.add(new PowerGrant(id,a.power().art().get(),a.power().metal(),PowerGrant.Origin.HEMALURGIC,a.token().instance(),a.potency()));
                }else passive.merge(a.power().kind(),a.potency(),Double::sum);
            }catch(IllegalArgumentException ignored){/* Invalid/stale previews confer no ownership. */}
        }
        PASSIVE.put(p.getUUID(),p.isAlive()?Map.copyOf(passive):Map.of());
        int vulnerability=Math.max(0,count-(int)Math.floor(passive.getOrDefault(StolenPower.Kind.EMOTIONAL_FORTITUDE,0.0)));
        VULNERABILITY.put(p.getUUID(),vulnerability);
        double strength=passive(p,StolenPower.Kind.STRENGTH);
        modifier(p,Attributes.ATTACK_DAMAGE,"hemalurgic_strength",2*strength);
        modifier(p,Attributes.ATTACK_KNOCKBACK,"hemalurgic_force",.5*strength);
        modifier(p,Attributes.MOVEMENT_SPEED,"hemalurgic_instability",p.isAlive()?-.002*Math.max(0,vulnerability-3):0);
        if(!grants.equals(state.grants())){
            p.setAttached(ModRegistries.SPIRITWEB,new Spiritweb(state.schema(),state.identity(),grants,state.reserves(),state.investedReserves()));ModNetworking.send(p);
        }
        if(tick%20==0)send(p,"");
    }
    private static void modifier(ServerPlayer p,net.minecraft.core.Holder<Attribute> attribute,String name,double amount){
        var value=p.getAttribute(attribute);if(value==null)return;var id=MetallicArts.id(name);
        if(amount==0)value.removeModifier(id);else if(value.getModifier(id)==null||value.getModifier(id).amount()!=amount)
            value.addOrUpdateTransientModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_VALUE));
    }
    public static double passive(ServerPlayer p,StolenPower.Kind kind){return Math.min(4,PASSIVE.getOrDefault(p.getUUID(),Map.of()).getOrDefault(kind,0.0));}
    public static int vulnerability(ServerPlayer p){return VULNERABILITY.getOrDefault(p.getUUID(),0);}
    public static Set<UUID> donorKeys(ServerPlayer p,Metal metal){
        var keys=new HashSet<UUID>();var ledger=SpikeLedger.get(p.level().getServer());
        for(int i=0;i<12;i++){
            var data=equipment(p).item(i).get(ModRegistries.SPIKE);if(data==null)continue;
            try{var a=ledger.owned(data.token(),new AssetLease(p.getUUID(),i));
                boolean granted=p.getAttachedOrCreate(ModRegistries.SPIRITWEB).grants().stream().anyMatch(g->g.origin()==PowerGrant.Origin.HEMALURGIC&&g.source().equals(a.token().instance())&&g.art()==Art.FERUCHEMY&&g.metal()==metal);
                if(!a.drained()&&BindingPoints.ALL.get(i).accepts(a.metal())&&(granted&&a.power().kind()==StolenPower.Kind.FERUCHEMY&&a.power().metal()==metal||a.power().kind()==StolenPower.Kind.CONNECTION_IDENTITY))keys.add(a.donorIdentity());
            }catch(IllegalArgumentException ignored){}
        }
        return Set.copyOf(keys);
    }
    private static void send(ServerPlayer p,String message){
        if(!ServerPlayNetworking.canSend(p,SpikeSnapshot.TYPE))return;
        var equipment=equipment(p);var ledger=SpikeLedger.get(p.level().getServer());
        for(int i=0;i<12;i++){
            var stack=equipment.item(i);var data=stack.get(ModRegistries.SPIKE);if(data==null)continue;
            try{stack.set(ModRegistries.SPIKE,SpikeData.of(ledger.owned(data.token(),new AssetLease(p.getUUID(),i))));equipment=equipment.with(i,stack);}
            catch(IllegalArgumentException ignored){}
        }
        ServerPlayNetworking.send(p,new SpikeSnapshot(equipment,message==null?"Action refused":message,CONSENT.contains(p.getUUID()),vulnerability(p)));
    }
    private HemalurgyRuntime(){}
}
