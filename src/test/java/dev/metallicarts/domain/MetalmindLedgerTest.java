package dev.metallicarts.domain;

import com.mojang.serialization.JsonOps;
import dev.metallicarts.equipment.*;
import dev.metallicarts.feruchemy.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MetalmindLedgerTest {
    @Test void copiedTokensCannotEquipOrSpendTheSameChargeTwice(){
        var ledger=new MetalmindLedger();var asset=ledger.create(FeruchemicalAttribute.SPEED,100000,1,false);
        var copy=asset.token();var lease=new AssetLease(UUID.randomUUID(),0);
        var worn=ledger.claim(copy,lease);
        assertThrows(IllegalArgumentException.class,()->ledger.claim(copy,new AssetLease(UUID.randomUUID(),0)));
        var identity=UUID.randomUUID();
        ledger.change(worn.token(),lease,identity,1000);
        assertThrows(IllegalArgumentException.class,()->ledger.change(worn.token(),new AssetLease(UUID.randomUUID(),0),identity,-1000));
        var free=ledger.release(worn.token(),lease);
        assertEquals(1000,free.charge());
        assertThrows(IllegalArgumentException.class,()->ledger.resolve(worn.token()));
        assertThrows(IllegalArgumentException.class,()->ledger.resolve(copy));
        assertEquals(0,ledger.claim(free.token(),lease).lease().get().slot());
    }
    @Test void keyedAndUnkeyedDoNotGrantPowersOrAllowForeignDeposits(){
        var ledger=new MetalmindLedger();var identity=UUID.randomUUID();var lease=new AssetLease(UUID.randomUUID(),0);
        var worn=ledger.claim(ledger.create(FeruchemicalAttribute.WEIGHT,10000,1,false).token(),lease);
        ledger.change(worn.token(),lease,identity,500);
        assertThrows(IllegalArgumentException.class,()->ledger.change(worn.token(),lease,UUID.randomUUID(),-1));
        assertThrows(IllegalArgumentException.class,()->ledger.change(worn.token(),lease,UUID.randomUUID(),1));
        assertEquals(500,ledger.resolve(worn.token()).charge());
        var otherLease=new AssetLease(UUID.randomUUID(),1);
        var unkeyed=ledger.claim(ledger.create(FeruchemicalAttribute.SIGHT,10000,1,true).token(),otherLease);
        ledger.change(unkeyed.token(),otherLease,identity,500);
        assertEquals(250,ledger.change(unkeyed.token(),otherLease,UUID.randomUUID(),-250).charge());
    }
    @Test void capacityDepletionAndInvalidTransfersAreAtomic(){
        var ledger=new MetalmindLedger();var lease=new AssetLease(UUID.randomUUID(),2);var identity=UUID.randomUUID();
        var worn=ledger.claim(ledger.create(FeruchemicalAttribute.STRENGTH,1000,.9,false).token(),lease);
        assertEquals(900,worn.capacity());
        assertThrows(IllegalArgumentException.class,()->ledger.change(worn.token(),lease,identity,901));
        assertEquals(0,ledger.resolve(worn.token()).charge());
        ledger.change(worn.token(),lease,identity,900);
        assertThrows(IllegalArgumentException.class,()->ledger.change(worn.token(),lease,identity,-901));
        assertEquals(900,ledger.resolve(worn.token()).charge());
        assertThrows(IllegalArgumentException.class,()->ledger.claim(ledger.create(FeruchemicalAttribute.HEARING,1000,1,false).token(),lease));
    }
    @Test void savedLedgerRetainsDistinctSensesIdentityChargeAndLeases(){
        var ledger=new MetalmindLedger();var identity=UUID.randomUUID();
        for(var attribute:List.of(FeruchemicalAttribute.SIGHT,FeruchemicalAttribute.HEARING,FeruchemicalAttribute.TOUCH,FeruchemicalAttribute.SMELL,FeruchemicalAttribute.TASTE)){
            var lease=new AssetLease(UUID.randomUUID(),0);
            var worn=ledger.claim(ledger.create(attribute,10000,1,false).token(),lease);
            ledger.change(worn.token(),lease,identity,attribute.ordinal()*100);
        }
        var json=MetalmindLedger.CODEC.encodeStart(JsonOps.INSTANCE,ledger).getOrThrow();
        var loaded=MetalmindLedger.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();
        assertEquals(ledger.all(),loaded.all());assertEquals(5,loaded.all().size());
        var token=loaded.all().getFirst().token();
        assertThrows(IllegalArgumentException.class,()->loaded.resolve(new AssetToken(token.instance(),UUID.randomUUID(),token.revision())));
    }
}
