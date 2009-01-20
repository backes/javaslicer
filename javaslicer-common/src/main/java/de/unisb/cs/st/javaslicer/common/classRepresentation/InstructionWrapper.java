package de.unisb.cs.st.javaslicer.common.classRepresentation;

import java.io.DataOutputStream;
import java.io.IOException;

import de.hammacher.util.StringCacheOutput;
import de.unisb.cs.st.javaslicer.common.exceptions.TracerException;

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

    public int getBackwardInstructionIndex(final TraceIterationInformationProvider infoProv) {
        return this.wrappedInstruction.getBackwardInstructionIndex(infoProv);
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

    public InstructionInstance getNextInstance(final TraceIterationInformationProvider infoProv, final int stackDepth)
            throws TracerException {
        return this.wrappedInstruction.getNextInstance(infoProv, stackDepth);
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

    public int compareTo(final Instruction o) {
        return this.wrappedInstruction.compareTo(o);
    }

}
