package de.unisb.cs.st.javaslicer.tracer.util.sequitur.input;

import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import de.unisb.cs.st.javaslicer.tracer.util.sequitur.input.Rule.Dummy;

public class Grammar<T> {

    private final Map<Symbol<T>, Symbol<T>> digrams = new HashMap<Symbol<T>, Symbol<T>>();

    // in writeOut, this map is filled
    private Map<Rule<T>, Long> ruleNumbers = new IdentityHashMap<Rule<T>, Long>();
    private long nextRuleNumber = 0;

    /**
     *
     * @param first
     * @return whether or not there was a substitution
     */
    public boolean checkDigram(final Symbol<T> first) {
        if (first instanceof Dummy<?> || first.next instanceof Dummy<?>)
            return false;

        if (first.meltDigram())
            return true;

        final Symbol<T> oldDigram = this.digrams.put(first, first);
        if (oldDigram == null)
            return false;

        // only substitude a new rule if the two digrams don't overlap!
        assert oldDigram.next != first;

        match(first, oldDigram);
        return true;
    }

    private void match(final Symbol<T> newDigram, final Symbol<T> oldDigram) {
        Rule<T> rule;
        if (oldDigram.prev instanceof Dummy<?> && oldDigram.next.next instanceof Dummy<?>
                && (rule = ((Dummy<T>)oldDigram.prev).getRule()).mayBeReused()) {
            newDigram.substituteDigram(rule, this);
        } else {
            final Symbol<T> clone = newDigram.clone();
            rule = new Rule<T>(clone, newDigram.next.clone());
            this.digrams.put(clone, clone);
            newDigram.substituteDigram(rule, this);
            oldDigram.substituteDigram(rule, this);
        }

        if (rule.getUseCount() > 0) {
            final Dummy<T> dummy = rule.dummy;
            for (Symbol<T> s = dummy.next; s != dummy; s = s.next) {
                if (s instanceof NonTerminal<?>)
                    ((NonTerminal<?>)s).checkExpand();
            }
        }
    }

    protected long getRuleNr(final Rule<T> rule) {
        return getRuleNr(rule, null);
    }

    protected long getRuleNr(final Rule<T> rule, final LinkedList<Rule<?>> queue) {
        Long nr = this.ruleNumbers.get(rule);
        if (nr == null) {
            if (queue != null)
                queue.add(rule);
            nr = this.nextRuleNumber++;
            // this rule must not be removed!!
            rule.incUseCount();
            try {
                this.ruleNumbers.put(rule, nr);
            } catch (final IllegalStateException e) {
                if (this.ruleNumbers.getClass().equals(TreeMap.class))
                    throw e;
                // capacity exceeded: switch to treemap
                this.ruleNumbers = new TreeMap<Rule<T>, Long>(this.ruleNumbers);
                this.ruleNumbers.put(rule, nr);
            }
        }
        return nr;
    }

    public static <T> Grammar<T> readFrom(final ObjectInputStream objIn, final Class<? extends T> checkInstance) {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
    private Rule<T>[][][] newRuleArray(final int dim1, final int dim2, final int dim3) {
        return (Rule<T>[][][]) (
                dim2 == -1 ? new Rule<?>[dim1][][]
              : dim3 == -1 ? new Rule<?>[dim1][dim2][]
              : new Rule<?>[dim1][dim2][dim3]);
    }
    @SuppressWarnings("unchecked")
    private Rule<T>[][] newRuleArray(final int dim1, final int dim2) {
        return (Rule<T>[][]) (
                dim2 == -1 ? new Rule<?>[dim1][]
              : new Rule<?>[dim1][dim2]);
    }
    @SuppressWarnings("unchecked")
    private Rule<T>[] newRuleArray(final int dim1) {
        return (Rule<T>[]) new Rule<?>[dim1];
    }

}
