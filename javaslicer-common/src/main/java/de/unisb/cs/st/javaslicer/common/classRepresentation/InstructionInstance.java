package de.unisb.cs.st.javaslicer.common.classRepresentation;

/**
 * Represents a (dynamic) instance of an {@link Instruction}.
 *
 * @author Clemens Hammacher
 */
public interface InstructionInstance {

    /**
     * Returns the Instruction that this InstructionInstance is an instance of.
     * @return the Instruction of this instance
     */
    Instruction getInstruction();

    /**
     * Returns the number of this instance. In backward traversal, it counts upwards from 0 on,
     * in forward traversal reversed, so that the numbering is always consistent.
     *
     * @return the number of this instance
     */
    long getInstanceNr();

    /**
     * Returns the occurrence number of this instance.
     * It always counts upwards from 0 on, so the numbering is in reverse order
     * when traversing a trace backwards.
     *
     * @return the occurrence number of this instance
     */
    long getOccurrenceNumber();

    /**
     * Returns the stack depth at which this instance occurred.
     * The stack depth is always > 0.
     *
     * @return the stack depth at which this instance occurred
     */
    int getStackDepth();

}
