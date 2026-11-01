package dev.metallicarts.domain;
import dev.metallicarts.arts.FactionStanding;
import dev.metallicarts.config.WorldConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
final class ReleasePolicyTest {
 @Test void omittedWorldOptionsKeepDocumentedDefaults(){assertEquals(WorldConfig.DEFAULT,WorldConfig.parse("{}"));}
 @Test void invalidIntegerAndBooleanTypesDoNotCoerce(){assertThrows(RuntimeException.class,()->WorldConfig.parse("{\"scanBudget\":64.5}"));assertThrows(RuntimeException.class,()->WorldConfig.parse("{\"researchCamps\":\"false\"}"));}
 @Test void unsafeWorldBudgetsAreRejected(){for(String json:new String[]{"{\"scanBudget\":8193}","{\"constructChance\":1.1}","{\"godmetalBurnTicks\":0}"})assertThrows(RuntimeException.class,()->WorldConfig.parse(json));}
 @Test void factionBoundsAndIndependentScoresArePreserved(){var s=FactionStanding.empty().change("terris",100).change("terris",5).change("empire",-100).change("empire",-5);assertEquals(100,s.get("terris"));assertEquals(-100,s.get("empire"));assertEquals(0,s.get("village"));assertThrows(IllegalArgumentException.class,()->new FactionStanding(Map.of("terris",101),0));assertThrows(IllegalArgumentException.class,()->s.change("operator",100));}
}
