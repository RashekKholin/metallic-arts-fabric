package dev.metallicarts.domain;

import dev.metallicarts.feruchemy.*;
import dev.metallicarts.equipment.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class FeruchemicalEngineTest {
    private record Fixture(MetalmindLedger ledger,UUID player,Spiritweb state,List<Optional<AssetToken>> worn){}
    private Fixture fixture(int count){
        var ledger=new MetalmindLedger();UUID player=UUID.randomUUID(),identity=UUID.randomUUID(),grant=UUID.randomUUID();
        var state=new Spiritweb(1,identity,List.of(new PowerGrant(grant,Art.FERUCHEMY,Metal.IRON,PowerGrant.Origin.ADMIN,grant,1)),Map.of());
        var worn=new ArrayList<Optional<AssetToken>>(Collections.nCopies(12,Optional.empty()));
        for(int i=0;i<count;i++){
            var asset=ledger.claim(ledger.create(FeruchemicalAttribute.WEIGHT,1000000,1,false).token(),new AssetLease(player,i));
            worn.set(i,Optional.of(asset.token()));
        }
        return new Fixture(ledger,player,state,List.copyOf(worn));
    }
    @Test void storageCostIsCappedAcrossMultipleMetalmindsWithoutMintingCharge(){
        var f=fixture(4);var engine=new FeruchemicalEngine();
        for(int i=0;i<4;i++)assertTrue(engine.request(i,FeruchemicalEngine.Mode.STORE,3,f.worn,f.ledger,f.player,f.state));
        assertEquals(-.75,engine.step(f.worn,f.ledger,f.player,f.state).get(FeruchemicalAttribute.WEIGHT));
        assertEquals(750,f.ledger.all().stream().mapToLong(MetalmindAsset::charge).sum());
        assertEquals(FeruchemicalEngine.Mode.OFF,engine.mode(3));
    }
    @Test void splittingTapsDoesNotBypassHighRateInefficiency(){
        var f=fixture(4);var engine=new FeruchemicalEngine();
        for(int i=0;i<4;i++){
            f.ledger.change(f.worn.get(i).get(),new AssetLease(f.player,i),f.state.identity(),20000);
            assertTrue(engine.request(i,FeruchemicalEngine.Mode.TAP,3,f.worn,f.ledger,f.player,f.state));
        }
        long before=f.ledger.all().stream().mapToLong(MetalmindAsset::charge).sum();
        assertEquals(4,engine.step(f.worn,f.ledger,f.player,f.state).get(FeruchemicalAttribute.WEIGHT));
        long after=f.ledger.all().stream().mapToLong(MetalmindAsset::charge).sum();
        assertEquals(FeruchemicalEngine.tappingCost(4000),before-after);
        assertEquals(16000,before-after);
    }
    @Test void ownershipForeignIdentityAndPhysicalContactAreRequired(){
        var f=fixture(1);var engine=new FeruchemicalEngine();
        assertFalse(engine.request(0,FeruchemicalEngine.Mode.STORE,1,f.worn,f.ledger,f.player,Spiritweb.empty()));
        assertTrue(engine.request(0,FeruchemicalEngine.Mode.STORE,1,f.worn,f.ledger,f.player,f.state));
        engine.step(f.worn,f.ledger,f.player,f.state);
        var foreign=new Spiritweb(1,UUID.randomUUID(),f.state.grants(),Map.of());
        assertFalse(engine.request(0,FeruchemicalEngine.Mode.TAP,1,f.worn,f.ledger,f.player,foreign));
        f.ledger.release(f.worn.getFirst().get(),new AssetLease(f.player,0));
        assertTrue(engine.step(f.worn,f.ledger,f.player,f.state).isEmpty());
        assertEquals(FeruchemicalEngine.Mode.OFF,engine.mode(0));
    }
    @Test void contraryActionsAndDepletedChargeCannotCreateBenefits(){
        var f=fixture(2);var engine=new FeruchemicalEngine();
        assertTrue(engine.request(0,FeruchemicalEngine.Mode.STORE,1,f.worn,f.ledger,f.player,f.state));
        assertFalse(engine.request(1,FeruchemicalEngine.Mode.TAP,1,f.worn,f.ledger,f.player,f.state));
        engine.stopAll();
        assertTrue(engine.request(1,FeruchemicalEngine.Mode.TAP,5,f.worn,f.ledger,f.player,f.state));
        assertTrue(engine.step(f.worn,f.ledger,f.player,f.state).isEmpty());
        assertEquals(FeruchemicalEngine.Mode.OFF,engine.mode(1));
    }
}
