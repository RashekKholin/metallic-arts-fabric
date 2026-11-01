package dev.metallicarts.domain;
import dev.metallicarts.arts.SurvivalProgression;
import dev.metallicarts.config.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class SurvivalPolicyTest {
 @Test void hostPolicyRejectsWrongTypesAndFractionalIntegerCosts(){assertThrows(RuntimeException.class,()->AdvancedConfig.parse("{\"compounding\":{\"multiplier\":2.5}}"));assertThrows(RuntimeException.class,()->AdvancedConfig.parse("{\"constructs\":{\"exceptionalExtraction\":\"true\"}}"));assertThrows(RuntimeException.class,()->ProgressionConfig.parse("{\"discoveryLevels\":2.5}"));assertThrows(RuntimeException.class,()->ProgressionConfig.parse("{\"enabled\":\"false\"}"));}
 @Test void missingNewSettingsDefaultWithoutChangingUnits(){assertEquals(AdvancedConfig.DEFAULT,AdvancedConfig.parse("{}"));assertEquals(ProgressionConfig.DEFAULT,ProgressionConfig.parse("{}"));assertEquals(10,AdvancedConfig.DEFAULT.compounding().multiplier());}
 @Test void probabilityBoundariesAreDeterministicAndValidated(){assertThrows(IllegalArgumentException.class,()->new ProgressionConfig(1,true,Double.NaN,0,0,5,1000,false,true));var none=new ProgressionConfig(1,true,0,0,0,5,1000,false,true);var all=new ProgressionConfig(1,true,1,1,1,5,1000,false,true);for(int i=0;i<100;i++){var id=new UUID(i,42);var n=SurvivalProgression.allocation(id,12,none);var a=SurvivalProgression.allocation(id,12,all);assertTrue(n.allomancy().isEmpty()&&n.feruchemy().isEmpty());assertTrue(a.allomancy().isPresent()&&a.feruchemy().isPresent());assertEquals(a,SurvivalProgression.allocation(id,12,all));}}
}
