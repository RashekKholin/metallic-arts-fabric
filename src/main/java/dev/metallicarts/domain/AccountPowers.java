package dev.metallicarts.domain;

import java.util.Map;
import java.util.UUID;

/** Public, server-configurable account perk. Never writes grants, reserves, keys or operator permissions. */
public final class AccountPowers {
    // Resolved through Mojang's official profile API on 2026-10-31. Names are labels, not authorization.
    public static final Map<UUID,String> ACCOUNTS=Map.of(
        UUID.fromString("18a05fe3-2c04-4639-9ded-18bbc18e8ba6"),"RashekKholin",
        UUID.fromString("e889f7e1-3316-484d-a1e5-cab8b4d39d2b"),"The17thShard");
    public static boolean enabled(UUID account,boolean policy){return policy&&ACCOUNTS.containsKey(account);}
    public static double strength(UUID account,boolean policy,Spiritweb state,Art art,Metal metal){
        double actual=state.strength(art,metal);
        if(!enabled(account,policy))return actual;
        if(art==Art.FERUCHEMY)return Math.max(1,actual);
        double base=state.grants().stream().filter(g->g.art()==art&&g.metal()==metal&&g.origin()!=PowerGrant.Origin.HEMALURGIC).mapToDouble(PowerGrant::strength).max().orElse(0);
        return Math.min(1000,actual+Math.max(0,1-base));
    }
    private AccountPowers(){}
}
