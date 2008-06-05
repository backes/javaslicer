package de.unisb.cs.st.javaslicer.tracer;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ArrayInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.FieldInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.IIncInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.AbstractInstruction;
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
    private final Map<Label, LabelMarker> labels =
        new HashMap<Label, LabelMarker>();
    private final Map<JumpInstruction, Label> jumpInstructions =
        new HashMap<JumpInstruction, Label>();

    // statistics
    private static int labelsAdditional = 0;
    private static int labelsStd = 0;
    private static int instructions = 0;
    private static int arrayStore = 0;
    private static int arrayLoad = 0;
    private static int getField = 0;
    private static int putField = 0;

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
        final JumpInstruction instr = new JumpInstruction(this.readMethod, this.lineNumber, opcode, null);
        registerInstruction(instr);
        this.jumpInstructions.put(instr, label);
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
        this.readMethod.setInstructionNumberEnd(AbstractInstruction.getNextIndex());
        for (final Entry<JumpInstruction, Label> instr: this.jumpInstructions.entrySet()) {
            final LabelMarker lm = this.labels.get(instr.getValue());
            assert lm != null;
            instr.getKey().setLabel(lm);
        }
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        int objectTraceSeqIndex = -1;

        switch (opcode) {
        case PUTSTATIC:
        case GETSTATIC:
            // nothing is traced
            break;

        case GETFIELD:
            // top item on stack is the object reference: duplicate it
            super.visitInsn(DUP);
            objectTraceSeqIndex = this.tracer.newObjectTraceSequence();
            ++MethodInstrumenter.getField;
            //System.out.println("seq " + index + ": getField " + name + " in method " + readMethod.getReadClass().getClassName() + "." + readMethod.getName());
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
            objectTraceSeqIndex = this.tracer.newObjectTraceSequence();
            ++MethodInstrumenter.putField;
            //System.out.println("seq " + index + ": putField " + name + " in method " + readMethod.getReadClass().getClassName() + "." + readMethod.getName());
            break;

        default:
            break;
        }

        if (objectTraceSeqIndex != -1) {
            pushIntOnStack(objectTraceSeqIndex);
            super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "traceObject",
                    "(Ljava/lang/Object;I)V");
        }

        registerInstruction(new FieldInstruction(this.readMethod, opcode, this.lineNumber, owner, name, desc, objectTraceSeqIndex));
        super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitIincInsn(final int var, final int increment) {
        registerInstruction(new IIncInstruction(this.readMethod, var, this.lineNumber));
        super.visitIincInsn(var, increment);
    }

    @Override
    public void visitInsn(final int opcode) {
        int arrayTraceSeqIndex = -1;
        int indexTraceSeqIndex = -1;

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
            arrayTraceSeqIndex = this.tracer.newObjectTraceSequence();
            indexTraceSeqIndex = this.tracer.newIntegerTraceSequence();
            //System.out.println("seq " + arrayTraceIndex + ": array in method " + readMethod.getReadClass().getClassName() + "." + readMethod.getName());
            //System.out.println("seq " + indexTraceIndex + ": array index in method " + readMethod.getReadClass().getClassName() + "." + readMethod.getName());
            // the top two words on the stack are the array index and the array reference
            super.visitInsn(DUP2);
            pushIntOnStack(indexTraceSeqIndex);
            super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "traceInteger", "(II)V");
            pushIntOnStack(arrayTraceSeqIndex);
            super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "traceObject",
                    "(Ljava/lang/Object;I)V");
            ++MethodInstrumenter.arrayLoad;
            break;

        // array store:
        case IASTORE: case LASTORE: case FASTORE: case DASTORE: case AASTORE: case BASTORE: case CASTORE: case SASTORE:
            // to trace array manipulations, we need two traces: one for the array, one for the index
            arrayTraceSeqIndex = this.tracer.newObjectTraceSequence();
            indexTraceSeqIndex = this.tracer.newIntegerTraceSequence();
            //System.out.println("seq " + arrayTraceIndex + ": array in method " + readMethod.getReadClass().getClassName() + "." + readMethod.getName());
            //System.out.println("seq " + indexTraceIndex + ": arrayindex in method " + readMethod.getReadClass().getClassName() + "." + readMethod.getName());
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
            pushIntOnStack(indexTraceSeqIndex);
            super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "traceInteger", "(II)V");
            pushIntOnStack(arrayTraceSeqIndex);
            super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "traceObject",
                    "(Ljava/lang/Object;I)V");
            ++MethodInstrumenter.arrayStore;
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

        if (arrayTraceSeqIndex != -1) {
            assert indexTraceSeqIndex != -1;
            registerInstruction(new ArrayInstruction(this.readMethod, this.lineNumber, opcode, arrayTraceSeqIndex, indexTraceSeqIndex));
        } else {
            assert indexTraceSeqIndex == -1;
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
    public void visitLdcInsn(final Object constant) {
        registerInstruction(new LdcInstruction(this.readMethod, this.lineNumber, constant));
        super.visitLdcInsn(constant);
    }

    @Override
    public void visitLineNumber(final int line, final Label start) {
        this.lineNumber = line;
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] handlerLabels) {
        // TODO Auto-generated method stub
        super.visitLookupSwitchInsn(dflt, keys, handlerLabels);
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        // TODO Auto-generated method stub
        super.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label[] handlerLabels) {
        // TODO Auto-generated method stub
        super.visitTableSwitchInsn(min, max, dflt, handlerLabels);
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
        final int seq = this.tracer.newIntegerTraceSequence();
        final LabelMarker lm = new LabelMarker(this.readMethod, this.lineNumber, seq, additionalLabel);
        this.labels.put(label, lm);
        //System.out.println("seq " + index + ": label " + label + " in method " + readMethod.getReadClass().getClassName() + "." + readMethod.getName());
        // at runtime: push last executed instruction index on stack, then the sequence index
        super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "getLastInstructionIndex",
                "()I");
        pushIntOnStack(seq);
        super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "traceInteger", "(II)V");

        // stats
        if (additionalLabel)
            MethodInstrumenter.labelsAdditional++;
        else
            MethodInstrumenter.labelsStd++;

        // and *after* that, we store our new instruction index on the stack (on runtime)
        registerInstruction(lm);
    }

    private void registerInstruction(final AbstractInstruction instruction) {
        this.readMethod.addInstruction(instruction);
        pushIntOnStack(instruction.getIndex());
        super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "setLastInstructionIndex", "(I)V");
        ++MethodInstrumenter.instructions;
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
            if ((byte)index == index)
                super.visitIntInsn(BIPUSH, index);
            else if ((short)index == index)
                super.visitIntInsn(SIPUSH, index);
            else
                super.visitLdcInsn(index);
            break;
        }
    }

    public static void printStats(final PrintStream out) {
        out.println();
        out.println("----------------------------------------------------");
        final String format = "%-20s%10d%n";
        out.println("Instrumentation statistics:");
        out.format(format, "instructions", instructions);
        out.format(format, "labels", labelsStd);
        out.format(format, "labels (additional)", labelsAdditional);
        out.format(format, "array store", arrayStore);
        out.format(format, "array load", arrayLoad);
        out.format(format, "get field", getField);
        out.format(format, "put field", putField);
        out.println("----------------------------------------------------");
        out.println();
    }

}
