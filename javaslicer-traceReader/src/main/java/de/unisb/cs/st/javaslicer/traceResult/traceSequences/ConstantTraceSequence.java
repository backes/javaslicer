/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult.traceSequences
 *    Class:     ConstantTraceSequence
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/traceSequences/ConstantTraceSequence.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.traceResult.traceSequences;

import java.io.IOException;
import java.util.Iterator;
import java.util.ListIterator;

public interface ConstantTraceSequence {

    public interface ConstantIntegerTraceSequence extends ConstantTraceSequence {

        ListIterator<Integer> iterator() throws IOException;
        Iterator<Integer> backwardIterator() throws IOException;

    }

    public interface ConstantLongTraceSequence extends ConstantTraceSequence {

        ListIterator<Long> iterator() throws IOException;
        Iterator<Long> backwardIterator() throws IOException;

    }

}
