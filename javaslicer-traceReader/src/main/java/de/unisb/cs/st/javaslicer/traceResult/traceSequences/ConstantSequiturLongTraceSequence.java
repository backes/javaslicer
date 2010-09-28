/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult.traceSequences
 *    Class:     ConstantSequiturLongTraceSequence
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/traceSequences/ConstantSequiturLongTraceSequence.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.traceResult.traceSequences;

import java.io.IOException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import de.hammacher.util.iterators.LongArrayIterator;
import de.hammacher.util.iterators.ReverseLongArrayIterator;
import de.hammacher.util.iterators.SingletonIterator;
import de.unisb.cs.st.javaslicer.traceResult.traceSequences.ConstantTraceSequence.ConstantLongTraceSequence;
import de.unisb.cs.st.sequitur.input.InputSequence;

public class ConstantSequiturLongTraceSequence implements ConstantLongTraceSequence {

    private static class BackwardIterator implements Iterator<Long> {

        private final ListIterator<Long> it;
        private long lastValue;
        private int count;

        public BackwardIterator(final ListIterator<Long> it, final int count) throws IOException {
            if (count < 0)
                throw new IOException("Illegal sequitur sequence (count < 0)");
            this.count = count;
            this.it = it;
            if (!it.hasPrevious())
                throw new IOException("Illegal sequitur sequence");
            this.lastValue = it.previous();
        }

        public boolean hasNext() {
            return this.count != 0 && this.it.hasPrevious();
        }

        public Long next() {
            if (this.count == 0)
                throw new NoSuchElementException();
            final long oldValue = this.lastValue;
            this.lastValue -= this.it.previous();
            --this.count;
            return oldValue;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    private static class ForwardIterator implements ListIterator<Long> {

        private final ListIterator<Long> it;
        private long lastValue;
        private final int count;
        private int pos;

        public ForwardIterator(final ListIterator<Long> it, final int count) throws IOException {
            if (count < 0)
                throw new IOException("Illegal sequitur sequence (count < 0)");
            this.count = count;
            this.pos = 0;
            this.it = it;
            this.lastValue = 0;
        }

        public boolean hasNext() {
            return this.pos != this.count && this.it.hasNext();
        }

        public Long next() {
            if (!hasNext())
                throw new NoSuchElementException();
            this.lastValue += this.it.next();
            ++this.pos;
            return this.lastValue;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void add(final Long e) {
            throw new UnsupportedOperationException();
        }

        public boolean hasPrevious() {
            return this.pos != 0 && this.it.hasPrevious();
        }

        public int nextIndex() {
            return this.pos;
        }

        public Long previous() {
            if (!hasPrevious())
                throw new NoSuchElementException();
            final long ret = this.lastValue;
            this.lastValue -= this.it.previous();
            --this.pos;
            return ret;
        }

        public int previousIndex() {
            return this.pos - 1;
        }

        public void set(final Long e) {
            throw new UnsupportedOperationException();
        }

    }

    private final long offset;
    private final int count;
    private final InputSequence<Long> sequence;

    public ConstantSequiturLongTraceSequence(final long offset, final int count,
            final InputSequence<Long> inputSequence) {
        this.offset = offset;
        this.count = count;
        this.sequence = inputSequence;
    }

    public Iterator<Long> backwardIterator() throws IOException {
        if (this.count <= 10) {
            if (this.count == 1)
                return new SingletonIterator<Long>(this.sequence.iterator(this.offset).next());
            final long[] values = new long[this.count];
            final ListIterator<Long> it = this.sequence.iterator(this.offset);
            int last = 0;
            for (int i = 0; i < this.count; ++i) {
                values[i] = last += it.next();
            }
            return new ReverseLongArrayIterator(values);
        }
        return new BackwardIterator(this.sequence.iterator(this.offset+this.count+1), this.count);
    }

    public ListIterator<Long> iterator() throws IOException {
        if (this.count <= 10) {
            if (this.count == 1)
                return new SingletonIterator<Long>(this.sequence.iterator(this.offset).next());
            final long[] values = new long[this.count];
            final ListIterator<Long> it = this.sequence.iterator(this.offset);
            long last = 0;
            for (int i = 0; i < this.count; ++i) {
                values[i] = last += it.next();
            }
            return new LongArrayIterator(values);
        }
        return new ForwardIterator(this.sequence.iterator(this.offset), this.count);
    }

}
