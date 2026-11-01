package dev.metallicarts.arts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.domain.DomainCodecs;

/** Integer body-resource escrow. One food point=400,000 nutrition units; one air point=1,000 air units. */
public record BodyState(long fatigue,long temperature,long nutrition,long air,long updated,long rescueUntil){
    public static final long MAX_FATIGUE=96000000,MIN_TEMP=20000000,MAX_TEMP=45000000,FOOD_POINT=400000,MAX_NUTRITION=8000000;
    public static final BodyState DEFAULT=new BodyState(0,36500000,0,0,0);
    public static final Codec<BodyState> CODEC=RecordCodecBuilder.create(i->i.group(
        DomainCodecs.longRange(0,MAX_FATIGUE).fieldOf("fatigue").forGetter(BodyState::fatigue),DomainCodecs.longRange(MIN_TEMP,MAX_TEMP).fieldOf("temperature").forGetter(BodyState::temperature),
        DomainCodecs.longRange(0,MAX_NUTRITION).fieldOf("nutrition").forGetter(BodyState::nutrition),DomainCodecs.longRange(0,1000000).fieldOf("air").forGetter(BodyState::air),
        DomainCodecs.longRange(0,Long.MAX_VALUE).fieldOf("updated").forGetter(BodyState::updated),DomainCodecs.longRange(0,Long.MAX_VALUE).optionalFieldOf("rescue_until",0L).forGetter(BodyState::rescueUntil)
    ).apply(i,BodyState::new));
    public BodyState{if(fatigue<0||fatigue>MAX_FATIGUE||temperature<MIN_TEMP||temperature>MAX_TEMP||nutrition<0||nutrition>MAX_NUTRITION||air<0||air>1000000||updated<0||rescueUntil<0)throw new IllegalArgumentException("Invalid body escrow");}
    public BodyState(long fatigue,long temperature,long nutrition,long air,long updated){this(fatigue,temperature,nutrition,air,updated,0);}
    public BodyState fatigue(long value){return new BodyState(value,temperature,nutrition,air,updated,rescueUntil);}
    public BodyState temperature(long value){return new BodyState(fatigue,value,nutrition,air,updated,rescueUntil);}
    public BodyState nutrition(long value){return new BodyState(fatigue,temperature,value,air,updated,rescueUntil);}
    public BodyState air(long value){return new BodyState(fatigue,temperature,nutrition,value,updated,rescueUntil);}
    public BodyState updated(long value){return new BodyState(fatigue,temperature,nutrition,air,value,rescueUntil);}
    public BodyState rescueUntil(long value){return new BodyState(fatigue,temperature,nutrition,air,updated,value);}
}
