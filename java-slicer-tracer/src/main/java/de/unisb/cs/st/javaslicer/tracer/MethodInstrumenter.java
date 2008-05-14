package de.unisb.cs.st.javaslicer.tracer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ArrayInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.FieldInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.IIncInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.IntPush;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.JumpInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.LabelMarker;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.LdcInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.MethodInvocationInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.NewArrayInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.SimpleInstruction;

public class MethodInstrumenter extends MethodAdapter implements Opcodes {

    private final Tracer tracer;
    private final ReadMethod readMethod;

    private int lineNumber = -1;

    private final Label startLabel = new Label();
    private final Label endLabel = new Label();

    public MethodInstrumenter(final MethodVisitor mv, final Tracer tracer, final ReadMethod readMethod) {
        super(mv);
        this.tracer = tracer;
        this.readMethod = readMethod;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        visitLabel(this.startLabel, true);
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        registerInstruction(new JumpInstruction(this.readMethod, this.lineNumber, opcode, label));
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
        registerInstruction(new MethodInvocationInstruction(this.readMethod, this.lineNumber, opcode, owner, name, desc));

        super.visitMethodInsn(opcode, owner, name, desc);

        // *after* the method invocation, we have to add a label, s.t. we can record when the method returns
        final Label afterReturnLabel = new Label();
        visitLabel(afterReturnLabel, true);
    }

    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        // all the code is ready, so we add the endLabel here
        visitLabel(this.endLabel, true);
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        this.readMethod.ready();
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        int index = -1;
        switch (opcode) {
        case PUTSTATIC:
        case GETSTATIC:
            // nothing is traced
            break;

        case GETFIELD:
            // top item on stack is the object reference: duplicate it
            super.visitInsn(DUP);
            index = this.tracer.newObjectTraceSequence().getIndex();
            break;

        case PUTFIELD:
            // the second item on the stack is the object reference
            final int size = Type.getType(desc).getSize(); // either 1 or 2
            if (size == 1) {
                super.visitInsn(DUP2);
                super.visitInsn(POP);
            } else {
                super.visitInsn(DUP2_X1);
                super.visitInsn(POP2);
                super.visitInsn(DUP_X2);
            }
            index = this.tracer.newObjectTraceSequence().getIndex();
            break;

        default:
            break;
        }

        if (index != -1) {
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "traceObject",
                    "(Ljava/lang/Object;I)V");
        }

        registerInstruction(new FieldInstruction(this.readMethod, opcode, owner, name, desc, index));
        super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitIincInsn(final int var, final int increment) {
        registerInstruction(new IIncInstruction(this.readMethod, var));
        super.visitIincInsn(var, increment);
    }

    @Override
    public void visitInsn(final int opcode) {
        int arrayTraceIndex = -1;
        int indexTraceIndex = -1;
        switch (opcode) {
        // the not interesting ones:
        case NOP:
        // constants:
        case ACONST_NULL: case ICONST_M1: case ICONST_0: case ICONST_1: case ICONST_2: case ICONST_3: case ICONST_4:
        case ICONST_5: case LCONST_0: case LCONST_1: case FCONST_0: case FCONST_1: case FCONST_2: case DCONST_0:
        case DCONST_1:
            break;

        // array load:
        case IALOAD: case LALOAD: case FALOAD: case DALOAD: case AALOAD: case BALOAD: case CALOAD: case SALOAD:
            // to trace array manipulations, we need two traces: one for the array, one for the index
            arrayTraceIndex = this.tracer.newObjectTraceSequence().getIndex();
            indexTraceIndex = this.tracer.newIntegerTraceSequence().getIndex();
            // the top two words on the stack are the array index and the array reference
            super.visitInsn(DUP2);
            pushIntOnStack(indexTraceIndex);
            super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "traceInteger", "(II)V");
            pushIntOnStack(arrayTraceIndex);
            super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "traceObject",
                    "(Ljava/lang/Object;I)V");
            break;

        // array store:
        case IASTORE: case LASTORE: case FASTORE: case DASTORE: case AASTORE: case BASTORE: case CASTORE: case SASTORE:
            // to trace array manipulations, we need two traces: one for the array, one for the index
            arrayTraceIndex = this.tracer.newObjectTraceSequence().getIndex();
            indexTraceIndex = this.tracer.newIntegerTraceSequence().getIndex();
            // top three words on the stack: value, array index, array reference
            // after our manipulation: array index, array reference, value, array index, array reference
            if (opcode == LASTORE || opcode == DASTORE) { // 2-word values
                super.visitInsn(DUP2_X2);
                super.visitInsn(POP2);
                super.visitInsn(DUP2_X2);
            } else {
                super.visitInsn(DUP_X2);
                super.visitInsn(POP);
                super.visitInsn(DUP2_X1);
            }
            pushIntOnStack(indexTraceIndex);
            super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "traceInteger", "(II)V");
            pushIntOnStack(arrayTraceIndex);
            super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "traceObject",
                    "(Ljava/lang/Object;I)V");
            break;

        // stack manipulation:
        case POP: case POP2: case DUP: case DUP_X1: case DUP_X2: case DUP2: case DUP2_X1: case DUP2_X2: case SWAP:
            break;

        // arithmetic:
        case IADD: case LADD: case FADD: case DADD: case ISUB: case LSUB: case FSUB: case DSUB: case IMUL: case LMUL:
        case FMUL: case DMUL: case IDIV: case LDIV: case FDIV: case DDIV: case IREM: case LREM: case FREM: case DREM:
        case INEG: case LNEG: case FNEG: case DNEG: case ISHL: case LSHL: case ISHR: case LSHR: case IUSHR: case LUSHR:
        case IAND: case LAND: case IOR: case LOR: case IXOR: case LXOR:
            break;

        // type conversions:
        case I2L: case I2F: case I2D: case L2I: case L2F: case L2D: case F2I: case F2L: case F2D: case D2I: case D2L:
        case D2F: case I2B: case I2C: case I2S:
            break;

        // comparison:
        case LCMP: case FCMPL: case FCMPG: case DCMPL: case DCMPG:
            break;

        // control-flow statements:
        case IRETURN: case LRETURN: case FRETURN: case DRETURN: case ARETURN: case RETURN:
            break;

        // special things
        case ARRAYLENGTH: case ATHROW: case MONITORENTER: case MONITOREXIT:
            break;

        default:
            assert false;
        }

        if (arrayTraceIndex != -1) {
            registerInstruction(new ArrayInstruction(this.readMethod, this.lineNumber, opcode, arrayTraceIndex, indexTraceIndex));
        } else {
            registerInstruction(new SimpleInstruction(this.readMethod, this.lineNumber, opcode));
        }

        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        if (opcode == NEWARRAY) {
            registerInstruction(new NewArrayInstruction(this.readMethod, this.lineNumber, operand));
        } else {
            assert opcode == BIPUSH || opcode == SIPUSH;
            registerInstruction(new IntPush(this.readMethod, this.lineNumber, opcode, operand));
        }

        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitLdcInsn(final Object cst) {
        registerInstruction(new LdcInstruction(this.readMethod, this.lineNumber, cst));
        super.visitLdcInsn(cst);
    }

    @Override
    public void visitLineNumber(final int line, final Label start) {
        this.lineNumber = line;
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
        // TODO Auto-generated method stub
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        // TODO Auto-generated method stub
        super.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label[] labels) {
        // TODO Auto-generated method stub
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        // TODO Auto-generated method stub
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        // TODO Auto-generated method stub
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitVarInsn(final int opcode, final int var) {
        // TODO Auto-generated method stub
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitLabel(final Label label) {
        visitLabel(label, false);
    }

    @Override
    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start,
            final Label end, final int index) {
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    private void visitLabel(final Label label, final boolean additionalLabel) {
        super.visitLabel(label);

        // whenever a label is crossed, it stores the instruction index we come from
        final int index = this.tracer.newIntegerTraceSequence().getIndex();
        // at runtime: push last executed instruction index on stack, then the sequence index
        super.visitFieldInsn(GETSTATIC, Type.getInternalName(Tracer.class), "lastInstructionIndex",
                Type.INT_TYPE.getDescriptor());
        pushIntOnStack(index);
        super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "traceInteger", "(II)V");

        // and *after* that, we store our new instruction index on the stack
        registerInstruction(new LabelMarker(this.readMethod, this.lineNumber, label, index, additionalLabel));
    }

    private void registerInstruction(final Instruction instruction) {
        pushIntOnStack(instruction.getIndex());
        super.visitFieldInsn(PUTSTATIC, Type.getInternalName(Tracer.class), "lastInstructionIndex",
                Type.INT_TYPE.getDescriptor());
    }

    private void pushIntOnStack(final int index) {
        switch (index) {
        case -1:
            super.visitInsn(ICONST_M1);
            break;
        case 0:
            super.visitInsn(ICONST_0);
            break;
        case 1:
            super.visitInsn(ICONST_1);
            break;
        case 2:
            super.visitInsn(ICONST_2);
            break;
        case 3:
            super.visitInsn(ICONST_3);
            break;
        case 4:
            super.visitInsn(ICONST_4);
            break;
        case 5:
            super.visitInsn(ICONST_5);
            break;
        default:
            if ((index << 24) >> 24 == index)
                super.visitIntInsn(BIPUSH, index);
            else if ((index << 16) >> 16 == index)
                super.visitIntInsn(SIPUSH, index);
            else
                super.visitLdcInsn(index);
            break;
        }
    }

}
