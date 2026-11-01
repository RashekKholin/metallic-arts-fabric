package dev.metallicarts.arts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
public record TraumaState(int arm,int leg,int head){
 public static final TraumaState NONE=new TraumaState(0,0,0);
 public static final Codec<TraumaState> CODEC=RecordCodecBuilder.create(i->i.group(Codec.intRange(0,3).fieldOf("arm").forGetter(TraumaState::arm),Codec.intRange(0,3).fieldOf("leg").forGetter(TraumaState::leg),Codec.intRange(0,3).fieldOf("head").forGetter(TraumaState::head)).apply(i,TraumaState::new));
 public TraumaState{if(arm<0||arm>3||leg<0||leg>3||head<0||head>3)throw new IllegalArgumentException("Invalid injury severity");}
 public int total(){return arm+leg+head;}
 public TraumaState injure(int part){return switch(Math.floorMod(part,3)){case 0->new TraumaState(Math.min(3,arm+1),leg,head);case 1->new TraumaState(arm,Math.min(3,leg+1),head);default->new TraumaState(arm,leg,Math.min(3,head+1));};}
 public TraumaState heal(){if(arm>=leg&&arm>=head&&arm>0)return new TraumaState(arm-1,leg,head);if(leg>=head&&leg>0)return new TraumaState(arm,leg-1,head);return head>0?new TraumaState(arm,leg,head-1):this;}
}
