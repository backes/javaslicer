/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult.traceSequences
 *    Class:     ConstantSequiturIntegerTraceSequence
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/traceSequences/ConstantSequiturIntegerTraceSequence.java
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

import de.hammacher.util.iterators.IntArrayIterator;
import de.hammacher.util.iterators.ReverseIntArrayIterator;
import de.hammacher.util.iterators.SingletonIterator;
import de.unisb.cs.st.javaslicer.traceResult.traceSequences.ConstantTraceSequence.ConstantIntegerTraceSequence;
import de.unisb.cs.st.sequitur.input.InputSequence;

public class ConstantSequiturIntegerTraceSequence implements ConstantIntegerTraceSequence {

    private static class BackwardIterator implements Iterator<Integer> {

        private final ListIterator<Integer> it;
        private int lastValue;
        private int count;

        public BackwardIterator(final ListIterator<Integer> it, final int count) throws IOException {
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
		public Integer next() {
            if (this.count == 0)
                throw new NoSuchElementException();
            final int oldValue = this.lastValue;
            this.lastValue -= this.it.previous();
            --this.count;
            return oldValue;
        }

        @Override
		public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    private static class ForwardIterator implements ListIterator<Integer> {

        private final ListIterator<Integer> it;
        private int lastValue;
        private final int count;
        private int pos;

        public ForwardIterator(final ListIterator<Integer> it, final int count) throws IOException {
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
		public Integer next() {
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
		public void add(final Integer e) {
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
		public Integer previous() {
            if (!hasPrevious())
                throw new NoSuchElementException();
            final int ret = this.lastValue;
            this.lastValue -= this.it.previous();
            --this.pos;
            return ret;
        }

        @Override
		public int previousIndex() {
            return this.pos - 1;
        }

        @Override
		public void set(final Integer e) {
            throw new UnsupportedOperationException();
        }

    }

    private final long offset;
    private final int count;
    private final InputSequence<Integer> sequence;

    public ConstantSequiturIntegerTraceSequence(final long offset,
            final int count, final InputSequence<Integer> inputSequence) {
        this.offset = offset;
        this.count = count;
        this.sequence = inputSequence;
    }

    @Override
	public Iterator<Integer> backwardIterator() throws IOException {
        if (this.count <= 10) {
            if (this.count == 1)
                return new SingletonIterator<Integer>(this.sequence.iterator(this.offset).next());
            final int[] values = new int[this.count];
            final ListIterator<Integer> it = this.sequence.iterator(this.offset);
            int last = 0;
            for (int i = 0; i < this.count; ++i) {
                values[i] = last += it.next();
            }
            return new ReverseIntArrayIterator(values);
        }
        return new BackwardIterator(this.sequence.iterator(this.offset+this.count+1), this.count);
    }

    @Override
	public ListIterator<Integer> iterator() throws IOException {
        if (this.count <= 10) {
            if (this.count == 1)
                return new SingletonIterator<Integer>(this.sequence.iterator(this.offset).next());
            final int[] values = new int[this.count];
            final ListIterator<Integer> it = this.sequence.iterator(this.offset);
            int last = 0;
            for (int i = 0; i < this.count; ++i) {
                values[i] = last += it.next();
            }
            return new IntArrayIterator(values);
        }
        return new ForwardIterator(this.sequence.iterator(this.offset), this.count);
    }

}
