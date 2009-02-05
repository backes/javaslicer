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
    // objectAllocated is called to register the trace sequence where
    // the identity of the created object should be stored.
    // after the initialization of the object (the constructor call),
    // the identitiy of the created object is stored in the registered
    // sequence.
    // this is necessary because we cannot use the allocated but uninitialized object!
    void objectAllocated(int instructionIndex, int traceSequenceNr);
    void objectInitialized(Object obj);

}
