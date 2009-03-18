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

    int getBackwardInstructionIndex(final TraceIterationInformationProvider infoProv);

    // for internal use only!
    InstructionInstance getNextInstance(final TraceIterationInformationProvider infoProv, int stackDepth, long instanceNr) throws TracerException;

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
