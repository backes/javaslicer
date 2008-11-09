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

import de.unisb.cs.st.javaslicer.tracer.util.LongArrayList;
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

            if (((NonTerminal<T>)newDigram).count != 0 &&
                    ((NonTerminal<T>) newDigram).checkSubstRule(this))
                return;
            if (((NonTerminal<T>)oldDigram).count != 0 &&
                    ((NonTerminal<T>) oldDigram).checkSubstRule(this))
                return;

            if (newDigram.prev == newDigram.next && ((Dummy<?>)newDigram.next).getRule().mayBeReused()) {
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
            } else if (oldDigram.prev == oldDigram.next && ((Dummy<?>)oldDigram.next).getRule().mayBeReused()) {
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
            } else {
                checkDigram(newDigram);
                checkDigram(oldDigram);
            }
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
        // take core of the order!
        if (TreeMap.class.equals(this.ruleNumbers.getClass())) {
            // on a TreeMap, we cannot rely on the size, because it is
            // stored in an int
            final LongArrayList<Rule<T>> rules = new LongArrayList<Rule<T>>();
            long numRules = 0;
            for (final Entry<Rule<T>, Long> e: this.ruleNumbers.entrySet()) {
                ++numRules;
                rules.ensureCapacity(e.getValue());
                while (rules.longSize() <= e.getValue())
                    rules.add(null);
                assert rules.get(e.getValue()) == null;
                rules.set(e.getValue(), e.getKey());
            }
            assert numRules == rules.size();
            ruleQueue.addAll(rules);
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

        long ruleNr = 0;
        while (!ruleQueue.isEmpty()) {
            final Rule<T> rule = ruleQueue.poll();
            assert getRuleNr(rule) == ruleNr;
            ++ruleNr;
            rule.writeOut(objOut, this, objectWriter, ruleQueue);
        }
        objOut.write(0); // mark end of rules
    }

    @SuppressWarnings("unchecked")
    private Rule<T>[] newRuleArray(final int dim) {
        return (Rule<T>[]) new Rule<?>[dim];
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
