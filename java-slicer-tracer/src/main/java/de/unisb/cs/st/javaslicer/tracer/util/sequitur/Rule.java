package de.unisb.cs.st.javaslicer.tracer.util.sequitur;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Rule<T> {

    protected static class Dummy<T> extends Symbol<T> {

        private final Rule<T> rule;

        public Dummy(final Rule<T> rule) {
            this.rule = rule;
            this.next = this;
            this.prev = this;
        }

        @Override
        protected boolean singleEquals(final Symbol<?> obj) {
            // this method should not be called
            assert false;
            return false;
        }

        @Override
        protected int singleHashcode() {
            // this method should not be called
            assert false;
            return 0;
        }

        public Rule<T> getRule() {
            return this.rule;
        }

        @Override
        public boolean meltDigram() {
            return false;
        }
    }

    protected final Dummy<T> dummy;
    private int useCount;

    // TODO remove this (only needed for easier debugging)
    private static long nextRuleNr = 0;
    protected final long ruleNr = nextRuleNr++;

    public Rule() {
        this(true);
    }

    public Rule(final boolean mayBeReused) {
        this.useCount = mayBeReused ? 0 : -1;
        this.dummy = new Dummy<T>(this);
    }

    public Rule(final Symbol<T> first, final Symbol<T> second) {
        this();
        this.dummy.next = first;
        first.next = second;
        second.next = this.dummy;
        this.dummy.prev = second;
        second.prev = first;
        first.prev = this.dummy;
    }

    public void append(final Symbol<T> newSymbol, final Grammar<T> grammar) {
        this.dummy.insertBefore(newSymbol);
        grammar.checkDigram(newSymbol.prev);
    }

    public boolean mayBeReused() {
        return this.useCount >= 0;
    }

    public void incUseCount() {
        assert this.useCount >= 0;
        ++this.useCount;
    }

    public void decUseCount() {
        assert this.useCount >= 0;
        --this.useCount;
    }

    public int getUseCount() {
        assert this.useCount >= 0;
        return this.useCount;
    }

    public void writeOut(final ObjectOutputStream objOut, final Grammar<T> grammar) {
        // TODO Auto-generated method stub

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("R").append(this.ruleNr).append(" --> ");
        if (this.dummy.next != this.dummy) {
            sb.append(this.dummy.next);
            for (Symbol<T> s = this.dummy.next.next; s != this.dummy; s = s.next)
                sb.append(" ").append(s);
        }
        return sb.toString();
    }

    public Set<Rule<T>> getUsedRules() {
        final Set<Rule<T>> rules = new HashSet<Rule<T>>();
        addUsedRules(rules);
        return rules;
    }

    public void addUsedRules(final Set<Rule<T>> ruleSet) {
        final List<Rule<T>> ruleQueue = new ArrayList<Rule<T>>();
        ruleQueue.add(this);


        while (!ruleQueue.isEmpty()) {
            final Rule<T> r = ruleQueue.remove(ruleQueue.size()-1);
            for (Symbol<T> s = r.dummy.next; s != r.dummy; s = s.next)
                if (s instanceof NonTerminal<?>) {
                    final Rule<T> newR = ((NonTerminal<T>)s).getRule();
                    if (ruleSet.add(newR))
                        ruleQueue.add(newR);
                }
        }
    }

    public static <T> Rule<T> readFrom(final ObjectInputStream objIn, final Grammar<T> grammar,
            final Class<? extends T> checkInstance) {
        // TODO
        return null;
    }

}
