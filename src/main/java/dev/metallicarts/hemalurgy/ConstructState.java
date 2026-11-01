package dev.metallicarts.hemalurgy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.domain.DomainCodecs;
public record ConstructState(Lineage lineage,Kind kind,Shape shape,long growthTicks,int degradedTicks,int knownShapes){
 public enum Lineage{HUMAN,MISTWRAITH}public enum Kind{HUMAN,KOLOSS,INQUISITOR,KANDRA}public enum Shape{HUMAN,WOLF,SLIME}
 public static final ConstructState HUMAN=new ConstructState(Lineage.HUMAN,Kind.HUMAN,Shape.HUMAN,0,0,1);
 public static final Codec<ConstructState> CODEC=RecordCodecBuilder.create(i->i.group(DomainCodecs.enumeration(Lineage.values(),Enum::name).fieldOf("lineage").forGetter(ConstructState::lineage),DomainCodecs.enumeration(Kind.values(),Enum::name).fieldOf("kind").forGetter(ConstructState::kind),DomainCodecs.enumeration(Shape.values(),Enum::name).fieldOf("shape").forGetter(ConstructState::shape),DomainCodecs.longRange(0,1000000000000L).fieldOf("growth_ticks").forGetter(ConstructState::growthTicks),Codec.intRange(0,24000).fieldOf("degraded_ticks").forGetter(ConstructState::degradedTicks),Codec.intRange(1,7).fieldOf("known_shapes").forGetter(ConstructState::knownShapes)).apply(i,ConstructState::new));
 public ConstructState{if(lineage==null||kind==null||shape==null||growthTicks<0||growthTicks>1000000000000L||degradedTicks<0||degradedTicks>24000||knownShapes<1||knownShapes>7||kind==Kind.KANDRA&&lineage!=Lineage.MISTWRAITH||kind==Kind.KOLOSS&&lineage!=Lineage.HUMAN||kind==Kind.INQUISITOR&&lineage!=Lineage.HUMAN)throw new IllegalArgumentException("Invalid construct body");}
}
