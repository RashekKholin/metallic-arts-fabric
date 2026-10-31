package dev.metallicarts.feruchemy;

import dev.metallicarts.domain.*;
import dev.metallicarts.equipment.*;
import java.util.*;

/** One unit is 1/1000 of one baseline attribute-tick. No floating-point charge arithmetic. */
public final class FeruchemicalEngine {
    public enum Mode { OFF, STORE, TAP }
    private final Mode[] modes=new Mode[AssetLease.SLOTS];
    private final int[] rates=new int[AssetLease.SLOTS];
    public FeruchemicalEngine(){Arrays.fill(modes,Mode.OFF);Arrays.fill(rates,1);}
    public Mode mode(int slot){return modes[slot];}
    public int rate(int slot){return rates[slot];}
    public void stop(int slot){modes[slot]=Mode.OFF;}
    public void stopAll(){Arrays.fill(modes,Mode.OFF);}
    public static long tappingCost(int aggregateUnits){
        if(aggregateUnits<0||aggregateUnits>4000)throw new IllegalArgumentException("Invalid tapping throughput");
        return ((long)aggregateUnits*Math.max(1000,aggregateUnits)+999)/1000;
    }
    public boolean request(int slot,Mode mode,int rate,List<Optional<AssetToken>> equipment,MetalmindLedger ledger,UUID player,Spiritweb spiritweb){
        return request(slot,mode,rate,equipment,ledger,player,spiritweb,m->Set.of());
    }
    public boolean request(int slot,Mode mode,int rate,List<Optional<AssetToken>> equipment,MetalmindLedger ledger,UUID player,Spiritweb spiritweb,java.util.function.Function<Metal,Set<UUID>> keys){
        if(slot<0||slot>=AssetLease.SLOTS||equipment.size()!=AssetLease.SLOTS||rate<1||rate>(mode==Mode.STORE?3:5))return false;
        if(mode==Mode.OFF){stop(slot);return true;}
        try{
            var asset=ledger.owned(equipment.get(slot).orElseThrow(),new AssetLease(player,slot));
            if(spiritweb.strength(Art.FERUCHEMY,asset.metal())<=0||key(asset,spiritweb,keys)==null)return false;
            for(int i=0;i<AssetLease.SLOTS;i++)if(i!=slot&&modes[i]!=Mode.OFF&&modes[i]!=mode&&equipment.get(i).isPresent()){
                var other=ledger.owned(equipment.get(i).get(),new AssetLease(player,i));
                if(other.attribute()==asset.attribute())return false;
            }
            modes[slot]=mode;rates[slot]=rate;return true;
        }catch(IllegalArgumentException|NoSuchElementException e){return false;}
    }
    public Map<FeruchemicalAttribute,Double> step(List<Optional<AssetToken>> equipment,MetalmindLedger ledger,UUID player,Spiritweb spiritweb){
        return step(equipment,ledger,player,spiritweb,m->Set.of());
    }
    public Map<FeruchemicalAttribute,Double> step(List<Optional<AssetToken>> equipment,MetalmindLedger ledger,UUID player,Spiritweb spiritweb,java.util.function.Function<Metal,Set<UUID>> keys){
        if(equipment.size()!=AssetLease.SLOTS)throw new IllegalArgumentException("Invalid contact equipment");
        var units=new EnumMap<FeruchemicalAttribute,Integer>(FeruchemicalAttribute.class);
        for(int slot=0;slot<AssetLease.SLOTS;slot++){
            if(modes[slot]==Mode.OFF)continue;
            try{
                var lease=new AssetLease(player,slot);var asset=ledger.owned(equipment.get(slot).orElseThrow(),lease);
                double access=spiritweb.strength(Art.FERUCHEMY,asset.metal());
                UUID key=key(asset,spiritweb,keys);
                if(access<=0||key==null){stop(slot);continue;}
                int previous=units.getOrDefault(asset.attribute(),0);
                if(modes[slot]==Mode.STORE){
                    if(previous>0){stop(slot);continue;}
                    int amount=(int)Math.min(Math.min(rates[slot]*250,750+previous),asset.capacity()-asset.charge());
                    if(amount<=0){stop(slot);continue;}
                    ledger.change(asset.token(),lease,key,amount);
                    units.put(asset.attribute(),previous-amount);
                }else{
                    if(previous<0){stop(slot);continue;}
                    int limit=(int)Math.floor(4000*Math.min(1,access));
                    int requested=Math.min(new int[]{250,500,1000,2000,4000}[rates[slot]-1],Math.max(0,limit-previous));
                    int lo=0,hi=requested;
                    while(lo<hi){
                        int mid=(lo+hi+1)/2;
                        if(tappingCost(previous+mid)-tappingCost(previous)<=asset.charge())lo=mid;else hi=mid-1;
                    }
                    if(lo==0){stop(slot);continue;}
                    long cost=tappingCost(previous+lo)-tappingCost(previous);
                    ledger.change(asset.token(),lease,key,-cost);
                    units.put(asset.attribute(),previous+lo);
                }
            }catch(IllegalArgumentException|NoSuchElementException|ArithmeticException e){stop(slot);}
        }
        var result=new EnumMap<FeruchemicalAttribute,Double>(FeruchemicalAttribute.class);
        units.forEach((attribute,value)->result.put(attribute,value/1000.0));
        return Map.copyOf(result);
    }
    private static UUID key(MetalmindAsset asset,Spiritweb spiritweb,java.util.function.Function<Metal,Set<UUID>> keys){
        if(asset.accessible(spiritweb.identity()))return spiritweb.identity();
        return asset.identity().filter(keys.apply(asset.metal())::contains).orElse(null);
    }
}
