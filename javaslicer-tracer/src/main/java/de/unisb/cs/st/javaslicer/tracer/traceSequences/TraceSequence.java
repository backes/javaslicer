/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer.traceSequences
 *    Class:     TraceSequence
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/traceSequences/TraceSequence.java
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
