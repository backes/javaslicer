/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer.traceSequences
 *    Class:     TraceSequenceFactory
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/traceSequences/TraceSequenceFactory.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.io.IOException;
import java.io.OutputStream;

import de.unisb.cs.st.javaslicer.common.TraceSequenceTypes.Type;
import de.unisb.cs.st.javaslicer.tracer.ThreadTracer;
import de.unisb.cs.st.javaslicer.tracer.Tracer;


public interface TraceSequenceFactory {

    public interface PerThread {

        TraceSequence createTraceSequence(Type type, Tracer tracer) throws IOException;

        void finish() throws IOException;

        void writeOut(OutputStream out) throws IOException;

    }

    PerThread forThreadTracer(ThreadTracer tt);

    boolean shouldAutoFlushFile();

}
