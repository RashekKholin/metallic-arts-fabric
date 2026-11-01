package dev.metallicarts.allomancy;

import dev.metallicarts.domain.*;
import java.util.*;

/**
 * Pure, transient accounting state. A caller must commit Step.state before applying any
 * returned powers. Supported handlers are explicit: an absent handler cannot consume fuel.
 */
public final class BurnEngine {
    public record Step(Spiritweb state,Map<Metal,Double> strengths) {
        public Step { strengths=Map.copyOf(strengths); }
    }
    private final Set<Metal> handlers;
    private final Map<Metal,BurnMode> modes=new EnumMap<>(Metal.class);
    private final Map<Metal,BurnBudget> budgets=new EnumMap<>(Metal.class);
    public BurnEngine(Set<Metal> implementedHandlers) {
        if(!AllomancyConfig.SUPPORTED.containsAll(implementedHandlers))throw new IllegalArgumentException("Unsupported handler");
        handlers=Set.copyOf(implementedHandlers);
    }
    public BurnMode mode(Metal metal) {return modes.getOrDefault(metal,BurnMode.OFF);}
    public boolean request(Spiritweb state,Metal metal,BurnMode requested) {
        return request(state,metal,requested,m->state.strength(Art.ALLOMANCY,m));
    }
    public boolean request(Spiritweb state,Metal metal,BurnMode requested,java.util.function.ToDoubleFunction<Metal> access) {
        Objects.requireNonNull(requested);
        if(requested==BurnMode.OFF){stop(metal);return true;}
        if(!handlers.contains(metal)||!ReserveLedger.valid(state)||access.applyAsDouble(metal)<=0||
            ReserveLedger.ordinary(state,metal)==0)return false;
        if(mode(metal)==BurnMode.OFF)budgets.put(metal,new BurnBudget());
        modes.put(metal,requested);return true;
    }
    public void stop(Metal metal){modes.remove(metal);budgets.remove(metal);}
    /** A burst consumes all remaining fuel; discard rounding credit without erasing this paid frame. */
    public void exhaust(Metal metal){if(mode(metal)!=BurnMode.OFF)budgets.put(metal,new BurnBudget());}
    public void stopAll(){modes.clear();budgets.clear();}
    public Step step(Spiritweb state,AllomancyConfig config) {
        Spiritweb original=state;return step(state,config,m->original.strength(Art.ALLOMANCY,m));
    }
    public Step step(Spiritweb state,AllomancyConfig config,java.util.function.ToDoubleFunction<Metal> access) {
        if(!ReserveLedger.valid(state)){stopAll();return new Step(state,Map.of());}
        var powers=new EnumMap<Metal,Double>(Metal.class);
        for(Metal metal:List.copyOf(modes.keySet())) {
            double strength=access.applyAsDouble(metal);
            // Check remaining ordinary mass even when the fractional budget has prepaid credit.
            if(strength<=0||ReserveLedger.ordinary(state,metal)==0){stop(metal);continue;}
            long debit=budgets.get(metal).charge(config.rate(metal,mode(metal)));
            if(debit>ReserveLedger.ordinary(state,metal)){stop(metal);continue;}
            state=ReserveLedger.debit(state,metal,debit);
            powers.put(metal,Math.min(4,strength)*(mode(metal)==BurnMode.FLARE?config.flareStrength():1));
        }
        return new Step(state,powers);
    }
}
