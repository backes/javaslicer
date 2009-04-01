package de.unisb.cs.st.javaslicer.common.classRepresentation;

import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.ArrayInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.FieldInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MultiANewArrayInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.NewArrayInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.TypeInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.ArrayInstruction.ArrayInstrInstanceInfo;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.FieldInstruction.FieldInstrInstanceInfo;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MultiANewArrayInstruction.MultiANewArrayInstrInstanceInfo;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.NewArrayInstruction.NewArrayInstrInstanceInfo;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.TypeInstruction.TypeInstrInstanceInfo;


/**
 * Represents a (dynamic) instance of an {@link Instruction}.
 *
 * @author Clemens Hammacher
 */
public interface InstructionInstance extends Comparable<InstructionInstance> {

    public enum InstructionInstanceType {

        /**
         * The type for most instructions.
         * {@link InstructionInstance#getAdditionalInfo()} returns <code>null</code>.
         */
        DEFAULT,

        /**
         * The type for {@link ArrayInstruction}s.
         * {@link InstructionInstance#getAdditionalInfo()} returns an {@link ArrayInstrInstanceInfo}.
         */
        ARRAY,

        /**
         * The type for {@link FieldInstruction}s.
         * {@link InstructionInstance#getAdditionalInfo()} returns an {@link FieldInstrInstanceInfo}.
         */
        FIELD,

        /**
         * The type for {@link NewArrayInstruction}s.
         * {@link InstructionInstance#getAdditionalInfo()} returns an {@link NewArrayInstrInstanceInfo}.
         */
        NEWARRAY,

        /**
         * The type for {@link MultiANewArrayInstruction}s.
         * {@link InstructionInstance#getAdditionalInfo()} returns an {@link MultiANewArrayInstrInstanceInfo}.
         */
        MULTIANEWARRAY,

        /**
         * The type for {@link TypeInstruction}s.
         * {@link InstructionInstance#getAdditionalInfo()} returns an {@link TypeInstrInstanceInfo}.
         */
        TYPE;

    }

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

    /**
     * Returns the type of this instruction instance.
     * The type also determines the kind of information which is returned
     * by {@link #getAdditionalInfo()}.
     *
     * @return the type of this instruction instance
     */
    InstructionInstanceType getType();

    /**
     * Returns additonal information which corresponds to this {@link InstructionInstance}.
     *
     * @return additional information for this InstructionInstance
     */
    InstructionInstanceInfo getAdditionalInfo();

}
