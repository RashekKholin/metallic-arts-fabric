package dev.metallicarts.domain;

import com.mojang.serialization.JsonOps;
import dev.metallicarts.equipment.*;
import dev.metallicarts.feruchemy.*;
import dev.metallicarts.hemalurgy.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SpikeLedgerTest {
    private SpikeAsset create(SpikeLedger ledger){return ledger.create(Metal.STEEL,new StolenPower(StolenPower.Kind.ALLOMANCY,Metal.IRON),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),1,0);}
    @Test void materialMatrixSeparatesAllomancyFeruchemyAndInnateTypes(){
        var used=EnumSet.noneOf(Metal.class);
        for(var p:StolenPower.choices()){
            long count=Arrays.stream(Metal.values()).filter(p::compatible).peek(used::add).count();assertEquals(1,count,p.label());
        }
        assertEquals(16,used.size());
        assertFalse(new StolenPower(StolenPower.Kind.FERUCHEMY,Metal.IRON).compatible(Metal.STEEL));
        assertTrue(new StolenPower(StolenPower.Kind.FERUCHEMY,Metal.IRON).compatible(Metal.PEWTER));
    }
    @Test void copiedAndTransferredReferencesCannotOwnTwoAnatomicalSlots(){
        var ledger=new SpikeLedger();var a=create(ledger);var lease=new AssetLease(UUID.randomUUID(),0);var copy=a.token();
        var worn=ledger.claim(a.token(),lease,100);
        assertThrows(IllegalArgumentException.class,()->ledger.claim(copy,new AssetLease(UUID.randomUUID(),1),100));
        assertThrows(IllegalArgumentException.class,()->ledger.release(worn.token(),new AssetLease(UUID.randomUUID(),0),100));
        var free=ledger.release(worn.token(),lease,200);
        assertThrows(IllegalArgumentException.class,()->ledger.resolve(worn.token()));
        assertEquals(worn.potency(),free.potency());assertTrue(free.lease().isEmpty());
    }
    @Test void decayIsMonotoneBloodSlowsItAndImplantationStopsExposure(){
        var ledger=new SpikeLedger();var initial=create(ledger);var exposed=initial.advance(24000);
        var preserved=initial.preserved(true).advance(24000);
        assertTrue(exposed.potency()<preserved.potency());assertTrue(preserved.potency()<initial.potency());
        var worn=ledger.claim(initial.token(),new AssetLease(UUID.randomUUID(),0),0);
        assertEquals(worn.potency(),worn.advance(1_000_000).potency());
        assertEquals(exposed.potency(),exposed.advance(1).potency());
        assertTrue(initial.advance(Long.MAX_VALUE).potency()>0);assertTrue(initial.advance(Long.MAX_VALUE).potency()<initial.potency());
    }
    @Test void provenanceTombstonesLeasesDecayAndDrainSurviveCodec(){
        var ledger=new SpikeLedger();var a=create(ledger);var lease=new AssetLease(UUID.randomUUID(),4);
        a=ledger.claim(a.token(),lease,400);ledger.drainSource(a.token().instance());
        var json=SpikeLedger.CODEC.encodeStart(JsonOps.INSTANCE,ledger).getOrThrow();var loaded=SpikeLedger.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();
        assertEquals(ledger.all(),loaded.all());assertTrue(loaded.extracted(a.donorSource()));assertFalse(loaded.canCreate(a.donorSource()));
        assertEquals(0,loaded.owned(a.token(),lease).potency());
        var drained=a.token();assertThrows(IllegalArgumentException.class,()->loaded.claim(drained,new AssetLease(UUID.randomUUID(),0),500));
    }
    @Test void duplicateDonorSourceCannotCreateAnotherSpike(){
        var ledger=new SpikeLedger();var a=create(ledger);
        assertThrows(IllegalArgumentException.class,()->ledger.create(a.metal(),a.power(),UUID.randomUUID(),UUID.randomUUID(),a.donorSource(),1,0));
        assertEquals(1,ledger.all().size());
    }
    @Test void previewNeverSerializesPrivateDonorIdentity(){
        var a=create(new SpikeLedger());String preview=SpikeData.CODEC.encodeStart(JsonOps.INSTANCE,SpikeData.of(a)).getOrThrow().toString();
        assertFalse(preview.contains("donor"));assertFalse(preview.contains(a.donorIdentity().toString()));
        assertTrue(preview.contains("potency"));
    }
    @Test void donorKeyMustBeReauthorizedEveryFeruchemicalTick(){
        var ledger=new MetalmindLedger();var donor=UUID.randomUUID();var player=UUID.randomUUID();var lease=new AssetLease(player,0);
        var asset=ledger.claim(ledger.create(FeruchemicalAttribute.WEIGHT,100000,1,false).token(),lease);ledger.change(asset.token(),lease,donor,1000);
        UUID grant=UUID.randomUUID();var state=new Spiritweb(1,UUID.randomUUID(),List.of(new PowerGrant(grant,Art.FERUCHEMY,Metal.IRON,PowerGrant.Origin.HEMALURGIC,grant,.9)),Map.of());
        var equipment=new ArrayList<Optional<AssetToken>>(Collections.nCopies(12,Optional.empty()));equipment.set(0,Optional.of(asset.token()));
        var engine=new FeruchemicalEngine();assertFalse(engine.request(0,FeruchemicalEngine.Mode.TAP,1,equipment,ledger,player,state));
        assertTrue(engine.request(0,FeruchemicalEngine.Mode.TAP,1,equipment,ledger,player,state,m->Set.of(donor)));
        assertEquals(.25,engine.step(equipment,ledger,player,state,m->Set.of(donor)).get(FeruchemicalAttribute.WEIGHT));
        assertTrue(engine.step(equipment,ledger,player,state).isEmpty());assertEquals(FeruchemicalEngine.Mode.OFF,engine.mode(0));
        assertEquals(750,ledger.resolve(asset.token()).charge());
    }
    @Test void distinctAllomanticSpikesAddButCopiedSourcesDoNotMultiply(){
        UUID a=UUID.randomUUID(),b=UUID.randomUUID(),identity=UUID.randomUUID();
        var grants=List.of(new PowerGrant(UUID.randomUUID(),Art.ALLOMANCY,Metal.STEEL,PowerGrant.Origin.INNATE,identity,1),
            new PowerGrant(UUID.randomUUID(),Art.ALLOMANCY,Metal.STEEL,PowerGrant.Origin.HEMALURGIC,a,.5),
            new PowerGrant(UUID.randomUUID(),Art.ALLOMANCY,Metal.STEEL,PowerGrant.Origin.HEMALURGIC,a,.5),
            new PowerGrant(UUID.randomUUID(),Art.ALLOMANCY,Metal.STEEL,PowerGrant.Origin.HEMALURGIC,b,.75));
        var state=new Spiritweb(1,identity,grants,Map.of());
        assertEquals(2.25,state.strength(Art.ALLOMANCY,Metal.STEEL));assertEquals(1.75,state.withoutSource(a).strength(Art.ALLOMANCY,Metal.STEEL));
    }
}
