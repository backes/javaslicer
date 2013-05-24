/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult.traceSequences
 *    Class:     ConstantSequiturLongTraceSequence
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/traceSequences/ConstantSequiturLongTraceSequence.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
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

        @Override
		public boolean hasNext() {
            return this.count != 0 && this.it.hasPrevious();
        }

        @Override
		public Long next() {
            if (this.count == 0)
                throw new NoSuchElementException();
            final long oldValue = this.lastValue;
            this.lastValue -= this.it.previous();
            --this.count;
            return oldValue;
        }

        @Override
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

        @Override
		public boolean hasNext() {
            return this.pos != this.count && this.it.hasNext();
        }

        @Override
		public Long next() {
            if (!hasNext())
                throw new NoSuchElementException();
            this.lastValue += this.it.next();
            ++this.pos;
            return this.lastValue;
        }

        @Override
		public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
		public void add(final Long e) {
            throw new UnsupportedOperationException();
        }

        @Override
		public boolean hasPrevious() {
            return this.pos != 0 && this.it.hasPrevious();
        }

        @Override
		public int nextIndex() {
            return this.pos;
        }

        @Override
		public Long previous() {
            if (!hasPrevious())
                throw new NoSuchElementException();
            final long ret = this.lastValue;
            this.lastValue -= this.it.previous();
            --this.pos;
            return ret;
        }

        @Override
		public int previousIndex() {
            return this.pos - 1;
        }

        @Override
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

    @Override
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

    @Override
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
