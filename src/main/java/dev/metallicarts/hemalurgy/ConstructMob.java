package dev.metallicarts.hemalurgy;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
/** Real server entities, not fake players. Public body shape is synchronized; private ledgers are not. */
public final class ConstructMob extends Monster {
 private static final EntityDataAccessor<Byte> BODY=SynchedEntityData.defineId(ConstructMob.class,EntityDataSerializers.BYTE);
 private int lastArtsTick=-1;
 public ConstructMob(EntityType<? extends ConstructMob> type,Level level){super(type,level);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(BODY,(byte)0);}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(2,new MeleeAttackGoal(this,1,true));goalSelector.addGoal(6,new WaterAvoidingRandomStrollGoal(this,.8));goalSelector.addGoal(7,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(8,new RandomLookAroundGoal(this));targetSelector.addGoal(1,new HurtByTargetGoal(this));if(getType()!=NpcConstructs.KANDRA)targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));}
 @Override public void tick(){super.tick();if(!level().isClientSide()&&level().getServer().getTickCount()!=lastArtsTick){lastArtsTick=level().getServer().getTickCount();NpcConstructs.tick(this);}}
 public void publicBody(int shape,boolean collapsed){entityData.set(BODY,(byte)(shape|(collapsed?8:0)));}
 public int bodyShape(){return entityData.get(BODY)&3;}
 public boolean collapsed(){return (entityData.get(BODY)&8)!=0;}
}
