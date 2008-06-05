package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataOutput;
import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.traceResult.ThreadTraceResult.BackwardInstructionIterator;

public class InstructionWrapper implements Instruction {

    private final Instruction wrappedInstruction;

    public InstructionWrapper(final Instruction wrappedInstruction) {
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

    public Instruction getNextInstance(final BackwardInstructionIterator backwardInstructionIterator) throws TracerException {
        return this.wrappedInstruction.getNextInstance(backwardInstructionIterator);
    }

    public int getOpcode() {
        return this.wrappedInstruction.getOpcode();
    }

    public void writeOut(final DataOutput out) throws IOException {
        this.wrappedInstruction.writeOut(out);
    }

}
