/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer
 *    Class:     ThreadTracer
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/ThreadTracer.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
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
