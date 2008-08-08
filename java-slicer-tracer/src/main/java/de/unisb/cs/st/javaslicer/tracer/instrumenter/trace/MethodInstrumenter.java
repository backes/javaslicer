package de.unisb.cs.st.javaslicer.tracer.instrumenter.trace;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import de.unisb.cs.st.javaslicer.tracer.ThreadTracer;
import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.AbstractInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ArrayInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.FieldInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.IIncInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.IntPush;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.JumpInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.LabelMarker;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.LdcInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.LookupSwitchInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.MethodInvocationInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.MultiANewArrayInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.NewArrayInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.SimpleInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.TableSwitchInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.TypeInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.VarInstruction;
import de.unisb.cs.st.javaslicer.tracer.util.IntegerMap;
import de.unisb.cs.st.javaslicer.tracer.util.Pair;

public class MethodInstrumenter extends MethodAdapter implements Opcodes {

    private final Tracer tracer;
    private final ReadMethod readMethod;

    private boolean addLabel = false;
    private final int tracerLocalVarIndex;

    private final Map<Label, Integer> labelLineNumbers = new HashMap<Label, Integer>();

    private final Map<Label, LabelMarker> labels =
        new HashMap<Label, LabelMarker>();

    private int nextLabelNr = 0;
    private int nextAdditionalLabelNr = Integer.MAX_VALUE;
    private final Map<JumpInstruction, Label> jumpInstructions = new HashMap<JumpInstruction, Label>();
    private final Map<LookupSwitchInstruction, Pair<Label, IntegerMap<Label>>> lookupSwitchInstructions
        = new HashMap<LookupSwitchInstruction, Pair<Label,IntegerMap<Label>>>();
    private final Map<TableSwitchInstruction, Pair<Label, Label[]>> tableSwitchInstructions
        = new HashMap<TableSwitchInstruction, Pair<Label,Label[]>>();

    // statistics
    private static ReadClass lastClass = null;
    private static int statsClasses = 0;
    private static int statsMethods = 0;
    private static int statsInstructions = 0;
    private static int statsLabelsStd = 0;
    private static int statsLabelsAdditional = 0;
    private static int statsArrayStore = 0;
    private static int statsArrayLoad = 0;
    private static int statsGetField = 0;
    private static int statsPutField = 0;

    public MethodInstrumenter(final MethodVisitor mv, final Tracer tracer, final ReadMethod readMethod) {
        super(mv);
        this.tracer = tracer;
        this.readMethod = readMethod;
        int localParamVars = ((readMethod.getAccess() & Opcodes.ACC_STATIC) == 0 ? 1 : 0);
        for (final Type t: Type.getArgumentTypes(readMethod.getDesc()))
            localParamVars += t.getSize();
        this.tracerLocalVarIndex = localParamVars;
        ++statsMethods;
        if (lastClass != readMethod.getReadClass()) {
            lastClass = readMethod.getReadClass();
            ++statsClasses;
        }
    }

    @Override
    public void visitCode() {
        super.visitCode();
        // initialize the tracer
        super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "getInstance",
                "()L"+Type.getInternalName(Tracer.class)+";");
        super.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread",
                "()Ljava/lang/Thread;");
        super.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Tracer.class), "getThreadTracer",
                "(Ljava/lang/Thread;)L"+Type.getInternalName(ThreadTracer.class)+";");
        super.visitVarInsn(ASTORE, this.tracerLocalVarIndex);
    }

    private void checkAddLabel() {
        if (this.addLabel) {
            this.addLabel = false;
            visitLabel(new Label(), true);
        }
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        checkAddLabel();
        final JumpInstruction jumpInstr = new JumpInstruction(this.readMethod, opcode, null);
        this.jumpInstructions.put(jumpInstr, label);
        registerInstruction(jumpInstr);
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
        checkAddLabel();
        registerInstruction(new MethodInvocationInstruction(this.readMethod, opcode, owner, name, desc));

        super.visitMethodInsn(opcode, owner, name, desc);

        // *after* the method invocation, we have to add a label, s.t. we can record when the method returns
        this.addLabel  = true;
    }

    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        // at the end, we always add a label
        this.addLabel = true;
        checkAddLabel();
        // and our new local variable
        // (the variable declaration can be skipped)
        //super.visitLocalVariable("__thread_tracer", Type.getDescriptor(ThreadTracer.class), null, this.methodStartLabel, this.methodEndLabel, this.tracerLocalVarIndex);
        super.visitMaxs(maxStack, maxLocals+1);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        // if there are labels that were not visited (should not occure...),
        // then assign them a number now
        for (final LabelMarker lm: this.labels.values()) {
            if (lm.getLabelNr() == -1) {
                final int labelNr = lm.isAdditionalLabel() ? this.nextAdditionalLabelNr-- : this.nextLabelNr++;
                lm.setLabelNr(labelNr);
            }
        }

        // now set the line numbers of the instructions of this method
        final Map<LabelMarker, Label> labelInvs = new HashMap<LabelMarker, Label>(this.labels.size()*4/3+1);
        for (final Entry<Label, LabelMarker> e: this.labels.entrySet())
            labelInvs.put(e.getValue(), e.getKey());
        final Iterator<AbstractInstruction> instrIt = this.readMethod.getInstructions().iterator();
        int line = -1;
        while (instrIt.hasNext()) {
            final AbstractInstruction instr = instrIt.next();
            if (instr instanceof LabelMarker) {
                final Integer labelLine = this.labelLineNumbers.get(labelInvs.get(instr));
                if (labelLine != null)
                    line = labelLine.intValue();
            }
            instr.setLineNumber(line);
        }

        // and set label references
        for (final Entry<JumpInstruction, Label> e: this.jumpInstructions.entrySet()) {
            final LabelMarker lm = this.labels.get(e.getValue());
            if (lm == null)
                throw new RuntimeException("Unvisited Label in JumpInstruction");
            e.getKey().setLabel(lm);
        }
        for (final Entry<LookupSwitchInstruction, Pair<Label, IntegerMap<Label>>> e: this.lookupSwitchInstructions.entrySet()) {
            final LabelMarker defLab = this.labels.get(e.getValue().getFirst());
            if (defLab == null)
                throw new RuntimeException("Unvisited Label in LookupSwitchInstruction");
            final IntegerMap<LabelMarker> handlers = new IntegerMap<LabelMarker>(e.getValue().getSecond().size()*4/3+1);
            for (final Entry<Integer, Label> e2: e.getValue().getSecond().entrySet()) {
                final LabelMarker handlerLabel = this.labels.get(e2.getValue());
                if (handlerLabel == null)
                    throw new RuntimeException("Unvisited Label in LookupSwitchInstruction");
                handlers.put(e2.getKey(), handlerLabel);
            }
            e.getKey().setDefaultHandler(defLab);
            e.getKey().setHandlers(handlers);
        }
        for (final Entry<TableSwitchInstruction, Pair<Label, Label[]>> e: this.tableSwitchInstructions.entrySet()) {
            final LabelMarker defLab = this.labels.get(e.getValue().getFirst());
            if (defLab == null)
                throw new RuntimeException("Unvisited Label in TableSwitchInstruction");
            final Label[] oldHandlers = e.getValue().getSecond();
            final LabelMarker[] handlers = new LabelMarker[oldHandlers.length];
            for (int i = 0; i < oldHandlers.length; ++i) {
                handlers[i] = this.labels.get(oldHandlers[i]);
                if (handlers[i] == null)
                    throw new RuntimeException("Unvisited Label in TableSwitchInstruction");
            }
            e.getKey().setDefaultHandler(defLab);
            e.getKey().setHandlers(handlers);
        }

        this.readMethod.ready();
        this.readMethod.setInstructionNumberEnd(AbstractInstruction.getNextIndex());
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        checkAddLabel();
        int objectTraceSeqIndex = -1;

        switch (opcode) {
        case PUTSTATIC:
        case GETSTATIC:
            // nothing is traced
            break;

        case GETFIELD:
            if (!name.startsWith("this$")) {
                // top item on stack is the object reference: duplicate it
                super.visitInsn(DUP);
                objectTraceSeqIndex = this.tracer.newObjectTraceSequence();
                ++MethodInstrumenter.statsGetField;
                //System.out.println("seq " + index + ": getField " + name + " in method " + readMethod.getReadClass().getClassName() + "." + readMethod.getName());
            }
            break;

        case PUTFIELD:
            if (!name.startsWith("this$")) {
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
                ++MethodInstrumenter.statsPutField;
                //System.out.println("seq " + index + ": putField " + name + " in method " + readMethod.getReadClass().getClassName() + "." + readMethod.getName());
            }
            break;

        default:
            break;
        }

        if (objectTraceSeqIndex != -1) {
            super.visitVarInsn(ALOAD, this.tracerLocalVarIndex);
            super.visitInsn(SWAP);
            pushIntOnStack(objectTraceSeqIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ThreadTracer.class),
                    "traceObject", "(Ljava/lang/Object;I)V");
        }

        registerInstruction(new FieldInstruction(this.readMethod, opcode, owner, name, desc, objectTraceSeqIndex));
        super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitIincInsn(final int var, final int increment) {
        checkAddLabel();
        registerInstruction(new IIncInstruction(this.readMethod, var));
        final int newVar = var < this.tracerLocalVarIndex ? var : var+1;
        super.visitIincInsn(newVar, increment);
    }

    @Override
    public void visitInsn(final int opcode) {
        checkAddLabel();
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
            super.visitVarInsn(ALOAD, this.tracerLocalVarIndex);
            super.visitInsn(SWAP);
            pushIntOnStack(indexTraceSeqIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ThreadTracer.class),
                    "traceInt", "(II)V");
            super.visitVarInsn(ALOAD, this.tracerLocalVarIndex);
            super.visitInsn(SWAP);
            pushIntOnStack(arrayTraceSeqIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ThreadTracer.class),
                    "traceObject", "(Ljava/lang/Object;I)V");
            ++MethodInstrumenter.statsArrayLoad;
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
            super.visitVarInsn(ALOAD, this.tracerLocalVarIndex);
            super.visitInsn(SWAP);
            pushIntOnStack(indexTraceSeqIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ThreadTracer.class),
                    "traceInt", "(II)V");
            super.visitVarInsn(ALOAD, this.tracerLocalVarIndex);
            super.visitInsn(SWAP);
            pushIntOnStack(arrayTraceSeqIndex);
            super.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ThreadTracer.class),
                    "traceObject", "(Ljava/lang/Object;I)V");
            ++MethodInstrumenter.statsArrayStore;
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
            registerInstruction(new ArrayInstruction(this.readMethod, opcode, arrayTraceSeqIndex, indexTraceSeqIndex));
        } else {
            assert indexTraceSeqIndex == -1;
            registerInstruction(new SimpleInstruction(this.readMethod, opcode));
        }

        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        checkAddLabel();
        if (opcode == NEWARRAY) {
            registerInstruction(new NewArrayInstruction(this.readMethod, operand));
        } else {
            assert opcode == BIPUSH || opcode == SIPUSH;
            registerInstruction(new IntPush(this.readMethod, opcode, operand));
        }

        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitLdcInsn(final Object constant) {
        checkAddLabel();
        registerInstruction(new LdcInstruction(this.readMethod, constant));
        super.visitLdcInsn(constant);
    }

    @Override
    public void visitLineNumber(final int line, final Label start) {
        this.labelLineNumbers.put(start, line);
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] handlerLabels) {
        checkAddLabel();
        final IntegerMap<Label> handlers = new IntegerMap<Label>(handlerLabels.length*4/3+1);
        assert keys.length == handlerLabels.length;
        for (int i = 0; i < handlerLabels.length; ++i)
            handlers.put(keys[i], handlerLabels[i]);
        final LookupSwitchInstruction instr = new LookupSwitchInstruction(this.readMethod, null, null);
        this.lookupSwitchInstructions.put(instr, new Pair<Label, IntegerMap<Label>>(dflt, handlers));
        registerInstruction(instr);
        super.visitLookupSwitchInsn(dflt, keys, handlerLabels);
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        checkAddLabel();
        registerInstruction(new MultiANewArrayInstruction(this.readMethod, desc, dims));
        super.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label[] handlerLabels) {
        checkAddLabel();
        assert min + handlerLabels.length - 1 == max;
        final TableSwitchInstruction instr = new TableSwitchInstruction(this.readMethod, min, max, null, null);
        this.tableSwitchInstructions.put(instr, new Pair<Label, Label[]>(dflt, handlerLabels));
        registerInstruction(instr);
        super.visitTableSwitchInsn(min, max, dflt, handlerLabels);
    }

    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        // we do not store information about try-catch blocks
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        checkAddLabel();
        registerInstruction(new TypeInstruction(this.readMethod, opcode, type));
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitVarInsn(final int opcode, final int var) {
        checkAddLabel();
        registerInstruction(new VarInstruction(this.readMethod, opcode, var));
        final int newVar = var < this.tracerLocalVarIndex ? var : var+1;
        super.visitVarInsn(opcode, newVar);
    }

    @Override
    public void visitLabel(final Label label) {
        this.addLabel = false;
        visitLabel(label, false);
    }

    @Override
    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start,
            final Label end, final int index) {
        final int newIndex = index < this.tracerLocalVarIndex ? index : index+1;
        super.visitLocalVariable(name, desc, signature, start, end, newIndex);
    }

    private void visitLabel(final Label label, final boolean additionalLabel) {
        if (!additionalLabel)
            super.visitLabel(label);
        final int seq = this.tracer.newIntegerTraceSequence();
        final int labelNr = additionalLabel ? this.nextAdditionalLabelNr-- : this.nextLabelNr++;
        final LabelMarker lm = new LabelMarker(this.readMethod, seq, additionalLabel, labelNr);
        if (!additionalLabel)
            this.labels.put(label, lm);

        // at runtime: push sequence index on the stack and call method to trace last executed instruction
        super.visitVarInsn(ALOAD, this.tracerLocalVarIndex);
        pushIntOnStack(lm.getTraceSeqIndex());
        super.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ThreadTracer.class),
                "traceLastInstructionIndex", "(I)V");

        // stats
        if (additionalLabel)
            MethodInstrumenter.statsLabelsAdditional++;
        else
            MethodInstrumenter.statsLabelsStd++;

        registerInstruction(lm);
    }

    private void registerInstruction(final AbstractInstruction instruction) {
        this.readMethod.addInstruction(instruction);
        super.visitVarInsn(ALOAD, this.tracerLocalVarIndex);
        pushIntOnStack(instruction.getIndex());
        super.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ThreadTracer.class),
                "passInstruction", "(I)V");
        ++MethodInstrumenter.statsInstructions;
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
        out.format(format, "classes", statsClasses);
        out.format(format, "methods", statsMethods);
        out.format(format, "instructions", statsInstructions);
        out.format(format, "labels", statsLabelsStd);
        out.format(format, "labels (additional)", statsLabelsAdditional);
        out.format(format, "array store", statsArrayStore);
        out.format(format, "array load", statsArrayLoad);
        out.format(format, "get field", statsGetField);
        out.format(format, "put field", statsPutField);
        out.println("----------------------------------------------------");
        out.println();
    }

}
