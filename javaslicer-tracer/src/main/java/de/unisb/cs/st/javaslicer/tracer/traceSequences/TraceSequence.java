/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer.traceSequences
 *    Class:     TraceSequence
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/traceSequences/TraceSequence.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.io.DataOutputStream;
import java.io.IOException;

public interface TraceSequence {

    public interface IntegerTraceSequence extends TraceSequence {
        public abstract void trace(final int value) throws IOException;
    }

    public interface LongTraceSequence extends TraceSequence {
        public abstract void trace(final long value) throws IOException;
    }

    void writeOut(DataOutputStream out) throws IOException;

    void finish() throws IOException;

    /**
     * Determines whether the individual sequences are {@link #finish()}ed
     * in parallel.
     *
     * @return <code>true</code> iff the sequences should be finished in parallel
     */
    boolean useMultiThreading();

}
