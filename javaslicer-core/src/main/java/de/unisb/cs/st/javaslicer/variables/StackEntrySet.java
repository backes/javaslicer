package de.unisb.cs.st.javaslicer.variables;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.unisb.cs.st.javaslicer.dependenceAnalysis.ExecutionFrame;

public class StackEntrySet extends AbstractSet<Variable> {

    public class Itr implements Iterator<Variable> {

        private int itrOffset = StackEntrySet.this.topOffset-StackEntrySet.this.num;

        public boolean hasNext() {
            return this.itrOffset < StackEntrySet.this.topOffset;
        }

        public Variable next() {
            if (!hasNext())
                throw new NoSuchElementException();
            return StackEntrySet.this.frame.getStackEntry(this.itrOffset++);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    protected final ExecutionFrame frame;
    protected final int topOffset;
    protected final int num;

    /**
     *
     * @param frame The execution frame whose stack entries should be contained
     * @param topOffset The stack offset above the entries
     * @param num the number of entries
     */
    public StackEntrySet(final ExecutionFrame frame, final int topOffset, final int num) {
        this.frame = frame;
        this.topOffset = topOffset;
        this.num = num;
    }

    @Override
    public Iterator<Variable> iterator() {
        return new Itr();
    }

    @Override
    public int size() {
        return this.num;
    }

}
