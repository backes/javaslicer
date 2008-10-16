package de.unisb.cs.st.javaslicer.tracer.util.sequitur.output;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

// package-private
class Rule<T> {

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
                final LinkedList<Rule<T>> queue) {
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
        return this.useCount;
    }

    public void writeOut(final ObjectOutputStream objOut, final Grammar<T> grammar,
            final ObjectWriter<? super T> objectWriter, final LinkedList<Rule<T>> ruleQueue)
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

            DataOutput.writeLong(objOut, length());

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

    private long length() {
        long length = 0;
        for (Symbol<T> s = this.dummy.next; s != this.dummy; s = s.next)
            ++length;
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

}
