package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataOutput;
import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.traceResult.ThreadTraceResult.BackwardInstructionIterator;

public interface Instruction {

    int getIndex();

    ReadMethod getMethod();

    int getOpcode();

    int getLineNumber();

    int getBackwardInstructionIndex(final BackwardInstructionIterator backwardInstructionIterator);

    Instruction getNextInstance(final BackwardInstructionIterator backwardInstructionIterator) throws TracerException;

    void writeOut(final DataOutput out) throws IOException;

}
