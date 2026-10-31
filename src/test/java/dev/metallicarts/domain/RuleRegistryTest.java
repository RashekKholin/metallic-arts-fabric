package dev.metallicarts.domain;

import dev.metallicarts.rules.RuleRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RuleRegistryTest {
    @Test void provenanceLoadsWithUniqueIdsAndExplicitInterpretations() {
        var rules = RuleRegistry.load();
        assertEquals(16, rules.size());
        assertEquals(RuleRegistry.Status.INTERPRETATION, rules.get("duplicate_power_resolution").status());
        assertTrue(rules.values().stream().filter(r -> r.status() == RuleRegistry.Status.AUTHOR_STATEMENT).allMatch(r -> !r.sources().isEmpty()));
    }
}
