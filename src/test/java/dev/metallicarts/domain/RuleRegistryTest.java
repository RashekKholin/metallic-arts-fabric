package dev.metallicarts.domain;

import dev.metallicarts.rules.RuleRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RuleRegistryTest {
    @Test void provenanceLoadsWithUniqueIdsAndExplicitInterpretations() {
        var rules = RuleRegistry.load();
        for(var id:java.util.List.of("temporal_simulation_policy","finite_body_storage_policy","typed_investiture_policy","unsealed_construction_policy","copper_memory_policy"))assertEquals(RuleRegistry.Status.GAMEPLAY_POLICY,rules.get(id).status());
        assertEquals(RuleRegistry.Status.INTERPRETATION, rules.get("duplicate_power_resolution").status());
        assertTrue(rules.values().stream().filter(r -> r.status() == RuleRegistry.Status.AUTHOR_STATEMENT).allMatch(r -> !r.sources().isEmpty()));
    }
}
