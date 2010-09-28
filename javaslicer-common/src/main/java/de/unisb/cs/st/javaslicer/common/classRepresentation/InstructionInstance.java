/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation
 *    Class:     InstructionInstance
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/InstructionInstance.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.common.classRepresentation;

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
     * Returns additonal information which corresponds to this {@link InstructionInstance}.
     *
     * The kind of information depends of the type of the corresponding instruction
     * (<code>getInstruction().getType()</code>):
     * <ul>
     * <li>for {@link InstructionType#ARRAY}, it's an {@link ArrayInstrInstanceInfo}</li>
     * <li>for {@link InstructionType#FIELD}, it's an {@link FieldInstrInstanceInfo}</li>
     * <li>for {@link InstructionType#NEWARRAY}, it's an {@link NewArrayInstrInstanceInfo}</li>
     * <li>for {@link InstructionType#MULTIANEWARRAY}, it's an {@link MultiANewArrayInstrInstanceInfo}</li>
     * <li>for {@link InstructionType#TYPE}, it's an {@link TypeInstrInstanceInfo}</li>
     * <li>for the rest, this method returns <code>null</code></li>
     * </ul>
     *
     * @return additional information for this InstructionInstance
     */
    InstructionInstanceInfo getAdditionalInfo();

}
