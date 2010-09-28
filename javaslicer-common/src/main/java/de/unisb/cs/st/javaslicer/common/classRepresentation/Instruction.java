/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation
 *    Class:     Instruction
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/Instruction.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.common.classRepresentation;

import java.io.DataOutputStream;
import java.io.IOException;

import de.hammacher.util.StringCacheOutput;
import de.unisb.cs.st.javaslicer.common.exceptions.TracerException;

/**
 * Interface for all instructions that are stored in a {@link ReadMethod}.
 *
 * @author Clemens Hammacher
 */
public interface Instruction extends Comparable<Instruction> {

    int getIndex();

    ReadMethod getMethod();

    int getOpcode();

    int getLineNumber();

    int getBackwardInstructionIndex(final TraceIterator infoProv);

    // for internal use only!
    <InstanceType> InstanceType getNextInstance(final TraceIterator infoProv,
            int stackDepth, long instanceNr, InstructionInstanceFactory<InstanceType> instanceFactory)
        throws TracerException;

    void writeOut(final DataOutputStream out, StringCacheOutput stringCache) throws IOException;

    InstructionType getType();

    /**
     * Returns the (statically) preceeding instruction, which might be null if
     * <code>this</code> instruction is the first one of the method.
     *
     * The index of the previous instruction is the index of this one minus one.
     *
     * @return the previous instruction
     */
    Instruction getPrevious();

    /**
     * Returns the (statically) next instruction, which might be null if
     * <code>this</code> instruction is the last one of the method.
     *
     * The index of the next instruction is the index of this one plus one.
     *
     * @return the next instruction
     */
    Instruction getNext();

}
