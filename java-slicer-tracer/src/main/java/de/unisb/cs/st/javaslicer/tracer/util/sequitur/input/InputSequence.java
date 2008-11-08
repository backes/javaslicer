package de.unisb.cs.st.javaslicer.tracer.util.sequitur.input;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class InputSequence<T> implements Iterable<T> {

    public static class Itr<T> implements ListIterator<T> {

        private long pos;
        private final long seqLength;
        private final List<Rule<T>> ruleStack = new ArrayList<Rule<T>>();
        private int[] rulePos;
        private int[] count;

        public Itr(final long position, final Rule<T> firstRule) {
            this.pos = position;
            this.seqLength = firstRule.getLength();
            if (position == 0) {
                Rule<T> rule = firstRule;
                while (true) {
                    this.ruleStack.add(rule);
                    final Symbol<T> sym = rule.symbols.get(0);
                    if (sym instanceof Terminal<?>)
                        break;
                    rule = ((NonTerminal<T>)sym).getRule();
                }
                final int depth = Math.max(Integer.highestOneBit(this.ruleStack.size()-1)*2, 1);
                this.rulePos = new int[depth];
                this.count = new int[depth];
            } else if (position == firstRule.getLength()) {
                Rule<T> rule = firstRule;
                this.rulePos = new int[2];
                this.count = new int[2];
                int i = 0;
                while (true) {
                    this.ruleStack.add(rule);
                    final int ruleSymLength = rule.symbols.size();
                    final Symbol<T> sym = rule.symbols.get(ruleSymLength-1);
                    if (this.rulePos.length == i) {
                        this.rulePos = Arrays.copyOf(this.rulePos, 2*i);
                        this.count = Arrays.copyOf(this.count, 2*i);
                    }
                    if (sym instanceof Terminal<?>) {
                        // move behind the last symbol:
                        this.rulePos[i] = ruleSymLength;
                        break;
                    }
                    this.rulePos[i] = ruleSymLength - 1;
                    this.count[i++] = sym.count - 1;
                    rule = ((NonTerminal<T>)sym).getRule();
                }
            } else {
                Rule<T> rule = firstRule;
                this.rulePos = new int[2];
                this.count = new int[2];
                long after = 0;
                int i = 0;
                while (true) {
                    this.ruleStack.add(rule);
                    int ruleOffset = 0;
                    long newLength;
                    while (after + (newLength = rule.symbols.get(ruleOffset).getLength(false)) <= position) {
                        after += newLength;
                        ++ruleOffset;
                    }
                    if (this.rulePos.length == i) {
                        this.rulePos = Arrays.copyOf(this.rulePos, 2*i);
                        this.count = Arrays.copyOf(this.count, 2*i);
                    }
                    this.rulePos[i] = ruleOffset;
                    final Symbol<T> sym = rule.symbols.get(ruleOffset);
                    if (sym.count > 1) {
                        final long oneLength = sym.getLength(true);
                        this.count[i] = (int) ((position - after) / oneLength);
                        if (this.count[i] > 0)
                            after += this.count[i] * oneLength;
                        assert this.count[i] >= 0 && this.count[i] < sym.count;
                    }
                    if (sym instanceof Terminal<?>)
                        break;
                    rule = ((NonTerminal<T>)sym).getRule();
                    ++i;
                }
                assert after == position;
            }
        }

        @Override
        public void add(final T e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            return this.pos != this.seqLength;
        }

        @Override
        public boolean hasPrevious() {
            return this.pos != 0;
        }

        @Override
        public T next() {
            if (!hasNext())
                throw new NoSuchElementException();
            int depth = this.ruleStack.size()-1;
            List<Symbol<T>> ruleSymbols = this.ruleStack.get(depth).symbols;
            Symbol<T> sym = ruleSymbols.get(this.rulePos[depth]);
            final T value = ((Terminal<T>)sym).getValue();

            while (true) {
                if (this.count[depth]+1 < sym.count) {
                    ++this.count[depth];
                    break;
                }
                if (this.rulePos[depth] != ruleSymbols.size()-1) {
                    sym = ruleSymbols.get(++this.rulePos[depth]);
                    this.count[depth] = 0;
                    break;
                }
                if (depth == 0) {
                    assert this.pos == this.seqLength - 1;
                    ++this.rulePos[0];
                    this.count[0] = 0;
                    ++this.pos;
                    return value;
                }
                this.ruleStack.remove(depth);
                ruleSymbols = this.ruleStack.get(--depth).symbols;
                sym = ruleSymbols.get(this.rulePos[depth]);
            }
            while (sym instanceof NonTerminal<?>) {
                final Rule<T> rule = ((NonTerminal<T>)sym).getRule();
                this.ruleStack.add(rule);
                if (this.rulePos.length == ++depth) {
                    this.rulePos = Arrays.copyOf(this.rulePos, 2*depth);
                    this.count = Arrays.copyOf(this.count, 2*depth);
                }
                this.rulePos[depth] = 0;
                this.count[depth] = 0;
                sym = rule.symbols.get(0);
            }
            ++this.pos;
            return value;
        }

        @Override
        public int nextIndex() {
            if (this.pos >= Integer.MAX_VALUE)
                return Integer.MAX_VALUE;
            return (int) this.pos;
        }

        @Override
        public T previous() {
            if (!hasPrevious())
                throw new NoSuchElementException();

            int depth = this.ruleStack.size()-1;

            Symbol<T> sym;
            while (true) {
                if (this.count[depth] != 0) {
                    --this.count[depth];
                    sym = this.ruleStack.get(depth).symbols.get(this.rulePos[depth]);
                    break;
                }
                if (this.rulePos[depth] != 0) {
                    sym = this.ruleStack.get(depth).symbols.get(--this.rulePos[depth]);
                    this.count[depth] = sym.count-1;
                    break;
                }
                this.ruleStack.remove(depth--);
            }
            while (sym instanceof NonTerminal<?>) {
                final Rule<T> rule = ((NonTerminal<T>)sym).getRule();
                this.ruleStack.add(rule);
                if (this.rulePos.length == ++depth) {
                    this.rulePos = Arrays.copyOf(this.rulePos, 2*depth);
                    this.count = Arrays.copyOf(this.count, 2*depth);
                }
                this.rulePos[depth] = rule.symbols.size()-1;
                sym = rule.symbols.get(this.rulePos[depth]);
                this.count[depth] = sym.count-1;
            }
            --this.pos;
            return ((Terminal<T>)sym).getValue();
        }

        @Override
        public int previousIndex() {
            if (this.pos > Integer.MAX_VALUE)
                return Integer.MAX_VALUE;
            return (int) (this.pos-1);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(final T e) {
            throw new UnsupportedOperationException();
        }

    }

    private final Rule<T> firstRule;

    private InputSequence(final Rule<T> firstRule) {
        this.firstRule = firstRule;
    }

    public InputSequence(final long startRuleNumber, final SharedInputGrammar<T> grammar) {
        this(getStartRule(startRuleNumber, grammar));
    }

    private static <T> Rule<T> getStartRule(final long startRuleNumber, final SharedInputGrammar<T> grammar) {
        final Rule<T> rule = grammar.grammar.getRule(startRuleNumber);
        if (rule == null)
            throw new IllegalArgumentException("Unknown start rule number");
        return rule;
    }

    public ListIterator<T> iterator() {
        return iterator(0);
    }

    public ListIterator<T> iterator(final long position) {
        return new Itr<T>(position, this.firstRule);
    }

    public long getLength() {
        return this.firstRule.getLength();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final Iterator<Symbol<T>> it = this.firstRule.symbols.iterator();
        if (it.hasNext()) {
            sb.append(it.next());
            while (it.hasNext())
                sb.append(' ').append(it.next());
        }

        final Set<Rule<T>> rules = this.firstRule.getUsedRules();
        for (final Rule<T> r: rules)
            sb.append(System.getProperty("line.separator")).append(r);
        return sb.toString();
    }

    public static InputSequence<Object> readFrom(final ObjectInputStream objIn)
            throws IOException, ClassNotFoundException {
        return readFrom(objIn, SharedInputGrammar.readFrom(objIn));
    }

    public static <T> InputSequence<T> readFrom(final ObjectInputStream objIn,
            final Class<? extends T> checkInstance) throws IOException, ClassNotFoundException {
        return readFrom(objIn, SharedInputGrammar.readFrom(objIn, checkInstance));
    }

    public static <T> InputSequence<T> readFrom(final ObjectInputStream objIn,
            final ObjectReader<? extends T> objectReader) throws IOException, ClassNotFoundException {
        return readFrom(objIn, SharedInputGrammar.readFrom(objIn, objectReader));
    }

    public static <T> InputSequence<T> readFrom(final ObjectInputStream objIn,
            final ObjectReader<? extends T> objectReader, final Class<? extends T> checkInstance)
            throws IOException, ClassNotFoundException {
        return readFrom(objIn, SharedInputGrammar.readFrom(objIn, objectReader, checkInstance));
    }

    public static <T> InputSequence<T> readFrom(final ObjectInputStream objIn, final SharedInputGrammar<T> sharedGrammar) throws IOException {
        if (sharedGrammar == null)
            throw new NullPointerException();
        final long startRuleNr = DataInput.readLong(objIn);
        final Rule<T> rule = sharedGrammar.grammar.getRule(startRuleNr);
        if (rule == null)
            throw new IOException("Unknown rule number");
        return new InputSequence<T>(rule);
    }

}
