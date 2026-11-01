package dev.metallicarts.feruchemy;

import dev.metallicarts.MetallicArts;
import dev.metallicarts.mixin.BlockDisplayAccess;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import java.util.*;

/** One actual, non-container block. Custody lives in a world entity, not in a copied display stack. */
public final class PhysicalCargo extends Display.BlockDisplay {
    public static EntityType<PhysicalCargo> TYPE;
    private static final Map<Block,Double> MASS=Map.of(Blocks.COBBLESTONE,30.0,Blocks.COPPER_BLOCK,100.0,
        Blocks.IRON_BLOCK,120.0,Blocks.GOLD_BLOCK,160.0,Blocks.ANVIL,150.0,Blocks.CHIPPED_ANVIL,150.0,Blocks.DAMAGED_ANVIL,150.0);
    private UUID owner;private boolean restored;
    public PhysicalCargo(EntityType<?> type,Level level){super(type,level);}
    public static void initialize(){
        var key=ResourceKey.create(Registries.ENTITY_TYPE,MetallicArts.id("physical_cargo"));
        TYPE=Registry.register(BuiltInRegistries.ENTITY_TYPE,key,EntityType.Builder.<PhysicalCargo>of(PhysicalCargo::new,MobCategory.MISC)
            .sized(1,1).clientTrackingRange(8).updateInterval(1).build(key));
    }
    public ServerPlayer carrier(){
        return owner!=null&&level() instanceof ServerLevel server?server.getServer().getPlayerList().getPlayer(owner):null;
    }
    public BlockStateView view(){
        var state=((BlockDisplayAccess)(Object)this).metallicarts$getBlockState();
        return new BlockStateView(state.getBlock(),MASS.getOrDefault(state.getBlock(),0.0));
    }
    public record BlockStateView(Block block,double mass){}
    public boolean metallic(){return view().block()!=Blocks.COBBLESTONE&&view().mass()>0;}
    public static double carriedMass(ServerPlayer player){
        UUID id=player.getAttached(ModRegistries.PHYSICAL_CARGO);
        return id!=null&&player.level().getEntity(id) instanceof PhysicalCargo cargo&&player.getUUID().equals(cargo.owner)?cargo.view().mass():0;
    }
    public static boolean toggle(ServerPlayer player){
        if(!player.isAlive()||player.isSpectator()||player.gameMode()!=GameType.SURVIVAL&&player.gameMode()!=GameType.CREATIVE)return false;
        UUID held=player.getAttached(ModRegistries.PHYSICAL_CARGO);
        if(held!=null){
            if(player.level().getEntity(held) instanceof PhysicalCargo cargo&&player.getUUID().equals(cargo.owner)){cargo.drop();return true;}
            player.removeAttached(ModRegistries.PHYSICAL_CARGO);return false;
        }
        var hit=player.pick(3,1,false);
        if(!(hit instanceof BlockHitResult block)||hit.getType()!=HitResult.Type.BLOCK)return false;
        var level=player.level();var pos=block.getBlockPos();var state=level.getBlockState(pos);
        double mass=MASS.getOrDefault(state.getBlock(),Double.POSITIVE_INFINITY);
        if(mass>FeruchemyRuntime.carryCapacity(player)||!level.hasChunkAt(pos)||level.getBlockEntity(pos)!=null||!player.mayInteract(level,pos)||
            !dev.metallicarts.api.ArtsProtection.BLOCK_EVENT.invoker().allow(player,pos,dev.metallicarts.api.ArtsProtection.Action.MOVE_BLOCK)||
            !PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level,player,pos,state,null)||!level.getBlockState(pos).equals(state))return false;
        var cargo=new PhysicalCargo(TYPE,level);cargo.owner=player.getUUID();
        ((BlockDisplayAccess)(Object)cargo).metallicarts$setBlockState(state);cargo.setPos(player.getX()-.5,player.getBoundingBox().maxY+.25,player.getZ()-.5);
        if(!level.addFreshEntity(cargo))return false;
        if(!level.removeBlock(pos,false)){cargo.discard();return false;}
        player.setAttached(ModRegistries.PHYSICAL_CARGO,cargo.getUUID());return true;
    }
    public void drop(){
        if(isRemoved()||!(level() instanceof ServerLevel server))return;
        var view=view();
        if(view.mass()>0)spawnAtLocation(server,new ItemStack(view.block().asItem()));
        var player=carrier();
        if(player!=null&&getUUID().equals(player.getAttached(ModRegistries.PHYSICAL_CARGO)))player.removeAttached(ModRegistries.PHYSICAL_CARGO);
        discard();
    }
    @Override public void tick(){
        super.tick();
        if(!(level() instanceof ServerLevel))return;
        var player=carrier();
        if(restored||player==null||!player.isAlive()||player.isSpectator()||player.level()!=level()||
            !getUUID().equals(player.getAttached(ModRegistries.PHYSICAL_CARGO))||view().mass()>FeruchemyRuntime.carryCapacity(player)){drop();return;}
        setPos(player.getX()-.5,player.getBoundingBox().maxY+.25,player.getZ()-.5);
        if(tickCount%20==0)player.causeFoodExhaustion(.05f);
    }
    @Override protected void addAdditionalSaveData(ValueOutput output){
        super.addAdditionalSaveData(output);if(owner!=null)output.store("metallicarts_carrier",UUIDUtil.CODEC,owner);
    }
    @Override protected void readAdditionalSaveData(ValueInput input){
        super.readAdditionalSaveData(input);owner=input.read("metallicarts_carrier",UUIDUtil.CODEC).orElse(null);restored=true;
    }
}
