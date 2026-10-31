package dev.metallicarts.allomancy;

import dev.metallicarts.domain.*;
import java.util.*;

/** Ordinary fuel excludes the invested subset. All calculations use micrograms. */
public final class ReserveLedger {
    public static long investedMass(Spiritweb state,Metal metal) {
        return state.investedReserves().stream().filter(d->d.metal()==metal).mapToLong(MetalItemData::massMicrograms).reduce(0,Math::addExact);
    }
    public static boolean valid(Spiritweb state) {
        if(state.grants().stream().map(PowerGrant::id).distinct().count()!=state.grants().size() ||
            state.investedReserves().stream().map(MetalItemData::instance).distinct().count()!=state.investedReserves().size())return false;
        try { for(Metal m:Metal.values()) if(investedMass(state,m)>state.reserves().getOrDefault(m,0L))return false; }
        catch(ArithmeticException e){return false;}
        return true;
    }
    public static long ordinary(Spiritweb state,Metal metal) {
        if(!valid(state))throw new IllegalArgumentException("Inconsistent reserve ledger");
        return state.reserves().getOrDefault(metal,0L)-investedMass(state,metal);
    }
    public static Spiritweb debit(Spiritweb state,Metal metal,long amount) {
        if(amount<0||amount>ordinary(state,metal))throw new IllegalArgumentException("Insufficient ordinary fuel");
        var reserves=new EnumMap<Metal,Long>(Metal.class);reserves.putAll(state.reserves());
        long remaining=reserves.getOrDefault(metal,0L)-amount;
        if(remaining==0)reserves.remove(metal);else reserves.put(metal,remaining);
        return new Spiritweb(state.schema(),state.identity(),state.grants(),reserves,state.investedReserves());
    }
    public static Spiritweb grant(Spiritweb state,PowerGrant grant) {
        requireValid(state);
        if(state.grants().size()>=128||state.grants().stream().anyMatch(g->g.id().equals(grant.id())))throw new IllegalArgumentException("Duplicate grant or grant limit");
        var grants=new ArrayList<>(state.grants());grants.add(grant);
        return new Spiritweb(state.schema(),state.identity(),grants,state.reserves(),state.investedReserves());
    }
    /** Adds ordinary fuel without changing the invested subset or any ownership. */
    public static Spiritweb credit(Spiritweb state,Metal metal,long amount) {
        requireValid(state);
        if(amount<=0||amount>Spiritweb.MAX_RESERVE-state.reserves().getOrDefault(metal,0L))
            throw new IllegalArgumentException("Reserve capacity exceeded");
        var reserves=new EnumMap<Metal,Long>(Metal.class);reserves.putAll(state.reserves());
        reserves.merge(metal,amount,Math::addExact);
        return new Spiritweb(state.schema(),state.identity(),state.grants(),reserves,state.investedReserves());
    }
    /** Revoke exactly one grant, retaining other grants from the same source. */
    public static Spiritweb revoke(Spiritweb state,UUID grantId) {
        requireValid(state);
        if(state.grants().stream().noneMatch(g->g.id().equals(grantId)))throw new IllegalArgumentException("Unknown grant ID");
        return new Spiritweb(state.schema(),state.identity(),state.grants().stream().filter(g->!g.id().equals(grantId)).toList(),state.reserves(),state.investedReserves());
    }
    private static void requireValid(Spiritweb state) {
        if(!valid(state))throw new IllegalArgumentException("Inconsistent spiritweb ledger");
    }
    private ReserveLedger(){}
}
