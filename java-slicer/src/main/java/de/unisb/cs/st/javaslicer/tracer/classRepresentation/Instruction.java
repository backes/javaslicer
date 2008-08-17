package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.traceResult.ThreadTraceResult.BackwardInstructionIterator;

public interface Instruction {

    int getIndex();

    ReadMethod getMethod();

    int getOpcode();

    int getLineNumber();

    int getBackwardInstructionIndex(final BackwardInstructionIterator backwardInstructionIterator);

    Instance getNextInstance(final BackwardInstructionIterator backwardInstructionIterator) throws TracerException, EOFException;

    void writeOut(final DataOutput out) throws IOException;

    public interface Instance extends Instruction {

        long getOccurenceNumber();

    }

}
