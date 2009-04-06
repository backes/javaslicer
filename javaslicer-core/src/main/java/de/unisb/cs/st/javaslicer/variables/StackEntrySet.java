package de.unisb.cs.st.javaslicer.variables;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.unisb.cs.st.javaslicer.instructionSimulation.ExecutionFrame;

public class StackEntrySet<InstanceType> extends AbstractSet<Variable> {

    public class Itr implements Iterator<Variable> {

        private int itrOffset = StackEntrySet.this.offset+StackEntrySet.this.num-1;

        public boolean hasNext() {
            return this.itrOffset >= StackEntrySet.this.offset;
        }

        public Variable next() {
            if (!hasNext())
                throw new NoSuchElementException();
            return StackEntrySet.this.frame.getStackEntry(this.itrOffset--);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    protected final ExecutionFrame<InstanceType> frame;
    protected final int offset;
    protected final int num;

    /**
     *
     * @param frame The execution frame whose stack entries should be contained
     * @param offset The stack offset of the first entry
     * @param num the number of entries
     */
    public StackEntrySet(final ExecutionFrame<InstanceType> frame, final int offset, final int num) {
        assert offset >= 0 || frame.interruptedControlFlow;
        assert num >= 0;
        this.frame = frame;
        this.offset = offset;
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
