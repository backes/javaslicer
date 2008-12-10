package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataOutputStream;
import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.traceResult.BackwardInstructionIterator;
import de.unisb.cs.st.javaslicer.tracer.traceResult.ForwardInstructionIterator;

/**
 * An abstract class that wraps around another instruction and delegates all calles to this
 * inner instruction.
 *
 * @author Clemens Hammacher
 */
public abstract class InstructionWrapper implements Instruction {

    protected final Instruction wrappedInstruction;

    public InstructionWrapper(final Instruction wrappedInstruction) {
        if (wrappedInstruction == null)
            throw new NullPointerException();
        this.wrappedInstruction = wrappedInstruction;
    }

    public int getBackwardInstructionIndex(final BackwardInstructionIterator backwardInstructionIterator) {
        return this.wrappedInstruction.getBackwardInstructionIndex(backwardInstructionIterator);
    }

    public int getIndex() {
        return this.wrappedInstruction.getIndex();
    }

    public int getLineNumber() {
        return this.wrappedInstruction.getLineNumber();
    }

    public ReadMethod getMethod() {
        return this.wrappedInstruction.getMethod();
    }

    public Instance getBackwardInstance(final BackwardInstructionIterator backwardInstructionIterator, final int stackDepth)
            throws TracerException {
        return this.wrappedInstruction.getBackwardInstance(backwardInstructionIterator, stackDepth);
    }

    public Instance getForwardInstance(final ForwardInstructionIterator forwardInstructionIterator, final int stackDepth)
            throws TracerException {
        return this.wrappedInstruction.getForwardInstance(forwardInstructionIterator, stackDepth);
    }

    public int getOpcode() {
        return this.wrappedInstruction.getOpcode();
    }

    public Type getType() {
        return this.wrappedInstruction.getType();
    }

    public Instruction getNext() {
        return this.wrappedInstruction.getNext();
    }

    public Instruction getPrevious() {
        return this.wrappedInstruction.getPrevious();
    }

    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        this.wrappedInstruction.writeOut(out, stringCache);
    }

    @Override
    public int hashCode() {
        return 31 + this.wrappedInstruction.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final InstructionWrapper other = (InstructionWrapper) obj;
        return this.wrappedInstruction.equals(other.wrappedInstruction);
    }

    @Override
    public String toString() {
        return this.wrappedInstruction.toString();
    }

    @Override
    public int compareTo(final Instruction o) {
        return this.wrappedInstruction.compareTo(o);
    }

}
