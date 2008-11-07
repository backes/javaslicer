package de.unisb.cs.st.javaslicer.tracer.util.sequitur.output;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import de.unisb.cs.st.javaslicer.tracer.util.Pair;

// package-private
class Rule<T> {

    protected static class Dummy<T> extends Symbol<T> {

        private final Rule<T> rule;

        public Dummy(final Rule<T> rule) {
            super(0);
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
        public boolean meltDigram(final Grammar<T> grammar) {
            return false;
        }

        @Override
        public int getHeader() {
            assert false;
            return 0;
        }

        @Override
        public void writeOut(final ObjectOutputStream objOut, final Grammar<T> grammar, final ObjectWriter<? super T> objectWriter,
                final Queue<Rule<T>> queue) {
            assert false;
        }
    }

    protected final Dummy<T> dummy;
    private int useCount;

    public Rule() {
        this(true);
    }

    public Rule(final boolean mayBeReused) {
        this.useCount = mayBeReused ? 0 : -1;
        this.dummy = new Dummy<T>(this);
    }

    public Rule(final Symbol<T> first, final Symbol<T> second) {
        this();
        Symbol.linkTogether(this.dummy, first);
        Symbol.linkTogether(first, second);
        Symbol.linkTogether(second, this.dummy);
    }

    public void append(final Symbol<T> newSymbol, final Grammar<T> grammar) {
        this.dummy.insertBefore(newSymbol);
        grammar.checkDigram(newSymbol.prev);
    }

    /**
     * Returns <code>true</code> if this rule is not the first rule of a compressed sequence.
     */
    public boolean mayBeReused() {
        return this.useCount >= 0;
    }

    public void incUseCount() {
        if (this.useCount != -1)
            ++this.useCount;
    }

    public void decUseCount() {
        if (this.useCount != -1)
            --this.useCount;
    }

    public int getUseCount() {
        return this.useCount;
    }

    public void writeOut(final ObjectOutputStream objOut, final Grammar<T> grammar,
            final ObjectWriter<? super T> objectWriter, final Queue<Rule<T>> ruleQueue)
                throws IOException {
        if (ruleQueue.isEmpty()) {
            for (Symbol<T> s = this.dummy.next; s != this.dummy; s = s.next) {
                if (s instanceof NonTerminal<?>)
                    grammar.getRuleNr(((NonTerminal<T>)s).getRule(), ruleQueue);
            }
        }

        int header = 0;
        long written;
        Symbol<T> next = this.dummy.next;
        for (written = 0; written < 3 && next != this.dummy; ++written, next = next.next)
            header |= (next.getHeader() << (4 - 2 * written));
        assert (written >= 2 && written <= 3) || (written >= 0 && this.useCount == -1);
        if (next == this.dummy && !ruleQueue.isEmpty()) {
            header |= written == 2 ? (2 << 6) : (3 << 6);
            objOut.write(header);
        } else {
            if (!ruleQueue.isEmpty())
                header |= 1 << 6;
            objOut.write(header);

            DataOutput.writeInt(objOut, length());

            int pos = 4;
            int b = 0;
            for (; next != this.dummy; next = next.next) {
                if (--pos == -1) {
                    objOut.write(b);
                    pos = 3;
                    b = 0;
                }
                b |= next.getHeader() << (2*pos);
            }
            if (pos != 4)
                objOut.write(b);
        }

        for (Symbol<T> s = this.dummy.next; s != this.dummy; s = s.next) {
            s.writeOut(objOut, grammar, objectWriter, ruleQueue);
        }
    }

    private int length() throws IOException {
        int length = 0;
        for (Symbol<T> s = this.dummy.next; s != this.dummy; s = s.next)
            if (length++ == 1<<30)
                throw new IOException("Rule length > 1<<30!!");
        return length;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("R").append(hashCode()).append(" -->");
        for (Symbol<T> s = this.dummy.next; s != this.dummy; s = s.next)
            sb.append(' ').append(s);
        return sb.toString();
    }

    public Set<Rule<T>> getUsedRules() {
        Set<Rule<T>> rules = new HashSet<Rule<T>>();
        long rulesAdded = 0;
        final Queue<Rule<T>> ruleQueue = new LinkedList<Rule<T>>();
        ruleQueue.add(this);

        while (!ruleQueue.isEmpty()) {
            final Rule<T> r = ruleQueue.poll();
            for (Symbol<T> s = r.dummy.next; s != r.dummy; s = s.next)
                if (s instanceof NonTerminal<?>) {
                    final Rule<T> newR = ((NonTerminal<T>)s).getRule();
                    if (rules.add(newR)) {
                        if (++rulesAdded == 1<<30)
                            rules = new TreeSet<Rule<T>>(rules);
                        ruleQueue.add(newR);
                    }
                }
        }

        return rules;
    }

    protected void ensureInvariants(final Grammar<T> grammar) {
        boolean changed = true;
        while (changed) {
            changed = false;
            final Queue<Rule<T>> queue = new ArrayDeque<Rule<T>>();
            final Set<Rule<T>> ready = new HashSet<Rule<T>>();
            queue.add(this);
            ready.add(this);

            outer:
            while (!queue.isEmpty()) {
                final Rule<T> rule = queue.poll();
                if (rule.getUseCount() == 0)
                    continue;
                for (Symbol<T> s = rule.dummy.next; s != rule.dummy; s = s.next) {
                    if (s instanceof NonTerminal<?>) {
                        final Rule<T> r2 = ((NonTerminal<T>)s).getRule();
                        if (r2.dummy.next.next == r2.dummy || (r2.getUseCount() == 1 && s.getCount() == 1)) {
                            if (!(s.prev instanceof Dummy<?>))
                                grammar.removeDigram(s.prev);
                            if (!(s.next instanceof Dummy<?>))
                                grammar.removeDigram(s);
                            s.remove();
                            final Pair<Symbol<T>, Symbol<T>> cloned = cloneRule(r2);
                            if (s.getCount() > 1)
                                cloned.getFirst().count *= s.getCount();
                            Symbol.linkTogether(s.prev, cloned.getFirst());
                            Symbol.linkTogether(cloned.getSecond(), s.next);
                            if (!grammar.checkDigram(s.prev))
                                grammar.checkDigram(s);
                            queue.add(rule);
                            changed = true;
                            continue outer;
                        } else if (ready.add(r2)) {
                            queue.add(r2);
                        }
                    }
                }
            }
        }
        while (this.dummy.next.next == this.dummy && (this.dummy.next instanceof NonTerminal<?>) && this.dummy.next.count == 1) {
            final NonTerminal<T> s = (NonTerminal<T>) this.dummy.next;
            s.remove();
            if (!(s.prev instanceof Dummy<?>))
                grammar.removeDigram(s.prev);
            if (!(s.next instanceof Dummy<?>))
                grammar.removeDigram(s);
            final Rule<T> rule = s.getRule();
            final Pair<Symbol<T>, Symbol<T>> cloned = cloneRule(rule);
            Symbol.linkTogether(s.prev, cloned.getFirst());
            Symbol.linkTogether(cloned.getSecond(), s.next);
            if (!grammar.checkDigram(s.prev))
                grammar.checkDigram(s);
        }

    }

    private static <T> Pair<Symbol<T>, Symbol<T>> cloneRule(final Rule<T> rule) {
        final Symbol<T> firstSym = rule.dummy.next.clone();
        Symbol<T> lastSym = firstSym;
        while (lastSym.next != rule.dummy) {
            final Symbol<T> new2 = lastSym.next.clone();
            Symbol.linkTogether(lastSym, new2);
            lastSym = new2;
        }
        return new Pair<Symbol<T>, Symbol<T>>(firstSym, lastSym);
    }

}
