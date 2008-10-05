package de.unisb.cs.st.javaslicer.tracer.util.sequitur.output;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import de.unisb.cs.st.javaslicer.tracer.util.sequitur.output.Rule.Dummy;

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

    protected long getRuleNr(final Rule<T> rule, final LinkedList<Rule<T>> queue) {
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

    public void writeOut(final ObjectOutputStream objOut, final ObjectWriter<? super T> objectWriter)
            throws IOException {
        final LinkedList<Rule<T>> ruleQueue = new LinkedList<Rule<T>>();
        // first, fill in already written rules
        if (TreeMap.class.equals(this.ruleNumbers.getClass())) {
            // on a TreeMap, we cannot rely on the size, because it is
            // stored in an int
            Rule<T>[][][] ruleArrs = newRuleArray(1, 1, 1<<20);
            long numRules = 0;
            final int low20mask = 0xfffff;
            for (final Entry<Rule<T>, Long> e: this.ruleNumbers.entrySet()) {
                ++numRules;
                final int pos1 = (int) (e.getValue() >>> 40);
                final int pos2 = (int) (e.getValue() >>> 20) & low20mask;
                final int pos3 = e.getValue().intValue() & low20mask;
                if (pos1 >= ruleArrs.length)
                    ruleArrs = Arrays.copyOf(ruleArrs,
                            Math.min(1<<24, Math.max(pos1+1, ruleArrs.length*5/4+1)));
                if (ruleArrs[pos1] == null)
                    ruleArrs[pos1] = newRuleArray(pos2+1, -1);
                if (pos2 >= ruleArrs[pos1].length)
                    ruleArrs[pos1] = Arrays.copyOf(ruleArrs[pos1],
                            Math.min(1<<20, Math.max(pos2+1, ruleArrs[pos1].length*5/4+1)));
                if (ruleArrs[pos1][pos2] == null)
                    ruleArrs[pos1][pos2] = newRuleArray(1<<20);
                assert ruleArrs[pos1][pos2][pos3] == null;
                ruleArrs[pos1][pos2][pos3] = e.getKey();
            }
            final int p1 = 0, p2 = 0;
            for (long l = 0; l < numRules; ) {
                if (numRules - l < 1<<20) {
                    for (int i = 0; l < numRules; ++l, ++i) {
                        ruleQueue.add(ruleArrs[p1][p2][i]);
                    }
                } else {
                    ruleQueue.addAll(Arrays.asList(ruleArrs[p1][p2]));
                    l += 1<<20;
                }
            }
        } else if (this.ruleNumbers.size() > 0) {
            final Rule<T>[] ruleArr = newRuleArray(this.ruleNumbers.size());
            for (final Entry<Rule<T>, Long> e: this.ruleNumbers.entrySet())
                ruleArr[e.getValue().intValue()] = e.getKey();
            ruleQueue.addAll(Arrays.asList(ruleArr));
        }

        while (!ruleQueue.isEmpty()) {
            final Rule<T> rule = ruleQueue.poll();
            rule.writeOut(objOut, this, objectWriter, ruleQueue);
        }
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
