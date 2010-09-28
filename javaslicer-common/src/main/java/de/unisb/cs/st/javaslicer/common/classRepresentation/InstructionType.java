/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation
 *    Class:     InstructionType
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/InstructionType.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.common.classRepresentation;

import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.ArrayInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.FieldInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.IIncInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.IntPush;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.JumpInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LdcInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LookupSwitchInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MethodInvocationInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MultiANewArrayInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.NewArrayInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.SimpleInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.TableSwitchInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.TypeInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.VarInstruction;

/**
 * Used to identify the type of instructions. Should be preferred to multiple instanceof statements.
 *
 * @author Clemens Hammacher
 */
public enum InstructionType {
    /**
     * the type of {@link ArrayInstruction}s.
     */
    ARRAY,
    /**
     * the type of {@link FieldInstruction}s.
     */
    FIELD,
    /**
     * the type of {@link IIncInstruction}s.
     */
    IINC,
    /**
     * the type of {@link IntPush} instructions.
     */
    INT,
    /**
     * the type of {@link JumpInstruction}s.
     */
    JUMP,
    /**
     * the type of {@link LabelMarker}s.
     */
    LABEL,
    /**
     * the type of {@link LdcInstruction}s.
     */
    LDC,
    /**
     * the type of {@link LookupSwitchInstruction}s.
     */
    LOOKUPSWITCH,
    /**
     * the type of {@link MethodInvocationInstruction}s.
     */
    METHODINVOCATION,
    /**
     * the type of {@link MultiANewArrayInstruction}s.
     */
    MULTIANEWARRAY,
    /**
     * the type of {@link NewArrayInstruction}s.
     */
    NEWARRAY,
    /**
     * the type of {@link SimpleInstruction}s.
     */
    SIMPLE,
    /**
     * the type of {@link TableSwitchInstruction}s.
     */
    TABLESWITCH,
    /**
     * the type of {@link TypeInstruction}s.
     */
    TYPE,
    /**
     * the type of {@link VarInstruction}s.
     */
    VAR
}
