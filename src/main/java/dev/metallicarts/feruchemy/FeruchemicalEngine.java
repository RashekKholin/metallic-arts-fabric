package dev.metallicarts.feruchemy;

import dev.metallicarts.domain.*;
import dev.metallicarts.equipment.*;
import java.util.*;

/** Paid per-channel transactions. Runtime contexts surrender finite body resources before credit. */
public final class FeruchemicalEngine {
    public enum Mode { OFF, STORE, TAP }
    public interface Context {
        default double access(Spiritweb state,Art art,Metal metal){return state.strength(art,metal);}
        default Set<UUID> keys(Metal metal){return Set.of();}
        default boolean allowStore(MetalmindAsset asset){return true;}
        default int storeLimit(MetalmindAsset asset,int units){return units;}
        default int payStore(MetalmindAsset asset,int units){return units;}
        default int tapLimit(MetalmindAsset asset,int units){return units;}
        default boolean waitWhenFull(MetalmindAsset asset){return false;}
        default void paid(MetalmindAsset asset,int slot,int units){}
        default boolean blankIdentity(){return false;}
    }
    private final Mode[] modes=new Mode[AssetLease.SLOTS];
    private final int[] rates=new int[AssetLease.SLOTS];
    private final double[] paid=new double[AssetLease.SLOTS];
    public FeruchemicalEngine(){Arrays.fill(modes,Mode.OFF);Arrays.fill(rates,1);}
    public Mode mode(int slot){return modes[slot];}
    public int rate(int slot){return rates[slot];}
    public double paid(int slot){return paid[slot];}
    public void stop(int slot){modes[slot]=Mode.OFF;paid[slot]=0;}
    public void stopAll(){Arrays.fill(modes,Mode.OFF);Arrays.fill(paid,0);}
    public static long tappingCost(int aggregateUnits){
        if(aggregateUnits<0||aggregateUnits>4000)throw new IllegalArgumentException("Invalid tapping throughput");
        return ((long)aggregateUnits*Math.max(1000,aggregateUnits)+999)/1000;
    }
    private static Context keys(java.util.function.Function<Metal,Set<UUID>> keys){return new Context(){@Override public Set<UUID> keys(Metal metal){return keys.apply(metal);}};}
    public boolean request(int slot,Mode mode,int rate,List<Optional<AssetToken>> equipment,MetalmindLedger ledger,UUID player,Spiritweb state){return request(slot,mode,rate,equipment,ledger,player,state,new Context(){});}
    public boolean request(int slot,Mode mode,int rate,List<Optional<AssetToken>> equipment,MetalmindLedger ledger,UUID player,Spiritweb state,java.util.function.Function<Metal,Set<UUID>> keys){return request(slot,mode,rate,equipment,ledger,player,state,keys(keys));}
    public boolean request(int slot,Mode mode,int rate,List<Optional<AssetToken>> equipment,MetalmindLedger ledger,UUID player,Spiritweb state,Context context){
        if(slot<0||slot>=AssetLease.SLOTS||equipment.size()!=AssetLease.SLOTS||rate<1||rate>5)return false;
        if(mode==Mode.OFF){stop(slot);return true;}
        try{
            var asset=ledger.owned(equipment.get(slot).orElseThrow(),new AssetLease(player,slot));
            if(asset.attribute()==FeruchemicalAttribute.MEMORY)return false;
            if(mode==Mode.STORE&&(rate>(asset.attribute()==FeruchemicalAttribute.IDENTITY?4:3)||!context.allowStore(asset)))return false;
            if(access(state,asset,mode,context)<=0||key(asset,state,context,mode)==null)return false;
            for(int i=0;i<AssetLease.SLOTS;i++)if(i!=slot&&modes[i]!=Mode.OFF&&modes[i]!=mode&&equipment.get(i).isPresent()){
                var other=ledger.owned(equipment.get(i).get(),new AssetLease(player,i));if(channel(other).equals(channel(asset)))return false;
            }
            modes[slot]=mode;rates[slot]=rate;return true;
        }catch(IllegalArgumentException|NoSuchElementException e){return false;}
    }
    public Map<FeruchemicalAttribute,Double> step(List<Optional<AssetToken>> equipment,MetalmindLedger ledger,UUID player,Spiritweb state){return step(equipment,ledger,player,state,new Context(){});}
    public Map<FeruchemicalAttribute,Double> step(List<Optional<AssetToken>> equipment,MetalmindLedger ledger,UUID player,Spiritweb state,java.util.function.Function<Metal,Set<UUID>> keys){return step(equipment,ledger,player,state,keys(keys));}
    public Map<FeruchemicalAttribute,Double> step(List<Optional<AssetToken>> equipment,MetalmindLedger ledger,UUID player,Spiritweb state,Context context){
        if(equipment.size()!=AssetLease.SLOTS)throw new IllegalArgumentException("Invalid contact equipment");Arrays.fill(paid,0);
        var channels=new HashMap<String,Integer>();var totals=new EnumMap<FeruchemicalAttribute,Integer>(FeruchemicalAttribute.class);
        var order=new ArrayList<Integer>();for(int i=0;i<AssetLease.SLOTS;i++)order.add(i);
        order.sort(Comparator.comparingInt(i->{try{return priority(ledger.owned(equipment.get(i).orElseThrow(),new AssetLease(player,i)),modes[i]);}catch(IllegalArgumentException|NoSuchElementException e){return 5;}}));
        for(int slot:order){
            if(modes[slot]==Mode.OFF)continue;
            try{
                var lease=new AssetLease(player,slot);var asset=ledger.owned(equipment.get(slot).orElseThrow(),lease);var channel=channel(asset);
                double access=access(state,asset,modes[slot],context);UUID key=key(asset,state,context,modes[slot]);
                if(access<=0||key==null||asset.attribute()==FeruchemicalAttribute.MEMORY){stop(slot);continue;}
                int previous=channels.getOrDefault(channel,0),actual;
                if(modes[slot]==Mode.STORE){
                    if(previous>0||!context.allowStore(asset)){stop(slot);continue;}
                    int cap=asset.attribute()==FeruchemicalAttribute.IDENTITY?1000:750;
                    int requested=(int)Math.min(Math.min(rates[slot]*250,cap+previous),asset.capacity()-asset.charge());
                    requested=Math.max(0,Math.min(requested,context.storeLimit(asset,requested)));
                    if(requested==0){stop(slot);continue;}
                    actual=Math.max(0,Math.min(requested,context.payStore(asset,requested)));
                    if(actual==0){stop(slot);continue;}
                    boolean blank=context.blankIdentity()||asset.attribute()==FeruchemicalAttribute.IDENTITY&&actual-previous>=1000;
                    ledger.change(asset.token(),lease,key,actual,blank);actual=-actual;
                }else{
                    if(previous<0){stop(slot);continue;}
                    int limit=(int)Math.floor(4000*Math.min(1,access));
                    int requested=Math.min(new int[]{250,500,1000,2000,4000}[rates[slot]-1],Math.max(0,limit-previous));
                    requested=Math.max(0,Math.min(requested,context.tapLimit(asset,requested)));
                    if(requested==0&&context.waitWhenFull(asset))continue;
                    int lo=0,hi=requested;
                    while(lo<hi){int mid=(lo+hi+1)/2;if(tappingCost(previous+mid)-tappingCost(previous)<=asset.charge())lo=mid;else hi=mid-1;}
                    if(lo==0){stop(slot);continue;}long cost=tappingCost(previous+lo)-tappingCost(previous);
                    ledger.change(asset.token(),lease,key,-cost);actual=lo;
                }
                channels.put(channel,previous+actual);totals.merge(asset.attribute(),actual,Integer::sum);paid[slot]=actual/1000.0;
                context.paid(asset,slot,actual);
            }catch(IllegalArgumentException|NoSuchElementException|ArithmeticException e){stop(slot);}
        }
        var result=new EnumMap<FeruchemicalAttribute,Double>(FeruchemicalAttribute.class);totals.forEach((attribute,value)->result.put(attribute,value/1000.0));return Map.copyOf(result);
    }
    private static int priority(MetalmindAsset asset,Mode mode){
        if(asset.attribute()==FeruchemicalAttribute.INVESTITURE&&mode==Mode.TAP)return asset.content().unsealed()?0:1;
        return asset.attribute()==FeruchemicalAttribute.IDENTITY?2:asset.attribute()==FeruchemicalAttribute.INVESTITURE?3:4;
    }
    private static String channel(MetalmindAsset asset){return asset.attribute().id()+asset.content().power().map(p->":"+p.art()+":"+p.metal()).orElse("");}
    private static double access(Spiritweb state,MetalmindAsset asset,Mode mode,Context context){
        return mode==Mode.TAP&&asset.attribute()==FeruchemicalAttribute.INVESTITURE&&asset.content().unsealed()?1:context.access(state,Art.FERUCHEMY,asset.metal());
    }
    private static UUID key(MetalmindAsset asset,Spiritweb state,Context context,Mode mode){
        if(mode==Mode.STORE&&asset.charge()==0)return state.identity();
        if(asset.accessible(state.identity()))return state.identity();return asset.identity().filter(context.keys(asset.metal())::contains).orElse(null);
    }
}
