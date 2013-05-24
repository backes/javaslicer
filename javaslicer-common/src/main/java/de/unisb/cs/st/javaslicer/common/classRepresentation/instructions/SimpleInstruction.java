/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation.instructions
 *    Class:     SimpleInstruction
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/instructions/SimpleInstruction.java
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
package de.unisb.cs.st.javaslicer.common.classRepresentation.instructions;

import java.io.DataInputStream;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.StringCacheInput;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod.MethodReadInformation;

/**
 * Class representing an instruction with no arguments.
 *
 * These are:
 * <ul>
 *  <li> the &quot;no operation&quot;: NOP</li>
 *  <li> instructions that push a constant onto the stack:
 *       ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5,
 *       LCONST_0, LCONST_1, FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1</li>
 *  <li> stack manipulating operations: POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2, SWAP</li>
 *  <li> arithmetic operators:
 *       IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL,
 *       FMUL, DMUL, IDIV, LDIV, FDIV, DDIV, IREM, LREM, FREM, DREM,
 *       INEG, LNEG, FNEG, DNEG, ISHL, LSHL, ISHR, LSHR, IUSHR, LUSHR,
 *       IAND, LAND, IOR, LOR, IXOR, LXOR</li>
 *  <li> type conversions:
 *       I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F, I2B, I2C, I2S</li>
 *  <li> comparisons: LCMP, FCMPL, FCMPG, DCMPL, DCMPG</li>
 *  <li> control-flow statements: IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN</li>
 *  <li> and some special instructions: ARRAYLENGTH, ATHROW, MONITORENTER, MONITOREXIT</li>
 * </ul>
 *
 * @author Clemens Hammacher
 */
public class SimpleInstruction extends AbstractInstruction {

    public SimpleInstruction(final ReadMethod readMethod, final int opcode, final int lineNumber) {
        super(readMethod, opcode, lineNumber);
    }

    private SimpleInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode, final int index) {
        super(readMethod, opcode, lineNumber, index);
    }

    public static SimpleInstruction readFrom(@SuppressWarnings("unused") final DataInputStream in,
            final MethodReadInformation methodInfo,
            @SuppressWarnings("unused") final StringCacheInput stringCache,
            final int opcode, final int index, final int lineNumber) {
        return new SimpleInstruction(methodInfo.getMethod(), lineNumber, opcode, index);
    }

    @Override
	public InstructionType getType() {
        return InstructionType.SIMPLE;
    }

    @Override
    public String toString() {
        switch (getOpcode()) {
        // the not interesting ones:
        case Opcodes.NOP:
            return "NOP";
            // constants:
        case Opcodes.ACONST_NULL:
            return "ACONST_NULL";
        case Opcodes.ICONST_M1:
            return "ICONST_M1";
        case Opcodes.ICONST_0:
            return "ICONST_0";
        case Opcodes.ICONST_1:
            return "ICONST_1";
        case Opcodes.ICONST_2:
            return "ICONST_2";
        case Opcodes.ICONST_3:
            return "ICONST_3";
        case Opcodes.ICONST_4:
            return "ICONST_4";
        case Opcodes.ICONST_5:
            return "ICONST_5";
        case Opcodes.LCONST_0:
            return "LCONST_0";
        case Opcodes.LCONST_1:
            return "LCONST_1";
        case Opcodes.FCONST_0:
            return "FCONST_0";
        case Opcodes.FCONST_1:
            return "FCONST_1";
        case Opcodes.FCONST_2:
            return "FCONST_2";
        case Opcodes.DCONST_0:
            return "DCONST_0";
        case Opcodes.DCONST_1:
            return "DCONST_1";

            // array load:
        case Opcodes.IALOAD:
            return "IALOAD";
        case Opcodes.LALOAD:
            return "LALOAD";
        case Opcodes.FALOAD:
            return "FALOAD";
        case Opcodes.DALOAD:
            return "DALOAD";
        case Opcodes.AALOAD:
            return "AALOAD";
        case Opcodes.BALOAD:
            return "BALOAD";
        case Opcodes.CALOAD:
            return "CALOAD";
        case Opcodes.SALOAD:
            return "SALOAD";

            // array store:
        case Opcodes.IASTORE:
            return "IASTORE";
        case Opcodes.LASTORE:
            return "LASTORE";
        case Opcodes.FASTORE:
            return "FASTORE";
        case Opcodes.DASTORE:
            return "DASTORE";
        case Opcodes.AASTORE:
            return "AASTORE";
        case Opcodes.BASTORE:
            return "BASTORE";
        case Opcodes.CASTORE:
            return "CASTORE";
        case Opcodes.SASTORE:
            return "SASTORE";

            // stack manipulation:
        case Opcodes.POP:
            return "POP";
        case Opcodes.POP2:
            return "POP2";
        case Opcodes.DUP:
            return "DUP";
        case Opcodes.DUP_X1:
            return "DUP_X1";
        case Opcodes.DUP_X2:
            return "DUP_X2";
        case Opcodes.DUP2:
            return "DUP2";
        case Opcodes.DUP2_X1:
            return "DUP2_X1";
        case Opcodes.DUP2_X2:
            return "DUP2_X2";
        case Opcodes.SWAP:
            return "SWAP";

            // arithmetic:
        case Opcodes.IADD:
            return "IADD";
        case Opcodes.LADD:
            return "LADD";
        case Opcodes.FADD:
            return "FADD";
        case Opcodes.DADD:
            return "DADD";
        case Opcodes.ISUB:
            return "ISUB";
        case Opcodes.LSUB:
            return "LSUB";
        case Opcodes.FSUB:
            return "FSUB";
        case Opcodes.DSUB:
            return "DSUB";
        case Opcodes.IMUL:
            return "IMUL";
        case Opcodes.LMUL:
            return "LMUL";
        case Opcodes.FMUL:
            return "FMUL";
        case Opcodes.DMUL:
            return "DMUL";
        case Opcodes.IDIV:
            return "IDIV";
        case Opcodes.LDIV:
            return "LDIV";
        case Opcodes.FDIV:
            return "FDIV";
        case Opcodes.DDIV:
            return "DDIV";
        case Opcodes.IREM:
            return "IREM";
        case Opcodes.LREM:
            return "LREM";
        case Opcodes.FREM:
            return "FREM";
        case Opcodes.DREM:
            return "DREM";
        case Opcodes.INEG:
            return "INEG";
        case Opcodes.LNEG:
            return "LNEG";
        case Opcodes.FNEG:
            return "FNEG";
        case Opcodes.DNEG:
            return "DNEG";
        case Opcodes.ISHL:
            return "ISHL";
        case Opcodes.LSHL:
            return "LSHL";
        case Opcodes.ISHR:
            return "ISHR";
        case Opcodes.LSHR:
            return "LSHR";
        case Opcodes.IUSHR:
            return "IUSHR";
        case Opcodes.LUSHR:
            return "LUSHR";
        case Opcodes.IAND:
            return "IAND";
        case Opcodes.LAND:
            return "LAND";
        case Opcodes.IOR:
            return "IOR";
        case Opcodes.LOR:
            return "LOR";
        case Opcodes.IXOR:
            return "IXOR";
        case Opcodes.LXOR:
            return "LXOR";

            // type conversions:
        case Opcodes.I2L:
            return "I2L";
        case Opcodes.I2F:
            return "I2F";
        case Opcodes.I2D:
            return "I2D";
        case Opcodes.L2I:
            return "L2I";
        case Opcodes.L2F:
            return "L2F";
        case Opcodes.L2D:
            return "L2D";
        case Opcodes.F2I:
            return "F2I";
        case Opcodes.F2L:
            return "F2L";
        case Opcodes.F2D:
            return "F2D";
        case Opcodes.D2I:
            return "D2I";
        case Opcodes.D2L:
            return "D2L";
        case Opcodes.D2F:
            return "D2F";
        case Opcodes.I2B:
            return "I2B";
        case Opcodes.I2C:
            return "I2C";
        case Opcodes.I2S:
            return "I2S";

            // comparison:
        case Opcodes.LCMP:
            return "LCMP";
        case Opcodes.FCMPL:
            return "FCMPL";
        case Opcodes.FCMPG:
            return "FCMPG";
        case Opcodes.DCMPL:
            return "DCMPL";
        case Opcodes.DCMPG:
            return "DCMPG";

            // control-flow statements:
        case Opcodes.IRETURN:
            return "IRETURN";
        case Opcodes.LRETURN:
            return "LRETURN";
        case Opcodes.FRETURN:
            return "FRETURN";
        case Opcodes.DRETURN:
            return "DRETURN";
        case Opcodes.ARETURN:
            return "ARETURN";
        case Opcodes.RETURN:
            return "RETURN";

            // special things
        case Opcodes.ARRAYLENGTH:
            return "ARRAYLENGTH";
        case Opcodes.ATHROW:
            return "ATHROW";
        case Opcodes.MONITORENTER:
            return "MONITORENTER";
        case Opcodes.MONITOREXIT:
            return "MONITOREXIT";

        default:
            assert false;
            return "--ERROR--";
        }
    }

}
