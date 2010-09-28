/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer
 *    Class:     ThreadTracer
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/ThreadTracer.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
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
