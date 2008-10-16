package de.unisb.cs.st.javaslicer.tracer.util.sequitur.input;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;


// package-private
class NonTerminal<T> implements Symbol<T> {

    // only needed while reading in the grammar
    private static class RuleReference<T> extends Rule<T> {

        protected final long ruleNr;

        public RuleReference(final long ruleNr) {
            super(null);
            this.ruleNr = ruleNr;
        }

    }

    private final Rule<T> rule;
    private final int count;

    public NonTerminal(final Rule<T> rule, final int count) {
        assert rule != null;
        assert count > 0;
        this.rule = rule;
        this.count = count;
    }

    public Rule<T> getRule() {
        return this.rule;
    }

    public NonTerminal<T> substituteRealRules(final Map<Long, Rule<T>> rules) {
        if (this.rule instanceof RuleReference<?>) {
            return new NonTerminal<T>(rules.get(((RuleReference<T>)this.rule).ruleNr), this.count);
        }
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('R').append(this.rule.hashCode());
        if (this.count != 1) {
            sb.append('^').append(this.count);
        }
        return sb.toString();
    }

    public static <T> NonTerminal<T> readFrom(final ObjectInputStream objIn, final boolean counted) throws IOException {
        final int count = counted ? DataInput.readInt(objIn) : 1;
        final long ruleNr = DataInput.readLong(objIn);
        return new NonTerminal<T>(new RuleReference<T>(ruleNr), count);
    }

}
