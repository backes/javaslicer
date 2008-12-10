package de.unisb.cs.st.javaslicer.tracer.sequitur.input;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import de.hammacher.util.LongArrayList;
import de.hammacher.util.LongHolder;
import de.hammacher.util.MyByteArrayInputStream;

// package-private
class Rule<T> {

    protected final List<Symbol<T>> symbols;
    private long length;
    private long[] positionAfter = null;

    protected Rule(final List<Symbol<T>> symbols) {
        this.symbols = symbols;
    }

    public void substituteRealRules(final Grammar<T> grammar) {
        final ListIterator<Symbol<T>> it = this.symbols.listIterator();
        while (it.hasNext()) {
            final Symbol<T> sym = it.next();
            if (sym instanceof NonTerminal<?>)
                it.set(((NonTerminal<T>)sym).substituteRealRules(grammar));
        }
    }

    public long getLength() {
        return this.length;
    }

    protected boolean computeLength() {
        if (this.length != 0)
            return true;
        long len = 0;
        for (final Symbol<T> sym: this.symbols) {
            final long symLen = sym.getLength(false);
            if (symLen == 0)
                return false;
            len += symLen;
        }
        this.length = len;
        return true;
    }

    public Set<Rule<T>> getUsedRules() {
        Set<Rule<T>> rules = new HashSet<Rule<T>>();
        long rulesAdded = 0;
        final Queue<Rule<T>> ruleQueue = new LinkedList<Rule<T>>();
        ruleQueue.add(this);

        while (!ruleQueue.isEmpty()) {
            final Rule<T> r = ruleQueue.poll();
            for (final Symbol<T> s: r.symbols) {
                if (s instanceof NonTerminal<?>) {
                    final Rule<T> newR = ((NonTerminal<T>)s).getRule();
                    if (rules.add(newR)) {
                        if (++rulesAdded == 1<<30)
                            rules = new TreeSet<Rule<T>>(rules);
                        ruleQueue.add(newR);
                    }
                }
            }
        }

        return rules;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("R").append(hashCode()).append(" -->");
        for (final Symbol<T> sym: this.symbols)
            sb.append(' ').append(sym);
        return sb.toString();
    }

    public static <T> LongArrayList<Rule<T>> readAll(final ObjectInputStream objIn,
            final ObjectReader<? extends T> objectReader,
            final Class<? extends T> checkInstance) throws IOException, ClassNotFoundException {

        final LongArrayList<Rule<T>> rules = new LongArrayList<Rule<T>>();
        readRules:
        while (true) {
            int header = objIn.read();
            int length;
            switch (header >> 6) {
            case 0:
                break readRules;
            case 1:
                length = DataInput.readInt(objIn);
                break;
            case 2:
                length = 2;
                break;
            case 3:
                length = 3;
                break;
            default:
                throw new InternalError();
            }
            final int additionalHeaderBytes = length / 4;
            final MyByteArrayInputStream headerInputStream;
            if (additionalHeaderBytes == 0) {
                headerInputStream = null;
            } else {
                // maximum rule length == 1 << 30, to fit in arraylist
                if (additionalHeaderBytes > 1 << 28)
                    throw new IOException("Rule longer than 1<<30??");
                final byte[] headerBuf = new byte[additionalHeaderBytes];
                objIn.readFully(headerBuf);
                headerInputStream = new MyByteArrayInputStream(headerBuf);
            }
            final List<Symbol<T>> symbols = new ArrayList<Symbol<T>>(length);
            int pos = 3;
            while (length-- != 0) {
                if (pos-- == 0) {
                    assert headerInputStream != null;
                    header = headerInputStream.read();
                    pos = 3;
                }
                switch ((header >> (2*pos)) & 3) {
                case 0:
                    symbols.add(NonTerminal.<T>readFrom(objIn, false));
                    break;
                case 1:
                    symbols.add(NonTerminal.<T>readFrom(objIn, true));
                    break;
                case 2:
                    symbols.add(Terminal.readFrom(objIn, false, objectReader, checkInstance));
                    break;
                case 3:
                    symbols.add(Terminal.readFrom(objIn, true, objectReader, checkInstance));
                    break;
                default:
                    throw new InternalError();
                }
            }
            rules.add(new Rule<T>(symbols));
        }

        rules.trimToSize();
        return rules;
    }

    /**
     * Returns the maximum offset (= index of a symbol in this rule), s.t. the number of
     * symbols before this offset is smaller or equal to the given position.
     *
     * @param position the position to determine the offset for
     * @param positionHolder an object where the position of the symbol at the returned
     *                    offset is stored (may be <code>null</code>)
     * @return the maximum offset whose position is smaller or equal to the given position
     */
    public int findOffset(final long position, final LongHolder positionHolder) {
        if (this.symbols.size() < 10) {
            // simply search for the position from the beginning
            int offset = 0;
            long after = 0;
            long newLength;
            while (after + (newLength = this.symbols.get(offset).getLength(false)) <= position) {
                after += newLength;
                ++offset;
            }
            if (positionHolder != null)
                positionHolder.set(after);
            return offset;
        }

        // initialize the position cache if necessary
        if (this.positionAfter == null) {
            this.positionAfter = new long[this.symbols.size()-1];
            long after = 0;
            for (int i = 0; i < this.symbols.size()-1; ++i) {
                this.positionAfter[i] = after += this.symbols.get(i).getLength(false);
            }
        }

        if (this.symbols.size() == 0 || this.positionAfter[0] > position) {
            if (positionHolder != null)
                positionHolder.set(0);
            return 0;
        }

        // now do a binary search
        int left = 0;
        int right = this.symbols.size()-1;
        int mid;

        while ((mid = (left + right) / 2) != left) {
            final long midVal = this.positionAfter[mid];
            if (midVal <= position)
                left = mid;
            else
                right = mid;
        }

        if (positionHolder != null)
            positionHolder.set(this.positionAfter[left]);
        return left+1;
    }

}
