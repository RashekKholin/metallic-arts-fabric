package dev.metallicarts.test;

import com.google.gson.*;
import dev.metallicarts.MetallicArts;
import dev.metallicarts.allomancy.ReserveLedger;
import dev.metallicarts.allomancy.AllomancyRuntime;
import dev.metallicarts.config.ServerConfig;
import dev.metallicarts.domain.*;
import dev.metallicarts.economy.EconomyContent;
import dev.metallicarts.feruchemy.*;
import dev.metallicarts.hemalurgy.*;
import dev.metallicarts.network.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Opt-in loopback fixture. Only test sources contain this orchestration/probe protocol. */
public final class MultiplayerServer implements ModInitializer {
    private static final int SOAK_TICKS=Math.clamp(Integer.getInteger("metallicarts.soakTicks",1200),1200,144000);
    private record Step(String name,Runnable start,BooleanSupplier ready,Runnable verify){}
    private MinecraftServer server;private List<Step> steps;private int index,startTick=-1,sequence;
    private final Map<UUID,Integer> expected=new HashMap<>();private final Map<UUID,JsonObject> reports=new HashMap<>();
    private final Map<String,UUID> identities=new HashMap<>();private final List<String> passed=new ArrayList<>();
    private UUID mind;private long charge,offlineCharge;private ServerPlayer oldA;private ItemEntity forceItem;private double lastForceX;
    private long began,stressStarted,tickStarted,stressNanos,stressMaximum;private int stressTicks;private boolean stressing;private final List<net.minecraft.world.entity.Mob> stressMobs=new ArrayList<>();
    @Override public void onInitialize(){
        String role=System.getProperty("metallicarts.multiplayer");if(role==null)return;
        PayloadTypeRegistry.clientboundPlay().register(MultiplayerProbe.Instruction.TYPE,MultiplayerProbe.Instruction.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(MultiplayerProbe.Reply.TYPE,MultiplayerProbe.Reply.CODEC);
        if(!role.equals("server"))return;
        ServerPlayNetworking.registerGlobalReceiver(MultiplayerProbe.Reply.TYPE,(payload,context)->{
            var p=context.player();if(expected.getOrDefault(p.getUUID(),-1)!=payload.sequence())return;
            reports.put(p.getUUID(),JsonParser.parseString(payload.data()).getAsJsonObject());
        });
        ServerLifecycleEvents.SERVER_STARTED.register(s->{server=s;began=System.currentTimeMillis();});
        ServerTickEvents.START_SERVER_TICK.register(s->{tickStarted=System.nanoTime();if(server!=null)tick();});
        ServerTickEvents.END_SERVER_TICK.register(s->{if(stressing){long duration=System.nanoTime()-tickStarted;stressNanos+=duration;stressMaximum=Math.max(stressMaximum,duration);stressTicks++;}});
    }
    private ServerPlayer a(){return server.getPlayerList().getPlayerByName("MetalTestA");}
    private ServerPlayer b(){return server.getPlayerList().getPlayerByName("MetalTestB");}
    private void command(ServerPlayer p,String action){int n=++sequence;expected.put(p.getUUID(),n);reports.remove(p.getUUID());ServerPlayNetworking.send(p,new MultiplayerProbe.Instruction(n,action));}
    private void both(String action){command(a(),action);command(b(),action);}
    private boolean ack(ServerPlayer p){return p!=null&&reports.containsKey(p.getUUID());}
    private boolean bothAck(){return ack(a())&&ack(b());}
    private JsonObject report(ServerPlayer p){return reports.get(p.getUUID());}
    private void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    private void privateData(){
        for(var p:List.of(a(),b())){
            var j=report(p);String id=identities.get(p.getGameProfile().name()).toString();
            check(j.get("identity").getAsString().equals(id),"Foreign spiritweb snapshot on "+p.getGameProfile().name());
            for(var seen:j.getAsJsonArray("identities"))check(seen.getAsString().equals(id),"A foreign Identity was observed on the client");
        }
    }
    private void grant(ServerPlayer p,Art art,Metal metal){
        var state=p.getAttachedOrCreate(ModRegistries.SPIRITWEB);UUID id=UUID.randomUUID();
        p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.grant(state,new PowerGrant(id,art,metal,PowerGrant.Origin.ADMIN,id,1)));
    }
    private long mindCharge(){return MetalmindLedger.get(server).all().stream().filter(m->m.token().instance().equals(mind)).findFirst().orElseThrow().charge();}
    private void setup(){
        try{
            var props=new Properties();try(var reader=Files.newBufferedReader(Path.of("server.properties"))){props.load(reader);}
            check("127.0.0.1".equals(props.getProperty("server-ip"))&&"false".equals(props.getProperty("online-mode")),"Fixture must be isolated loopback/offline");
        }catch(java.io.IOException e){throw new RuntimeException(e);}
        for(int x=-12;x<=12;x++)for(int z=-6;z<=6;z++)server.overworld().setBlock(new BlockPos(x,80,z),Blocks.STONE.defaultBlockState(),3);
        for(var p:List.of(a(),b())){
            p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setNoGravity(true);p.getInventory().clearContent();p.getInventory().setSelectedSlot(0);
            identities.put(p.getGameProfile().name(),p.getAttachedOrCreate(ModRegistries.SPIRITWEB).identity());
            grant(p,Art.ALLOMANCY,Metal.STEEL);grant(p,Art.ALLOMANCY,Metal.IRON);grant(p,Art.FERUCHEMY,Metal.IRON);
            p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.credit(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),Metal.STEEL,p==a()?111111:222222));
            p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.credit(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),Metal.IRON,333333));ModNetworking.send(p);
        }
        a().teleportTo(.5,81,.5);b().teleportTo(5.5,81,.5);a().getInventory().setItem(0,new ItemStack(EconomyContent.item("iron","ring")));
    }
    private boolean pickup(ServerPlayer recipient){
        var items=server.overworld().getEntitiesOfClass(ItemEntity.class,new AABB(-20,75,-20,20,90,20),e->e.getItem().has(ModRegistries.METALMIND)||e.getItem().has(ModRegistries.SPIKE));
        if(!items.isEmpty()){var item=items.getFirst();recipient.teleportTo(item.getX(),item.getY(),item.getZ());}
        return recipient.getInventory().getItem(0).has(ModRegistries.METALMIND)||recipient.getInventory().getItem(0).has(ModRegistries.SPIKE);
    }
    private void tick(){
        try{
            if(System.currentTimeMillis()-began>240000L+SOAK_TICKS*75L)throw new AssertionError("Multiplayer fixture exceeded bounded startup/soak allowance");
            if(forceItem!=null){lastForceX=forceItem.getDeltaMovement().x;forceItem.setPos(0,81.9,.5);forceItem.setDeltaMovement(Vec3.ZERO);}
            if(steps==null){if(a()==null||b()==null)return;setup();steps=plan();}
            if(index>=steps.size()){finish(null);return;}
            var step=steps.get(index);if(startTick<0){startTick=server.getTickCount();step.start().run();}
            if(server.getTickCount()-startTick>(stressing?SOAK_TICKS+1800:1800))throw new AssertionError("Timed out: "+step.name()+" reports="+reports);
            if(server.getTickCount()-startTick>=20&&step.ready().getAsBoolean()){
                step.verify().run();passed.add(step.name());MetallicArts.LOGGER.info("MULTIPLAYER PASS: {}",step.name());index++;startTick=-1;
            }
        }catch(Throwable failure){finish(failure);}
    }
    private List<Step> plan(){return List.of(
        new Step("two independent clients open private Spiritweb menus",()->both("M"),this::bothAck,()->{privateData();check(report(a()).get("steel").getAsLong()==111111&&report(b()).get("steel").getAsLong()==222222,"Private reserves crossed owners");}),
        new Step("both contact menus open",()->both("F"),this::bothAck,()->{}),
        new Step("A equips actual ring",()->command(a(),"EQUIP"),()->ack(a()),()->{mind=ContactService.equipment(a()).item(0).get(ModRegistries.METALMIND).token().instance();}),
        new Step("A stores while B remains unaffected",()->{command(a(),"STORE");command(b(),"PEEK");},()->bothAck()&&mindCharge()>=5000,()->{privateData();check(!report(b()).get("contact").getAsBoolean()&&report(b()).get("fMode").getAsInt()==0,"A's contact state leaked to B");}),
        new Step("storage stops before contention",()->command(a(),"OFF"),()->ack(a()),()->charge=mindCharge()),
        new Step("copied leased mind rejected for B",()->{b().getInventory().setItem(0,ContactService.equipment(a()).item(0));command(b(),"EQUIP");},()->ack(b()),()->{check(ContactService.equipment(b()).item(0).isEmpty()&&mindCharge()==charge,"Copied charge was usable");b().getInventory().setItem(0,ItemStack.EMPTY);}),
        new Step("A unequips for a physical transfer",()->command(a(),"UNEQUIP"),()->ack(a()),()->check(ContactService.equipment(a()).item(0).isEmpty(),"A did not release contact")),
        new Step("A drops real metalmind through client input",()->command(a(),"DROP"),()->ack(a()),()->a().teleportTo(-6,81,.5)),
        new Step("B picks up same canonical metalmind",()->{},()->pickup(b()),()->check(b().getInventory().getItem(0).get(ModRegistries.METALMIND).token().instance().equals(mind),"Transferred identity changed")),
        new Step("B equips transferred item but cannot tap foreign Identity",()->command(b(),"EQUIP"),()->ack(b()),()->check(!ContactService.equipment(b()).item(0).isEmpty(),"Transfer could not equip")),
        new Step("foreign keyed tapping is rejected on real client",()->command(b(),"TAP"),()->ack(b()),()->check(report(b()).get("fMode").getAsInt()==0&&mindCharge()==charge,"Foreign player spent keyed charge")),
        new Step("B releases transferred item",()->command(b(),"UNEQUIP"),()->ack(b()),()->{}),
        new Step("B drops it back",()->command(b(),"DROP"),()->ack(b()),()->b().teleportTo(6,81,.5)),
        new Step("A picks up original keyed asset",()->{},()->pickup(a()),()->{}),
        new Step("A re-equips and retains canonical charge",()->command(a(),"EQUIP"),()->ack(a()),()->check(mindCharge()==charge,"Transfers changed charge")),
        new Step("rightful owner taps transferred charge",()->command(a(),"TAP"),()->ack(a()),()->check(mindCharge()<charge&&report(a()).get("fMode").getAsInt()==2,"A cannot spend its returned charge")),
        new Step("independent burn states remain private",()->{command(a(),"BURN_STEEL");command(b(),"BURN_IRON");},this::bothAck,()->{privateData();check(report(a()).get("burnModes").getAsInt()==1&&report(b()).get("burnModes").getAsInt()==4,"Burn status crossed players");}),
        new Step("prepare opposing live Allomancers",()->{command(a(),"OFF");command(b(),"STOP");},this::bothAck,()->{a().teleportTo(-3,81,.5);b().teleportTo(3,81,.5);forceItem=new ItemEntity(server.overworld(),0,81.9,.5,new ItemStack(Items.IRON_NUGGET));forceItem.setNoGravity(true);forceItem.setPickUpDelay(32767);server.overworld().addFreshEntity(forceItem);}),
        new Step("both clients burn Steel",()->both("BURN_STEEL"),this::bothAck,()->{}),
        new Step("both clients push same body with reciprocal recoil",()->both("PUSH"),this::bothAck,()->{check(a().getDeltaMovement().x<0&&b().getDeltaMovement().x>0,"Reciprocal recoil missing");check(Math.abs(lastForceX)<.15,"Opposing forces do not balance");forceItem.discard();forceItem=null;}),
        new Step("stop live force input",()->both("STOP"),this::bothAck,()->{}),
        new Step("A starts storage before disconnect",()->command(a(),"STORE"),()->ack(a()),()->check(report(a()).get("fMode").getAsInt()==1,"Storage not active before logout")),
        new Step("A disconnects while B stays connected",()->{oldA=a();command(a(),"RECONNECT");},()->a()==null&&b()!=null,()->offlineCharge=mindCharge()),
        new Step("A rejoins with same persistent ownership",()->{},()->a()!=null&&a()!=oldA,()->check(mindCharge()==offlineCharge,"Charge changed while owner was offline")),
        new Step("reconnected client has no active modes",()->both("F"),this::bothAck,()->{privateData();check(report(a()).get("fMode").getAsInt()==0&&report(a()).get("burnModes").getAsInt()==0,"Actions resumed after reconnect");grant(b(),Art.ALLOMANCY,Metal.TIN);ModNetworking.send(b());a().teleportTo(.5,81,.5);b().teleportTo(.5,81,2.5);a().getInventory().setItem(0,new ItemStack(EconomyContent.item("steel","spike")));}),
        new Step("aim at real player donor",()->{command(a(),"AIM_Z");command(b(),"CONSENT");},this::bothAck,()->{}),
        new Step("default policy blocks extraction despite consent",()->command(a(),"EXTRACT"),()->ack(a()),()->{check(b().isAlive()&&!a().getMainHandItem().has(ModRegistries.SPIKE),"Default policy did not protect donor");MetallicArts.config=new ServerConfig(1,true,20);}),
        new Step("donor revokes consent",()->command(b(),"CONSENT"),()->ack(b()),()->{}),
        new Step("enabled policy alone cannot override donor consent",()->command(a(),"EXTRACT"),()->ack(a()),()->check(b().isAlive()&&!a().getMainHandItem().has(ModRegistries.SPIKE),"Consent bypass")),
        new Step("donor explicitly consents",()->command(b(),"CONSENT"),()->ack(b()),()->{}),
        new Step("consensual PvP extraction transfers one actual source",()->command(a(),"EXTRACT"),()->ack(a()),()->{check(!b().isAlive()&&a().getMainHandItem().has(ModRegistries.SPIKE),"Valid extraction failed: "+report(a()));check(b().getAttachedOrCreate(ModRegistries.SPIRITWEB).strength(Art.ALLOMANCY,Metal.TIN)==0,"Donor kept extracted source");}),
        new Step("donor requests ordinary respawn",()->command(b(),"RESPAWN"),()->ack(b())&&b().isAlive(),()->check(b().getAttachedOrCreate(ModRegistries.SPIRITWEB).strength(Art.ALLOMANCY,Metal.TIN)==0,"Stolen source regenerated on respawn")),
        new Step("recipient implants real charged spike",()->command(a(),"INSERT"),()->ack(a()),()->check(a().getAttachedOrCreate(ModRegistries.SPIRITWEB).strength(Art.ALLOMANCY,Metal.TIN)>.8,"Implant did not grant stolen source")),
        new Step("removal revokes only stolen source",()->command(a(),"REMOVE"),()->ack(a()),()->check(a().getAttachedOrCreate(ModRegistries.SPIRITWEB).strength(Art.ALLOMANCY,Metal.TIN)==0&&a().getAttachedOrCreate(ModRegistries.SPIRITWEB).strength(Art.ALLOMANCY,Metal.STEEL)==1,"Removal damaged independent powers")),
        new Step("prepare advanced Arts on independent clients",()->both("STOP"),this::bothAck,()->{
            a().teleportTo(.5,81,.5);b().teleportTo(.5,81,2.5);
            for(var p:List.of(a(),b()))for(var m:List.of(Metal.BRONZE,Metal.COPPER,Metal.ZINC,Metal.CHROMIUM,Metal.NICROSIL,Metal.CADMIUM,Metal.BENDALLOY)){grant(p,Art.ALLOMANCY,m);p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.credit(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),m,200000));}
            for(var p:List.of(a(),b())){p.setAttached(ModRegistries.JOURNAL,new dev.metallicarts.arts.ArtsJournal(List.of(new dev.metallicarts.arts.MemoryEntry(UUID.randomUUID(),"private",p==a()?"A private route":"B private route","minecraft:overworld",1,80,2,1)),List.of(),List.of()));ModNetworking.send(p);}
        }),
        new Step("live Bronze identifies another client's actual paid Steel",()->{command(a(),"BURN:7");command(b(),"BURN_STEEL");},this::bothAck,()->{}),
        new Step("detected pulse reaches only the seeking client",()->both("PEEK"),this::bothAck,()->check(report(a()).get("cue").getAsString().contains("Pulse steel"),"Bronze did not report actual target metal")),
        new Step("live Copper conceals the burning target",()->command(b(),"BURN:6"),()->ack(b()),()->{}),
        new Step("equal-strength cloud hides pulse on the real seeker UI",()->command(a(),"PEEK"),()->ack(a()),()->check(report(a()).get("cue").getAsString().contains("no detectable"),"Copper failed to conceal live pulse")),
        new Step("stop cloud before consent-protected emotional influence",()->both("STOP"),this::bothAck,()->{}),
        new Step("select fear and aim at actual second player",()->{command(a(),"FEAR");command(b(),"PEEK");},this::bothAck,()->command(a(),"AIM_Z")),
        new Step("paid Zinc changes target's emotional profile",()->command(a(),"BURN:4"),()->ack(a()),()->check(b().getAttachedOrCreate(ModRegistries.EMOTIONS).value(dev.metallicarts.arts.EmotionProfile.Emotion.FEAR)>.25,"Actual player emotion did not change")),
        new Step("stop emotion and restore target fuel",()->both("STOP"),this::bothAck,()->{b().setAttached(ModRegistries.SPIRITWEB,ReserveLedger.credit(b().getAttachedOrCreate(ModRegistries.SPIRITWEB),Metal.STEEL,200000));}),
        new Step("real contact Chromium depletes target reserves",()->{command(a(),"BURN:10");command(b(),"BURN_STEEL");},this::bothAck,()->check(b().getAttachedOrCreate(ModRegistries.SPIRITWEB).reserves().isEmpty(),"Leech did not remove real target fuel")),
        new Step("stop leech and prepare forced burst",()->both("STOP"),this::bothAck,()->{b().setAttached(ModRegistries.SPIRITWEB,ReserveLedger.credit(b().getAttachedOrCreate(ModRegistries.SPIRITWEB),Metal.STEEL,200000));}),
        new Step("real contact Nicrosil exhausts concurrently active target Steel",()->{command(a(),"BURN:11");command(b(),"BURN_STEEL");},this::bothAck,()->check(ReserveLedger.ordinary(b().getAttachedOrCreate(ModRegistries.SPIRITWEB),Metal.STEEL)==0,"Forced burst did not consume actual concurrent fuel")),
        new Step("private advanced journals remain owner-only",()->both("M"),this::bothAck,()->{check(report(a()).get("journal").getAsString().contains("A private route")&&!report(a()).get("journal").getAsString().contains("B private route"),"A received foreign memory");check(report(b()).get("journal").getAsString().contains("B private route")&&!report(b()).get("journal").getAsString().contains("A private route"),"B received foreign memory");}),
        new Step("prepare temporal load with forty mobs and four real furnaces",()->both("STOP"),this::bothAck,()->{
            a().teleportTo(.5,81,.5);b().teleportTo(2.5,81,.5);for(var p:List.of(a(),b()))for(var m:List.of(Metal.CADMIUM,Metal.BENDALLOY))p.setAttached(ModRegistries.SPIRITWEB,ReserveLedger.credit(p.getAttachedOrCreate(ModRegistries.SPIRITWEB),m,SOAK_TICKS*100L+200000));
            for(int i=0;i<40;i++){var mob=net.minecraft.world.entity.EntityType.PIG.create(server.overworld(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);mob.setPos(.5+i%8,81,.5+i/8);mob.setInvulnerable(true);server.overworld().addFreshEntity(mob);stressMobs.add(mob);}
            for(int i=0;i<4;i++){var pos=new BlockPos(i*2,80,4);server.overworld().setBlock(pos,Blocks.FURNACE.defaultBlockState(),3);var furnace=(net.minecraft.world.level.block.entity.FurnaceBlockEntity)server.overworld().getBlockEntity(pos);furnace.setItem(0,new ItemStack(Items.IRON_ORE,64));furnace.setItem(1,new ItemStack(Items.COAL,8));}
        }),
        new Step("bounded extended overlapping paid temporal fields under load",()->{command(a(),"BURN:14");command(b(),"BURN:15");stressStarted=server.getTickCount();stressing=true;},()->bothAck()&&server.getTickCount()-stressStarted>=SOAK_TICKS,()->{stressing=false;check(stressTicks>=SOAK_TICKS-10,"Insufficient measured ticks");check(AllomancyRuntime.power(a(),Metal.CADMIUM)>0&&AllomancyRuntime.power(b(),Metal.BENDALLOY)>0,"Temporal fuel ran out during soak");for(int i=0;i<4;i++){var furnace=(net.minecraft.world.level.block.entity.FurnaceBlockEntity)server.overworld().getBlockEntity(new BlockPos(i*2,80,4));check(furnace.getItem(0).getCount()+furnace.getItem(2).getCount()==64&&furnace.getItem(2).getCount()>0,"Temporal machine lost/created inputs or stopped");}stressMobs.forEach(net.minecraft.world.entity.Entity::discard);}),
        new Step("final owner-only snapshots on both live clients",()->both("M"),this::bothAck,this::privateData)
    );}
    private void finish(Throwable failure){
        try{
            var result=new JsonObject();result.addProperty("status",failure==null?"PASS":"FAIL");result.addProperty("authenticatedAccounts",false);result.addProperty("clients",2);
            result.addProperty("temporalStressTicks",stressTicks);result.addProperty("temporalStressMeanTickMs",stressTicks==0?0:stressNanos/1000000.0/stressTicks);result.addProperty("temporalStressMaxTickMs",stressMaximum/1000000.0);result.addProperty("temporalStressMobs",40);result.addProperty("temporalStressFurnaces",4);
            result.addProperty("completedChecks",passed.size());var checks=new JsonArray();passed.forEach(checks::add);result.add("checks",checks);
            if(failure!=null)result.addProperty("failure",failure.toString());
            Files.writeString(Path.of("multiplayer-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));
        }catch(Exception e){throw new RuntimeException(e);}finally{var s=server;server=null;s.halt(false);}
    }
}
