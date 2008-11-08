package de.unisb.cs.st.javaslicer.tracer.util.sequitur.input;

import java.io.IOException;
import java.io.ObjectInputStream;

import de.unisb.cs.st.javaslicer.tracer.util.LongArrayList;

// package-private
class Grammar<T> {

    private final LongArrayList<Rule<T>> rules;

    protected Grammar(final LongArrayList<Rule<T>> rules) {
        this.rules = rules;
    }

    public static <T> Grammar<T> readFrom(final ObjectInputStream objIn, final ObjectReader<? extends T> objectReader,
            final Class<? extends T> checkInstance) throws IOException, ClassNotFoundException {
        final LongArrayList<Rule<T>> rules = Rule.readAll(objIn, objectReader, checkInstance);
        assert rules.size() > 0;
        final Grammar<T> grammar = new Grammar<T>(rules);
        for (final Rule<T> rule: rules)
            rule.substituteRealRules(grammar);
        boolean ready = false;
        while (!ready) {
            ready = true;
            for (final Rule<T> rule: rules)
                if (!rule.computeLength())
                    ready = false;
        }
        return grammar;
    }

    public Rule<T> getRule(final long ruleNr) {
        if (ruleNr < 0 || ruleNr >= this.rules.longSize())
            return null;
        return this.rules.get(ruleNr);
    }

}
