package de.unisb.cs.st.javaslicer.tracer;

import java.io.IOException;

public interface ThreadTracer {

    void traceInt(int value, int traceSequenceIndex);

    void traceObject(Object obj, int traceSequenceIndex);

    void traceLastInstructionIndex(int traceSequenceIndex);

    void passInstruction(int instructionIndex);

    void finish() throws IOException;

    void pauseTracing();
    void resumeTracing();

    boolean isPaused();

    void enterMethod(int instructionIndex);

    void leaveMethod(int instructionIndex);

    // these two work together: after the NEW bytecode instruction,
    // objectAllocation is called to register the trace sequence where
    // the identity of the created object should be stored.
    // in the Object constructor, objectInitialization is called to store
    // the identity in the registered sequence.
    void objectAllocation(int traceSequenceNr);
    void objectInitialization(Object obj);

}
