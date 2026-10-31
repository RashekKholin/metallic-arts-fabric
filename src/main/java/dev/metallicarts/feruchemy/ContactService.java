package dev.metallicarts.feruchemy;

import dev.metallicarts.domain.*;
import dev.metallicarts.economy.*;
import dev.metallicarts.equipment.*;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Main-thread inventory transactions. Every preflight finishes before taking an actual item. */
public final class ContactService {
    public static ContactEquipment equipment(ServerPlayer player){return player.getAttachedOrCreate(ModRegistries.CONTACT_EQUIPMENT);}
    public static List<Optional<AssetToken>> tokens(ContactEquipment equipment){
        return equipment.items().stream().map(stack->Optional.ofNullable(stack.get(ModRegistries.METALMIND)).map(MetalmindData::token)).toList();
    }
    public static void equip(ServerPlayer player,int slot,int sense){
        var equipment=equipment(player);
        if(slot<0||slot>=12||sense<0||sense>=5||!equipment.item(slot).isEmpty())throw new IllegalArgumentException("Choose an empty contact slot");
        var held=player.getMainHandItem();
        if(held.has(ModRegistries.SPIKE))throw new IllegalArgumentException("Hemalurgic charge cannot be repurposed as a metalmind");
        var form=EconomyContent.form(held.getItem());
        if(held.isEmpty()||held.getCount()!=1||form==null||!List.of("ring","bracelet","bracer","earring").contains(form.kind()))
            throw new IllegalArgumentException("Hold one ring, bracelet, bracer or earring");
        var ledger=MetalmindLedger.get(player.level().getServer());var lease=new AssetLease(player.getUUID(),slot);
        if(ledger.occupied(lease))throw new IllegalArgumentException("Contact slot has an existing world lease");
        MetalmindAsset asset;
        var reference=held.get(ModRegistries.METALMIND);
        if(reference!=null){
            asset=ledger.resolve(reference.token());
            if(!asset.metal().id().equals(form.material())||asset.lease().isPresent())throw new IllegalArgumentException("Invalid or already equipped metalmind");
        }else{
            var material=Materials.metal(form.material()).orElseThrow(()->new IllegalArgumentException("Unsupported Feruchemical material"));
            var attribute=material==Metal.TIN?new FeruchemicalAttribute[]{FeruchemicalAttribute.SIGHT,FeruchemicalAttribute.HEARING,FeruchemicalAttribute.TOUCH,FeruchemicalAttribute.SMELL,FeruchemicalAttribute.TASTE}[sense]:
                Arrays.stream(FeruchemicalAttribute.values()).filter(a->a.metal()==material).findFirst().orElseThrow(()->new IllegalArgumentException("This Feruchemical metal is not implemented"));
            var data=held.get(ModRegistries.METAL_DATA);
            if(data!=null&&(data.charge()>0||data.identity().isPresent()))throw new IllegalArgumentException("Legacy invested charge needs explicit migration");
            long mass=data==null?Math.multiplyExact((long)form.units(),Materials.PORTION_MASS):data.massMicrograms();
            var composition=held.get(EconomyContent.COMPOSITION);
            double purity=data!=null?data.purity():composition==null?1:composition.purity();
            if(purity<.000001)throw new IllegalArgumentException("Invalid material purity");
            asset=ledger.create(attribute,mass,purity,false);
        }
        var worn=ledger.claim(asset.token(),lease);
        var stack=held.copyWithCount(1);stack.set(ModRegistries.METALMIND,MetalmindData.of(worn));
        player.setAttached(ModRegistries.CONTACT_EQUIPMENT,equipment.with(slot,stack));
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
    }
    public static void unequip(ServerPlayer player,int slot){
        var equipment=equipment(player);var stack=equipment.item(slot);
        var data=stack.get(ModRegistries.METALMIND);
        if(stack.isEmpty()||data==null)throw new IllegalArgumentException("Contact slot is empty");
        int destination=-1;
        for(int i=0;i<36;i++)if(player.getInventory().getItem(i).isEmpty()){destination=i;break;}
        if(destination<0)throw new IllegalArgumentException("Make space in your inventory first");
        var ledger=MetalmindLedger.get(player.level().getServer());
        var free=ledger.release(data.token(),new AssetLease(player.getUUID(),slot));
        stack.set(ModRegistries.METALMIND,MetalmindData.of(free));
        player.setAttached(ModRegistries.CONTACT_EQUIPMENT,equipment.with(slot,ItemStack.EMPTY));
        player.getInventory().setItem(destination,stack);
    }
    public static ContactEquipment preview(ServerPlayer player){
        var equipment=equipment(player);var ledger=MetalmindLedger.get(player.level().getServer());
        var items=equipment.items().stream().map(ItemStack::copy).toList();
        for(int i=0;i<12;i++){
            var stack=items.get(i);var data=stack.get(ModRegistries.METALMIND);
            if(data!=null)try{
                var asset=ledger.owned(data.token(),new AssetLease(player.getUUID(),i));
                stack.set(ModRegistries.METALMIND,MetalmindData.of(asset));
            }catch(IllegalArgumentException ignored){/* Keep the original reference for explicit recovery; never mint replacement charge. */}
        }
        return new ContactEquipment(items);
    }
    private ContactService(){}
}
