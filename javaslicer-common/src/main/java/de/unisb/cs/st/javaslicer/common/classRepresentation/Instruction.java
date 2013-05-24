/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation
 *    Class:     Instruction
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/Instruction.java
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
