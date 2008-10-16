package de.unisb.cs.st.javaslicer.tracer.util.sequitur.input;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

// package-private
class Grammar<T> {

    private final Map<Long, Rule<T>> rules;

    protected Grammar(final Map<Long, Rule<T>> rules) {
        this.rules = rules;
    }

    public static <T> Grammar<T> readFrom(final ObjectInputStream objIn, final ObjectReader<? extends T> objectReader,
            final Class<? extends T> checkInstance) throws IOException, ClassNotFoundException {
        final Map<Long, Rule<T>> rules = Rule.readAll(objIn, objectReader, checkInstance);
        for (final Rule<T> rule: rules.values())
            rule.substituteRealRules(rules);
        return new Grammar<T>(rules);
    }

    public Rule<T> getRule(final long ruleNr) {
        return this.rules.get(ruleNr);
    }

}
