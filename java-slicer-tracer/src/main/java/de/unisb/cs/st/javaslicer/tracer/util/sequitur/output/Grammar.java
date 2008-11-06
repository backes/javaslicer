package de.unisb.cs.st.javaslicer.tracer.util.sequitur.output;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.Map.Entry;

import de.unisb.cs.st.javaslicer.tracer.util.sequitur.output.Rule.Dummy;

// package-private
class Grammar<T> {

    private final Map<Symbol<T>, Symbol<T>> digrams = new HashMap<Symbol<T>, Symbol<T>>();

    private final List<OutputSequence<T>> usingSequences = new ArrayList<OutputSequence<T>>();

    // in writeOut, this map is filled
    private Map<Rule<T>, Long> ruleNumbers = new IdentityHashMap<Rule<T>, Long>();
    private long nextRuleNumber = 0;

    /**
     *
     * @param first
     * @return whether or not there was a substitution
     */
    public boolean checkDigram(final Symbol<T> first) {
        if (first.count == 0 || first.next.count == 0)
            return false;

        if (first.meltDigram(this))
            return true;

        final Symbol<T> oldDigram = this.digrams.get(first);
        if (oldDigram == null) {
            this.digrams.put(first, first);
            return false;
        } else if (oldDigram == first) // this is necessary, but it should not be...
            return false;

        assert oldDigram.next != first && oldDigram != first;

        match(first, oldDigram);
        return true;
    }

    public void removeDigram(final Symbol<T> sym) {
        if (this.digrams.get(sym) == sym)
            this.digrams.remove(sym);
    }

    private void match(final Symbol<T> newDigram, final Symbol<T> oldDigram) {
        Rule<T> rule;
        if (newDigram instanceof NonTerminal<?> && ((NonTerminal<T>)newDigram).getCount() == 1
                && (rule = ((NonTerminal<T>)newDigram).getRule()).getUseCount() == 2) {
            assert oldDigram instanceof NonTerminal<?> && ((NonTerminal<T>)oldDigram).getRule() == rule;
            final Symbol<T> next = newDigram.next;
            removeDigram(newDigram);
            if (!(next.next instanceof Dummy<?>))
                removeDigram(next);
            Symbol.linkTogether(newDigram, next.next);
            removeDigram(oldDigram);
            if (!(oldDigram.next.next instanceof Dummy<?>))
                removeDigram(oldDigram.next);
            oldDigram.next.remove();
            rule.append(next, this);

            // TODO check if rule is of length 1
            if (((NonTerminal<T>)newDigram).count != 0)
                ((NonTerminal<T>) newDigram).checkSubstRule(this);
            if (((NonTerminal<T>)oldDigram).count != 0)
                ((NonTerminal<T>) oldDigram).checkSubstRule(this);

            /*
            if (newDigram.prev == newDigram.next) {
                assert newDigram.next instanceof Dummy<?>;
                final Rule<T> otherRule = ((Dummy<T>)newDigram.next).getRule();
                // rule is expanded inside the otherRule, since otherRule consisted of only this one nonterminal
                if (!(oldDigram.prev instanceof Dummy<?>))
                    removeDigram(oldDigram.prev);
                if (!(oldDigram.next instanceof Dummy<?>))
                    removeDigram(oldDigram);
                // newDigram.remove(); // not needed, because it is overwritten by these instructions:
                Symbol.linkTogether(newDigram.next, rule.dummy.next);
                Symbol.linkTogether(rule.dummy.prev, newDigram.next);
                oldDigram.remove();
                oldDigram.next.insertBefore(new NonTerminal<T>(otherRule));
                checkDigram(oldDigram.prev);
                checkDigram(oldDigram.next);
            } else if (oldDigram.prev == oldDigram.next) {
                assert oldDigram.next instanceof Dummy<?>;
                final Rule<T> otherRule = ((Dummy<T>)oldDigram.next).getRule();
                // rule is expanded inside the otherRule, since otherRule consisted of only this one nonterminal
                if (!(newDigram.prev instanceof Dummy<?>))
                    removeDigram(newDigram.prev);
                if (!(newDigram.next instanceof Dummy<?>))
                    removeDigram(newDigram);
                Symbol<T> firstSubst = rule.dummy.next;
                Symbol<T> lastSubst = rule.dummy.prev;
                // if rule is used more than twice (in newDigram and oldDigram), we have to clone it
                if (rule.getUseCount() > 2) {
                    firstSubst = rule.dummy.next.clone();
                    Symbol<T> s = firstSubst;
                    while (s.next != lastSubst) {
                        (s.next = s.next.clone()).prev = s;
                        s = s.next;
                    }
                    (s.next = lastSubst = lastSubst.clone()).prev = s;
                }
                // oldDigram.remove(); // not needed, because it is overwritten by these instructions:
                Symbol.linkTogether(oldDigram.next, firstSubst);
                Symbol.linkTogether(lastSubst, oldDigram.next);
                newDigram.remove();
                newDigram.next.insertBefore(new NonTerminal<T>(otherRule));
                checkDigram(newDigram.prev);
                checkDigram(newDigram.next);
            }
            */
            checkDigram(newDigram);
            checkDigram(oldDigram);
        } else if (oldDigram.prev == oldDigram.next.next
                && (rule = ((Dummy<T>)oldDigram.prev).getRule()).mayBeReused()) {
            newDigram.substituteDigram(rule, this);
            if (rule.getUseCount() > 0) {
                final Dummy<T> dummy = rule.dummy;
                for (Symbol<T> s = dummy.next; s != dummy; s = s.next) {
                    if (s instanceof NonTerminal<?>)
                        ((NonTerminal<T>)s).checkExpand(this);
                }
            }
        } else if (newDigram.prev == newDigram.next.next
                && (rule = ((Dummy<T>)newDigram.prev).getRule()).mayBeReused()) {
            oldDigram.substituteDigram(rule, this);
            if (rule.getUseCount() > 0) {
                final Dummy<T> dummy = rule.dummy;
                for (Symbol<T> s = dummy.next; s != dummy; s = s.next) {
                    if (s instanceof NonTerminal<?>)
                        ((NonTerminal<T>)s).checkExpand(this);
                }
            }
        } else {
            final Symbol<T> clone = newDigram.clone();
            rule = new Rule<T>(clone, newDigram.next.clone());
            this.digrams.remove(clone);
            this.digrams.put(clone, clone);
            newDigram.substituteDigram(rule, this);
            oldDigram.substituteDigram(rule, this);
            if (rule.getUseCount() > 0) {
                final Dummy<T> dummy = rule.dummy;
                for (Symbol<T> s = dummy.next; s != dummy; s = s.next) {
                    if (s instanceof NonTerminal<?>)
                        ((NonTerminal<T>)s).checkExpand(this);
                }
            }
        }
    }

    protected long getRuleNr(final Rule<T> rule) {
        return getRuleNr(rule, null);
    }

    protected long getRuleNr(final Rule<T> rule, final Queue<Rule<T>> queue) {
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
        final Queue<Rule<T>> ruleQueue = new LinkedList<Rule<T>>();
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
        // then, fill in the first rule of sequences that use this grammar
        for (final OutputSequence<T> seq: this.usingSequences) {
            getRuleNr(seq.firstRule, ruleQueue);
        }
        for (final Rule<T> rule: ruleQueue)
            rule.ensureInvariants(this);

        if (ruleQueue.isEmpty()) {
            // write out a dummy entry
            objOut.write(0); // header: marked as being last
            objOut.write(0); // this rule contains 0 symbols
        } else {
            long ruleNr = 0;
            while (!ruleQueue.isEmpty()) {
                final Rule<T> rule = ruleQueue.poll();
                assert getRuleNr(rule) == ruleNr;
                ++ruleNr;
                rule.writeOut(objOut, this, objectWriter, ruleQueue);
            }
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

    protected void newSequence(final OutputSequence<T> seq) {
        this.usingSequences.add(seq);
    }

    // TODO remove
    @Override
    public String toString() {
        return this.digrams.toString() +
            System.getProperty("line.separator") +
            this.usingSequences.toString();
    }

}
