package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.traceResult.ThreadTraceResult.BackwardInstructionIterator;

/**
 * An abstract class that wraps around another instruction and delegates all calles to this
 * inner instruction.
 *
 * @author Clemens Hammacher
 */
public abstract class InstructionWrapper implements Instruction {

    private final Instruction wrappedInstruction;

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

    public Instance getNextInstance(final BackwardInstructionIterator backwardInstructionIterator) throws TracerException, EOFException {
        return this.wrappedInstruction.getNextInstance(backwardInstructionIterator);
    }

    public int getOpcode() {
        return this.wrappedInstruction.getOpcode();
    }

    public void writeOut(final DataOutput out) throws IOException {
        this.wrappedInstruction.writeOut(out);
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

}
