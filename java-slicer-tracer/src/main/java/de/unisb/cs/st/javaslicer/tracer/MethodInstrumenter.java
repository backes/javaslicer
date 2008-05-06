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

    private int localVariableIndex = 0;
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
        // it's not necessary to record these predicated. If a branch is taken, the label where we jump to noticed that.
        /*
        int index = -1;
        switch (opcode) {
        // one operand:
        case IFEQ:
            index = this.tracer.newBooleanTraceSequence().getIndex();
            super.visitInsn(DUP);
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceIntEqZero", "(II)V");
            break;
        case IFNE:
            index = this.tracer.newBooleanTraceSequence().getIndex();
            super.visitInsn(DUP);
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceIntNeqZero", "(II)V");
            break;
        case IFLT:
            index = this.tracer.newBooleanTraceSequence().getIndex();
            super.visitInsn(DUP);
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceIntLtZero", "(II)V");
            break;
        case IFGE:
            index = this.tracer.newBooleanTraceSequence().getIndex();
            super.visitInsn(DUP);
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceIntGeZero", "(II)V");
            break;
        case IFGT:
            index = this.tracer.newBooleanTraceSequence().getIndex();
            super.visitInsn(DUP);
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceIntGtZero", "(II)V");
            break;
        case IFLE:
            index = this.tracer.newBooleanTraceSequence().getIndex();
            super.visitInsn(DUP);
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceIntLeZero", "(II)V");
            break;
        case IFNULL:
            index = this.tracer.newBooleanTraceSequence().getIndex();
            super.visitInsn(DUP);
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceObjIsNull", "(Ljava/lang/Object;I)V");
            break;
        case IFNONNULL:
            index = this.tracer.newBooleanTraceSequence().getIndex();
            super.visitInsn(DUP);
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceObjIsNotNull", "(Ljava/lang/Object;I)V");
            break;

        // two operands:
        case IF_ICMPEQ:
            index = this.tracer.newBooleanTraceSequence().getIndex();
            super.visitInsn(DUP2);
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceIntCmpEq", "(III)V");
            break;
        case IF_ICMPNE:
            index = this.tracer.newBooleanTraceSequence().getIndex();
            super.visitInsn(DUP2);
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceIntCmpNeq", "(III)V");
            break;
        case IF_ICMPLT:
            index = this.tracer.newBooleanTraceSequence().getIndex();
            super.visitInsn(DUP2);
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceIntCmpLt", "(III)V");
            break;
        case IF_ICMPGE:
            index = this.tracer.newBooleanTraceSequence().getIndex();
            super.visitInsn(DUP2);
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceIntCmpGe", "(III)V");
            break;
        case IF_ICMPGT:
            index = this.tracer.newBooleanTraceSequence().getIndex();
            super.visitInsn(DUP2);
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceIntCmpGt", "(III)V");
            break;
        case IF_ICMPLE:
            index = this.tracer.newBooleanTraceSequence().getIndex();
            super.visitInsn(DUP2);
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceIntCmpLe", "(III)V");
            break;
        case IF_ACMPEQ:
            index = this.tracer.newBooleanTraceSequence().getIndex();
            super.visitInsn(DUP2);
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceObjCmpEq", "(Ljava/lang/Object;Ljava/lang/Object;I)V");
            break;
        case IF_ACMPNE:
            index = this.tracer.newBooleanTraceSequence().getIndex();
            super.visitInsn(DUP2);
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceObjCmpNeq", "(Ljava/lang/Object;Ljava/lang/Object;I)V");
            break;

        // zero operands (not interesting, static):
        case GOTO:
        case JSR:
            break;

        default:
            assert false;
        }
        */

        registerInstruction(new JumpInstruction(this.readMethod, this.lineNumber, opcode, label));

        super.visitJumpInsn(opcode, label);
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
            super.visitIntInsn(getIntPushOpcode(index), index);
            break;
        }
    }

    private int getIntPushOpcode(final int index) {
        if ((index & 255) == index)
            return BIPUSH;
        if ((index & 65535) == index)
            return SIPUSH;
        return LDC;
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
        // it's not necessary to record the class of the object whose method is called,
        // because we get the called method when it's start label is crossed!
        /*
        int index = -1;
        switch (opcode) {
        // static invocations are not interesting
        case INVOKESTATIC:
        case INVOKESPECIAL:
            break;

        case INVOKEINTERFACE:
        case INVOKEVIRTUAL:
            // get the number of argument words (i.e. a double has two words)
            final int argWords = getArgumentWords(desc);
            if (argWords == 0) {
                // just duplicate the object reference
                super.visitInsn(DUP);
            } else if (argWords == 1) {
                // duplicate object reference and argument, then drop the argument
                super.visitInsn(DUP2);
                super.visitInsn(POP);
            } else if (argWords == 2) {
                // copy the two arguments under the object reference, then pop them.
                // at the end, just copy the object reference under the two arguments.
                super.visitInsn(DUP2_X1);
                super.visitInsn(POP2);
                super.visitInsn(DUP_X2);
            } else if (argWords == 3) {
                // copy the first two arguments under the object reference, then pop them.
                super.visitInsn(DUP2_X2);
                super.visitInsn(POP2);
                // copy third argument and object reference under the first two
                super.visitInsn(DUP2_X2);
                // pop the third argument
                super.visitInsn(POP);
            } else {
                copyObjectReference(owner, desc);
            }

            // now that the object reference is copied to the top of the stack, we just record it
            index = this.tracer.newClassTraceSequence().getIndex();
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods", "traceClass",
                    "(Ljava/lang/Object;I)V");
            break;

        default:
            assert false;
        }
        */

        registerInstruction(new MethodInvocationInstruction(this.readMethod, this.lineNumber, opcode, owner, name, desc));

        super.visitMethodInsn(opcode, owner, name, desc);

        // after the method invokation, we have to add a label, s.t. we can record when the method returns
        final Label afterReturnLabel = new Label();
        visitLabel(afterReturnLabel , true);
    }

    private int getArgumentWords(final String methodDesc) {
        final Type[] types = Type.getArgumentTypes(methodDesc);
        int size = 0;
        for (final Type type : types) {
            size += type.getSize();
        }
        return size;
    }

    private void copyObjectReference(final String methodOwner, final String methodDesc) {
        final Label copyStartLabel = new Label();
        final Label copyEndLabel = new Label();
        super.visitLabel(copyStartLabel);
        final Type[] types = Type.getArgumentTypes(methodDesc);
        final int startVarIndex = this.localVariableIndex;
        this.localVariableIndex += types.length;
        // the arguments are on the stack in reverse order!
        for (int i = types.length - 1; i >= 0; --i) {
            final int varIndex = startVarIndex + i;
            super.visitLocalVariable("__trace_argCopy" + varIndex, types[i].getDescriptor(), null, copyStartLabel, copyEndLabel,
                    varIndex);
            super.visitVarInsn(types[i].getOpcode(ISTORE), varIndex);
        }
        // now copy and save the object reference
        final int objRefIndex = this.localVariableIndex++;
        super.visitLocalVariable("__trace_argCopy" + objRefIndex, methodOwner, null, copyStartLabel, copyEndLabel, objRefIndex);
        super.visitInsn(DUP);
        super.visitVarInsn(ASTORE, objRefIndex);

        // half the way is done: now we have to reconstruct the arguments
        for (int i = 0; i < types.length; ++i) {
            final int varIndex = startVarIndex + i;
            super.visitVarInsn(types[i].getOpcode(ILOAD), varIndex);
        }

        // and at least push the stored object reference on the stack
        super.visitVarInsn(ALOAD, objRefIndex);

        super.visitLabel(copyEndLabel);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        super.visitLabel(this.endLabel);
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
            super.visitInsn(DUP2);
            super.visitInsn(POP);
            index = this.tracer.newObjectTraceSequence().getIndex();
            break;

        default:
            break;
        }

        if (index != -1) {
            pushIntOnStack(index);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceInteger", "(II)V");
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
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceInteger", "(II)V");
            pushIntOnStack(arrayTraceIndex);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceObject", "(Ljava/lang/Object;I)V");
            break;

        // array store:
        case IASTORE: case LASTORE: case FASTORE: case DASTORE: case AASTORE: case BASTORE: case CASTORE: case SASTORE:
            // to trace array manipulations, we need two traces: one for the array, one for the index
            arrayTraceIndex = this.tracer.newObjectTraceSequence().getIndex();
            indexTraceIndex = this.tracer.newIntegerTraceSequence().getIndex();
            // top three words on the stack: value, array index, array reference
            // after our manipulation: array index, array reference, value, array index, array reference
            super.visitInsn(DUP_X2);
            super.visitInsn(POP);
            super.visitInsn(DUP2_X1);
            pushIntOnStack(indexTraceIndex);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceInteger", "(II)V");
            pushIntOnStack(arrayTraceIndex);
            super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                    "traceObject", "(Ljava/lang/Object;I)V");
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

    private void visitLabel(final Label label, final boolean additionalLabel) {
        super.visitLabel(label);

        // whenever a label is crossed, it stores the instruction index we come from
        final int index = this.tracer.newIntegerTraceSequence().getIndex();
        // push last executed instruction index on stack
        super.visitFieldInsn(GETSTATIC, Type.getInternalName(Tracer.class), "lastInstructionIndex",
                Type.INT_TYPE.getDescriptor());
        pushIntOnStack(index);
        super.visitMethodInsn(INVOKESTATIC, "de/unisb/cs/st/javaslicer/tracer/SimpleTracerMethods",
                "traceInteger", "(II)V");
        // and *after* that, we store our new instruction index on the stack
        registerInstruction(new LabelMarker(this.readMethod, this.lineNumber, label, index, additionalLabel));
    }

    @Override
    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start,
            final Label end, final int index) {
        super.visitLocalVariable(name, desc, signature, start, end, this.localVariableIndex++);
    }

}
