package de.unisb.cs.st.javaslicer.tracer.util.sequitur;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;

import de.unisb.cs.st.javaslicer.tracer.util.sequitur.Rule.Dummy;

public class Grammar<T> {

    private final Map<Symbol<T>, Symbol<T>> digrams = new HashMap<Symbol<T>, Symbol<T>>();

    // in writeOut, this map is filled
    private final Map<Rule<T>, Long> ruleNumbers = new IdentityHashMap<Rule<T>, Long>();
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

    public void writeOut(final ObjectOutputStream objOut) {
        final LinkedList<Rule<T>> ruleQueue = new LinkedList<Rule<T>>();
        // first, fill in already written rules
        if (this.ruleNumbers.size() >= Integer.MAX_VALUE) {
            // TODO problem: IdentityHashMap can take only 1<<29 elements!
            final Rule<?>[][] ruleArrs = new Rule<?>[1][1<<30];
            /*
            Arrays.sort(ruleArr, new Comparator<Rule<?>>() {
                @SuppressWarnings("unchecked")
                @Override
                public int compare(final Rule<?> r1, final Rule<?> r2) {
                    return Long.signum(getRuleNr((Rule<T>) r1) - getRuleNr((Rule<T>) r2));
                }
            });
            for (final Rule r: ruleArr)
                ruleQueue.add(r);
            */
        } else if (this.ruleNumbers.size() > 0) {
            final Rule<?>[] ruleArr = this.ruleNumbers.keySet().toArray(new Rule<?>[this.ruleNumbers.size()]);
            Arrays.sort(ruleArr, new Comparator<Rule<?>>() {
                @SuppressWarnings("unchecked")
                @Override
                public int compare(final Rule<?> r1, final Rule<?> r2) {
                    return Long.signum(getRuleNr((Rule<T>) r1) - getRuleNr((Rule<T>) r2));
                }
            });
            for (final Rule r: ruleArr)
                ruleQueue.add(r);
        }
        // TODO Auto-generated method stub

    }

    protected long getRuleNr(final Rule<T> rule) {
        Long nr = this.ruleNumbers.get(rule);
        if (nr == null)
            this.ruleNumbers.put(rule, nr = this.nextRuleNumber++);
        return nr;
    }

    public static <T> Grammar<T> readFrom(final ObjectInputStream objIn, final Class<? extends T> checkInstance) {
        // TODO Auto-generated method stub
        return null;
    }
}
