package dev.metallicarts.arts;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.EnumSet;
/** Temporary distrust/anger gives even an otherwise passive creature a finite melee response. */
public final class EmotionCombatGoal extends Goal {
 private final Mob mob;private int cooldown;
 public EmotionCombatGoal(Mob mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
 @Override public boolean canUse(){return EmotionService.aggressor(mob)!=null;}
 @Override public boolean requiresUpdateEveryTick(){return true;}
 @Override public void tick(){var target=EmotionService.aggressor(mob);if(target==null)return;mob.setTarget(target);mob.getLookControl().setLookAt(target,30,30);mob.getNavigation().moveTo(target,1);if(cooldown>0)cooldown--;if(cooldown==0&&mob.distanceToSqr(target)<4&&mob.hasLineOfSight(target)&&mob.level() instanceof net.minecraft.server.level.ServerLevel level){cooldown=20;if(mob.getAttribute(Attributes.ATTACK_DAMAGE)!=null)mob.doHurtTarget(level,target);else target.hurtServer(level,mob.damageSources().mobAttack(mob),2);}}
 @Override public void stop(){mob.getNavigation().stop();if(mob.getTarget()!=null&&EmotionService.aggressor(mob)==null)mob.setTarget(null);}
}
