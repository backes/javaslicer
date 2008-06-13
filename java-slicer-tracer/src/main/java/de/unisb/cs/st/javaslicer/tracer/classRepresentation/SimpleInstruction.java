package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

public class SimpleInstruction extends AbstractInstruction {

    public SimpleInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode) {
        super(readMethod, opcode, lineNumber);
    }

    public static SimpleInstruction readFrom(final DataInput in, final ReadMethod readMethod, final int opcode,
            final int index, final int lineNumber) throws IOException {
        return new SimpleInstruction(readMethod, lineNumber, opcode);
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
            return "DUT";
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
