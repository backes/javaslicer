package de.unisb.cs.st.javaslicer.tracer.traceResult;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;

public class TraceResult {

    private final List<ReadClass> readClasses;
    private final List<ConstantTraceSequence> sequences;

    public TraceResult(final List<ReadClass> readClasses, final List<ConstantTraceSequence> sequences) {
        this.readClasses = readClasses;
        this.sequences = sequences;
    }

    public static TraceResult readFrom(final ObjectInputStream in) throws IOException {
        int numClasses = in.readInt();
        final List<ReadClass> readClasses = new ArrayList<ReadClass>(numClasses);
        while (numClasses-- > 0)
            readClasses.add(ReadClass.readFrom(in));

        int numSeq = in.readInt();
        final List<ConstantTraceSequence> sequences = new ArrayList<ConstantTraceSequence>(numSeq);
        while (numSeq-- > 0)
            sequences.add(ConstantTraceSequence.readFrom(in));

        return new TraceResult(readClasses, sequences);
    }

    public Iterator<Instruction> getBackwardIterator() {
        // TODO
        return null;
    }

}
