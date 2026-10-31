package dev.metallicarts.rules;

import com.google.gson.Gson;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Provenance is bundled reviewable data, never an implicit claim that a mechanic exists. */
public final class RuleRegistry {
    public enum Status { AUTHOR_STATEMENT, INTERPRETATION, GAMEPLAY_POLICY, DEFERRED }
    public record Rule(String id, Status status, String statement, List<String> sources, String implementation) {
        public Rule { sources = List.copyOf(sources); }
    }
    public static Map<String, Rule> load() {
        try (InputStream stream = RuleRegistry.class.getResourceAsStream("/metallicarts/rules.json")) {
            if (stream == null) throw new IllegalStateException("Missing rule provenance");
            Rule[] rules = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), Rule[].class);
            Map<String, Rule> result = new LinkedHashMap<>();
            for (Rule rule : rules) {
                if (rule.id() == null || !rule.id().matches("[a-z_]+") || rule.status() == null || rule.statement() == null || rule.implementation() == null)
                    throw new IllegalStateException("Invalid rule entry");
                if (rule.status() == Status.AUTHOR_STATEMENT && rule.sources().isEmpty()) throw new IllegalStateException("Unsourced canon claim");
                if (result.putIfAbsent(rule.id(), rule) != null) throw new IllegalStateException("Duplicate rule " + rule.id());
            }
            return Collections.unmodifiableMap(result);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private RuleRegistry() {}
}
