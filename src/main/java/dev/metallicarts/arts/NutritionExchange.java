package dev.metallicarts.arts;
import dev.metallicarts.registry.ModRegistries;
import net.minecraft.server.level.ServerPlayer;
/** A visible food-point exchange with fractional escrow; no rounding creates food or charge. */
public final class NutritionExchange {
 public static final int SCALE=20;
 public static int storeLimit(ServerPlayer p,BodyState body,int units){long available=body.nutrition()+Math.max(0,p.getFoodData().getFoodLevel()-1)*BodyState.FOOD_POINT+(long)Math.floor(p.getFoodData().getSaturationLevel()*BodyState.FOOD_POINT);return (int)Math.min(units,available/SCALE);}
 public static BodyState store(ServerPlayer p,BodyState body,int units){long cost=(long)units*SCALE,bank=body.nutrition();long used=Math.min(cost,bank);bank-=used;cost-=used;while(cost>0&&p.getFoodData().getFoodLevel()>1){p.getFoodData().setFoodLevel(p.getFoodData().getFoodLevel()-1);bank+=BodyState.FOOD_POINT;used=Math.min(cost,bank);bank-=used;cost-=used;}if(cost>0){float saturation=p.getFoodData().getSaturationLevel();long take=Math.min(cost,(long)Math.floor(saturation*BodyState.FOOD_POINT));p.getFoodData().setSaturation(Math.max(0,saturation-take/(float)BodyState.FOOD_POINT));cost-=take;}if(cost!=0)throw new IllegalStateException("Nutrition payment exceeded preflight");return body.nutrition(bank);}
 public static int tapLimit(BodyState body,int units){return (int)Math.min(units,(BodyState.MAX_NUTRITION-body.nutrition())/SCALE);}
 public static BodyState tap(BodyState body,int units){return body.nutrition(body.nutrition()+(long)units*SCALE);}
 private NutritionExchange(){}
}
