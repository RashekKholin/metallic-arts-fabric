package dev.metallicarts.hemalurgy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallicarts.domain.*;
import java.util.*;

/** Typed stolen capability. A type is not a promise that its later gameplay handler exists. */
public record StolenPower(Kind kind, Metal metal) {
    public enum Kind { ALLOMANCY, FERUCHEMY, STRENGTH, SENSES, EMOTIONAL_FORTITUDE, COGNITION, CONNECTION_IDENTITY, FORTUNE, INVESTITURE, NULLIFICATION }
    public static final Codec<StolenPower> CODEC=RecordCodecBuilder.create(i->i.group(
        DomainCodecs.enumeration(Kind.values(),Kind::name).fieldOf("kind").forGetter(StolenPower::kind),
        Metal.CODEC.fieldOf("metal").forGetter(StolenPower::metal)
    ).apply(i,StolenPower::new));
    public StolenPower { Objects.requireNonNull(kind);Objects.requireNonNull(metal); }
    public Optional<Art> art(){return kind==Kind.ALLOMANCY?Optional.of(Art.ALLOMANCY):kind==Kind.FERUCHEMY?Optional.of(Art.FERUCHEMY):Optional.empty();}
    public String label(){return art().isPresent()?kind.name().toLowerCase(Locale.ROOT)+" "+metal.id():kind.name().toLowerCase(Locale.ROOT).replace('_',' ');}
    public String implementation(){
        if(art().isPresent())return physical(metal)?"Physical handler available":"Effect planned";
        return switch(kind){case STRENGTH,SENSES,EMOTIONAL_FORTITUDE -> "Passive handler available";
            case NULLIFICATION -> "Destructive; spent after use";default -> "Typed hook; effect planned";};
    }
    public boolean compatible(Metal spike){
        if(kind==Kind.ALLOMANCY)return switch(spike){
            case STEEL -> physical(metal);
            case BRONZE -> mental(metal);
            case ELECTRUM -> spiritual(metal);
            case CADMIUM -> hybrid(metal);
            default -> false;
        };
        if(kind==Kind.FERUCHEMY)return switch(spike){
            case PEWTER -> physical(metal);
            case BRASS -> mental(metal);
            case GOLD -> hybrid(metal);
            case BENDALLOY -> spiritual(metal);
            default -> false;
        };
        return spike==switch(kind){
            case STRENGTH -> Metal.IRON; case SENSES -> Metal.TIN;
            case EMOTIONAL_FORTITUDE -> Metal.ZINC;case COGNITION -> Metal.COPPER;
            case CONNECTION_IDENTITY -> Metal.DURALUMIN;case FORTUNE -> Metal.CHROMIUM;
            case INVESTITURE -> Metal.NICROSIL;case NULLIFICATION -> Metal.ALUMINUM;
            default -> throw new IllegalStateException();
        };
    }
    private static boolean physical(Metal m){return Set.of(Metal.IRON,Metal.STEEL,Metal.TIN,Metal.PEWTER).contains(m);}
    private static boolean mental(Metal m){return Set.of(Metal.ZINC,Metal.BRASS,Metal.COPPER,Metal.BRONZE).contains(m);}
    private static boolean spiritual(Metal m){return Set.of(Metal.ALUMINUM,Metal.DURALUMIN,Metal.CHROMIUM,Metal.NICROSIL).contains(m);}
    private static boolean hybrid(Metal m){return Set.of(Metal.GOLD,Metal.ELECTRUM,Metal.CADMIUM,Metal.BENDALLOY).contains(m);}
    public static List<StolenPower> choices(){
        var result=new ArrayList<StolenPower>();
        for(var kind:List.of(Kind.ALLOMANCY,Kind.FERUCHEMY))for(var metal:Metal.values())result.add(new StolenPower(kind,metal));
        for(var kind:Kind.values())if(kind!=Kind.ALLOMANCY&&kind!=Kind.FERUCHEMY)result.add(new StolenPower(kind,Metal.IRON));
        return List.copyOf(result);
    }
}
