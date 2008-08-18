package de.unisb.cs.st.javaslicer.tracer.instrumenter;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.LazyMap;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.unisb.cs.st.javaslicer.tracer.ThreadTracer;
import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.AbstractInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.ArrayInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.FieldInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.IIncInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.IntPush;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.JumpInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.LabelMarker;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.LdcInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.LookupSwitchInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.MethodInvocationInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.MultiANewArrayInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.NewArrayInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.SimpleInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.TableSwitchInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.TypeInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.VarInstruction;
import de.unisb.cs.st.javaslicer.tracer.util.IntegerMap;
import de.unisb.cs.st.javaslicer.tracer.util.Pair;

public class TracingMethodInstrumenter implements Opcodes {

    public class FixedInstructionIterator implements ListIterator<AbstractInsnNode> {

        private final InsnList insnList;
        private ListIterator<AbstractInsnNode> iterator;

        @SuppressWarnings("unchecked")
        public FixedInstructionIterator(final InsnList insnList) {
            this.insnList = insnList;
            this.iterator = insnList.iterator();
        }

        @SuppressWarnings("unchecked")
        public void add(final AbstractInsnNode e) {
            if (this.iterator.hasNext())
                this.iterator.add(e);
            else {
                this.insnList.add(e);
                this.iterator = this.insnList.iterator(this.insnList.size());
            }
        }

        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        public boolean hasPrevious() {
            return this.iterator.hasPrevious();
        }

        public AbstractInsnNode next() {
            return this.iterator.next();
        }

        public int nextIndex() {
            return this.iterator.nextIndex();
        }

        public AbstractInsnNode previous() {
            return this.iterator.previous();
        }

        public int previousIndex() {
            return this.iterator.previousIndex();
        }

        public void remove() {
            this.iterator.remove();
        }

        public void set(final AbstractInsnNode e) {
            this.iterator.set(e);
        }

    }

    private final Tracer tracer;
    private final ReadMethod readMethod;
    private final ClassNode classNode;

    private final int tracerLocalVarIndex;

    private final Map<LabelNode, Integer> labelLineNumbers = new HashMap<LabelNode, Integer>();

    private final Map<LabelNode, LabelMarker> labels =
        new HashMap<LabelNode, LabelMarker>();

    private int nextLabelNr = 0;
    private int nextAdditionalLabelNr = Integer.MAX_VALUE;
    private final Map<JumpInstruction, LabelNode> jumpInstructions = new HashMap<JumpInstruction, LabelNode>();
    private final Map<LookupSwitchInstruction, Pair<LabelNode, IntegerMap<LabelNode>>> lookupSwitchInstructions
        = new HashMap<LookupSwitchInstruction, Pair<LabelNode,IntegerMap<LabelNode>>>();
    private final Map<TableSwitchInstruction, Pair<LabelNode, List<LabelNode>>> tableSwitchInstructions
        = new HashMap<TableSwitchInstruction, Pair<LabelNode, List<LabelNode>>>();

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
    private ListIterator<AbstractInsnNode> instructionIterator;

    public TracingMethodInstrumenter(final Tracer tracer, final ReadMethod readMethod, final ClassNode classNode) {
        this.tracer = tracer;
        this.readMethod = readMethod;
        this.classNode = classNode;
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

    @SuppressWarnings("unchecked")
    public void transform(final MethodNode method, final ListIterator<MethodNode> methodIt) {

        // do not modify abstract or native methods
        if ((method.access & ACC_ABSTRACT) != 0 || (method.access & ACC_NATIVE) != 0)
            return;

        this.instructionIterator = new FixedInstructionIterator(method.instructions);
        // in the old method, initialize the new local variable for the threadtracer
        this.instructionIterator.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Tracer.class),
                "getInstance", "()L"+Type.getInternalName(Tracer.class)+";"));
        this.instructionIterator.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Thread", "currentThread",
                "()Ljava/lang/Thread;"));
        this.instructionIterator.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Tracer.class),
                "getThreadTracer", "(Ljava/lang/Thread;)L"+Type.getInternalName(ThreadTracer.class)+";"));
        this.instructionIterator.add(new InsnNode(DUP));
        this.instructionIterator.add(new VarInsnNode(ASTORE, this.tracerLocalVarIndex));

        this.instructionIterator.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(ThreadTracer.class),
                "isPaused", "()Z"));
        final LabelNode noTracingLabel = new LabelNode();
        this.instructionIterator.add(new JumpInsnNode(IFNE, noTracingLabel));
        // create a copy of the (uninstrumented) instructions (later, while iterating through the instructions)
        final InsnList oldInstructions = new InsnList();
        final Map<LabelNode, LabelNode> labelCopies = LazyMap.decorate(
                new HashMap<LabelNode, LabelNode>(), new Factory() {
                    public Object create() {
                        return new LabelNode();
                    }
                });
        // copy the try-catch-blocks
        for (final Object o: method.tryCatchBlocks.toArray()) {
            final TryCatchBlockNode tcb = (TryCatchBlockNode) o;
            final TryCatchBlockNode newTcb = new TryCatchBlockNode(
                    labelCopies.get(tcb.start),
                    labelCopies.get(tcb.end),
                    labelCopies.get(tcb.handler),
                    tcb.type);
            method.tryCatchBlocks.add(newTcb);
        }


        // increment number of local variables by one (for the threadtracer)
        ++method.maxLocals;

        // and increment all local variable indexes after the new one by one
        for (final Object o: method.localVariables) {
            final LocalVariableNode localVar = (LocalVariableNode) o;
            if (localVar.index >= this.tracerLocalVarIndex)
                ++localVar.index;
        }

        // each method must start with a label:
        AbstractInsnNode next = null;
        if (this.instructionIterator.hasNext()) {
            next = this.instructionIterator.next();
            this.instructionIterator.previous();
        }
        boolean startLabelThere = false;
        while (next != null) {
            if (next instanceof LabelNode) {
                startLabelThere = true;
                break;
            }
            if (next instanceof FrameNode || next instanceof LineNumberNode)
                next = next.getNext();
            else
                break;
        }
        if (!startLabelThere)
            traceLabel(null);


        // then, visit the instructions that where in the method before
        while (this.instructionIterator.hasNext()) {
            final AbstractInsnNode insnNode = this.instructionIterator.next();
            switch (insnNode.getType()) {
            case AbstractInsnNode.INSN:
                transformInsn((InsnNode)insnNode);
                break;
            case AbstractInsnNode.INT_INSN:
                transformIntInsn((IntInsnNode)insnNode);
                break;
            case AbstractInsnNode.VAR_INSN:
                transformVarInsn((VarInsnNode)insnNode);
                break;
            case AbstractInsnNode.TYPE_INSN:
                transformTypeInsn((TypeInsnNode)insnNode);
                break;
            case AbstractInsnNode.FIELD_INSN:
                transformFieldInsn((FieldInsnNode)insnNode);
                break;
            case AbstractInsnNode.METHOD_INSN:
                transformMethodInsn((MethodInsnNode)insnNode);
                break;
            case AbstractInsnNode.JUMP_INSN:
                transformJumpInsn((JumpInsnNode)insnNode);
                break;
            case AbstractInsnNode.LABEL:
                transformLabel((LabelNode)insnNode);
                break;
            case AbstractInsnNode.LDC_INSN:
                transformLdcInsn((LdcInsnNode)insnNode);
                break;
            case AbstractInsnNode.IINC_INSN:
                transformIincInsn((IincInsnNode)insnNode);
                break;
            case AbstractInsnNode.TABLESWITCH_INSN:
                transformTableSwitchInsn((TableSwitchInsnNode)insnNode);
                break;
            case AbstractInsnNode.LOOKUPSWITCH_INSN:
                transformLookupSwitchInsn((LookupSwitchInsnNode)insnNode);
                break;
            case AbstractInsnNode.MULTIANEWARRAY_INSN:
                transformMultiANewArrayInsn((MultiANewArrayInsnNode)insnNode);
                break;
            case AbstractInsnNode.FRAME:
                break;
            case AbstractInsnNode.LINE:
                transformLineNumber((LineNumberNode)insnNode);
                break;
            default:
                throw new RuntimeException("Unknown instruction type " + insnNode.getType()
                        + " (" + insnNode.getClass().getSimpleName()+")");
            }
            oldInstructions.add(insnNode.clone(labelCopies));
        }

        // now add the code that is executed if no tracing should be performed
        method.instructions.add(noTracingLabel);
        method.instructions.add(oldInstructions);

        // finally: create a copy of the method that gets the ThreadTracer as argument
        // this is only necessary for private methods or "<init>"
        if (this.tracer.wasRedefined(this.readMethod.getReadClass().getName()) &&
                (method.access & ACC_PRIVATE) != 0) {
            final Type[] oldMethodArguments = Type.getArgumentTypes(method.desc);
            final Type[] newMethodArguments = Arrays.copyOf(oldMethodArguments, oldMethodArguments.length+1);
            newMethodArguments[oldMethodArguments.length] = Type.getType(ThreadTracer.class);
            final String newMethodDesc = Type.getMethodDescriptor(Type.getReturnType(method.desc), newMethodArguments);
            final MethodNode newMethod = new MethodNode(method.access, method.name, newMethodDesc,
                    method.signature, (String[]) method.exceptions.toArray(new String[method.exceptions.size()]));
            methodIt.add(newMethod);

            final Map<LabelNode, LabelNode> newMethodLabels = LazyMap.decorate(
                    new HashMap<LabelNode, LabelNode>(), new Factory() {
                        public Object create() {
                            return new LabelNode();
                        }
                    });

            // copy the local variables information to the new method
            for (final Object o: method.localVariables) {
                final LocalVariableNode lv = (LocalVariableNode) o;
                newMethod.localVariables.add(new LocalVariableNode(
                    lv.name, lv.desc, lv.signature, newMethodLabels.get(lv.start),
                    newMethodLabels.get(lv.end), lv.index
                ));
            }


            // increment number of local variables by one (for the threadtracer)
            newMethod.maxLocals = method.maxLocals;
            newMethod.maxStack = method.maxStack;

            // copy the try-catch-blocks
            for (final Object o: method.tryCatchBlocks) {
                final TryCatchBlockNode tcb = (TryCatchBlockNode) o;
                newMethod.tryCatchBlocks.add(new TryCatchBlockNode(
                        newMethodLabels.get(tcb.start),
                        newMethodLabels.get(tcb.end),
                        newMethodLabels.get(tcb.handler),
                        tcb.type));
            }

            // skip the first 7 instructions, replace them with these:
            newMethod.instructions.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
            newMethod.instructions.add(new JumpInsnNode(IFNULL, newMethodLabels.get(noTracingLabel)));
            final Iterator<AbstractInsnNode> oldInsnIt = method.instructions.iterator(7);
            // and add all the other instructions
            while (oldInsnIt.hasNext()) {
                final AbstractInsnNode insn = oldInsnIt.next();
                newMethod.instructions.add(insn.clone(newMethodLabels));
            }
        }

        ready();
    }

    private void transformJumpInsn(final JumpInsnNode insn) {
        final JumpInstruction jumpInstr = new JumpInstruction(this.readMethod, insn.getOpcode(), null);
        this.jumpInstructions.put(jumpInstr, insn.label);
        registerInstruction(jumpInstr);
    }

    private void transformMethodInsn(final MethodInsnNode insn) {
        registerInstruction(new MethodInvocationInstruction(this.readMethod, insn.getOpcode(), insn.owner, insn.name, insn.desc));

        if (this.tracer.wasRedefined(Type.getObjectType(insn.owner).getClassName())
                && (insn.owner.equals(this.classNode.name)
                    && isPrivateNotNative(insn.name, insn.desc))) {
            this.instructionIterator.previous();
            this.instructionIterator.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
            this.instructionIterator.next();
            final Type[] oldMethodArguments = Type.getArgumentTypes(insn.desc);
            final Type[] newMethodArguments = Arrays.copyOf(oldMethodArguments, oldMethodArguments.length+1);
            newMethodArguments[oldMethodArguments.length] = Type.getType(ThreadTracer.class);
            final String newDesc = Type.getMethodDescriptor(Type.getReturnType(insn.desc), newMethodArguments);
            this.instructionIterator.set(new MethodInsnNode(insn.getOpcode(), insn.owner, insn.name, newDesc));
        }

        // if the next instruction is no label, we have to add one after the instruction
        AbstractInsnNode next = insn.getNext();
        while (next != null) {
            if (next instanceof LabelNode)
                return;
            if (next instanceof FrameNode || next instanceof LineNumberNode)
                next = next.getNext();
            else
                break;
        }
        traceLabel(null);
    }

    private boolean isPrivateNotNative(final String methodName, final String methodDesc) {
        for (final Object o: this.classNode.methods) {
            final MethodNode method = (MethodNode) o;
            if (method.name.equals(methodName) && method.desc.equals(methodDesc)) {
                return ((method.access & ACC_PRIVATE) != 0) && ((method.access & ACC_NATIVE) == 0);
            }
        }
        return false;
    }

    private void ready() {
        // if there are labels that were not visited (should not occure...),
        // then assign them a number now
        for (final LabelMarker lm: this.labels.values()) {
            if (lm.getLabelNr() == -1) {
                final int labelNr = lm.isAdditionalLabel() ? this.nextAdditionalLabelNr-- : this.nextLabelNr++;
                lm.setLabelNr(labelNr);
            }
        }

        // now set the line numbers of the instructions of this method
        final Map<LabelMarker, LabelNode> labelInvs = new HashMap<LabelMarker, LabelNode>(this.labels.size()*4/3+1);
        for (final Entry<LabelNode, LabelMarker> e: this.labels.entrySet())
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
        for (final Entry<JumpInstruction, LabelNode> e: this.jumpInstructions.entrySet()) {
            final LabelMarker lm = this.labels.get(e.getValue());
            if (lm == null)
                throw new RuntimeException("Unvisited Label in JumpInstruction");
            e.getKey().setLabel(lm);
        }
        for (final Entry<LookupSwitchInstruction, Pair<LabelNode, IntegerMap<LabelNode>>> e:
                this.lookupSwitchInstructions.entrySet()) {
            final LabelMarker defLab = this.labels.get(e.getValue().getFirst());
            if (defLab == null)
                throw new RuntimeException("Unvisited Label in LookupSwitchInstruction");
            final IntegerMap<LabelMarker> handlers = new IntegerMap<LabelMarker>(e.getValue().getSecond().size()*4/3+1);
            for (final Entry<Integer, LabelNode> e2: e.getValue().getSecond().entrySet()) {
                final LabelMarker handlerLabel = this.labels.get(e2.getValue());
                if (handlerLabel == null)
                    throw new RuntimeException("Unvisited Label in LookupSwitchInstruction");
                handlers.put(e2.getKey(), handlerLabel);
            }
            e.getKey().setDefaultHandler(defLab);
            e.getKey().setHandlers(handlers);
        }
        for (final Entry<TableSwitchInstruction, Pair<LabelNode, List<LabelNode>>> e:
                this.tableSwitchInstructions.entrySet()) {
            final LabelMarker defLab = this.labels.get(e.getValue().getFirst());
            if (defLab == null)
                throw new RuntimeException("Unvisited Label in TableSwitchInstruction");
            final List<LabelNode> oldHandlers = e.getValue().getSecond();
            final LabelMarker[] handlers = new LabelMarker[oldHandlers.size()];
            for (int i = 0; i < handlers.length; ++i) {
                handlers[i] = this.labels.get(oldHandlers.get(i));
                if (handlers[i] == null)
                    throw new RuntimeException("Unvisited Label in TableSwitchInstruction");
            }
            e.getKey().setDefaultHandler(defLab);
            e.getKey().setHandlers(handlers);
        }

        this.readMethod.ready();
        this.readMethod.setInstructionNumberEnd(AbstractInstruction.getNextIndex());
    }

    private void transformFieldInsn(final FieldInsnNode insn) {
        int objectTraceSeqIndex = -1;

        switch (insn.getOpcode()) {
        case PUTSTATIC:
        case GETSTATIC:
            // nothing is traced
            break;

        case GETFIELD:
            if (!insn.name.startsWith("this$")) {
                // top item on stack is the object reference: duplicate it
                // (add instruction *before* the current one
                this.instructionIterator.previous();
                this.instructionIterator.add(new InsnNode(DUP));
                objectTraceSeqIndex = this.tracer.newObjectTraceSequence();
                ++TracingMethodInstrumenter.statsGetField;
                //System.out.println("seq " + index + ": getField " + name + " in method " + readMethod.getReadClass().getClassName() + "." + readMethod.getName());
            }
            break;

        case PUTFIELD:
            if (!insn.name.startsWith("this$")) {
                // the second item on the stack is the object reference
                // (add instruction *before* the current one
                this.instructionIterator.previous();
                final int size = Type.getType(insn.desc).getSize(); // either 1 or 2
                if (size == 1) {
                    this.instructionIterator.add(new InsnNode(DUP2));
                    this.instructionIterator.add(new InsnNode(POP));
                } else {
                    this.instructionIterator.add(new InsnNode(DUP2_X1));
                    this.instructionIterator.add(new InsnNode(POP2));
                    this.instructionIterator.add(new InsnNode(DUP_X2));
                }
                objectTraceSeqIndex = this.tracer.newObjectTraceSequence();
                ++TracingMethodInstrumenter.statsPutField;
                //System.out.println("seq " + index + ": putField " + name + " in method " + readMethod.getReadClass().getClassName() + "." + readMethod.getName());
            }
            break;

        default:
            break;
        }

        if (objectTraceSeqIndex != -1) {
            this.instructionIterator.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
            this.instructionIterator.add(new InsnNode(SWAP));
            this.instructionIterator.add(getIntConstInsn(objectTraceSeqIndex));
            this.instructionIterator.add(new MethodInsnNode(
                    INVOKEVIRTUAL, Type.getInternalName(ThreadTracer.class),
                    "traceObject", "(Ljava/lang/Object;I)V"));
            // and move to the position where it was before entering this method
            this.instructionIterator.next();
        }

        registerInstruction(new FieldInstruction(this.readMethod, insn.getOpcode(), insn.owner,
                insn.name, insn.desc, objectTraceSeqIndex));
    }

    private void transformIincInsn(final IincInsnNode insn) {
        registerInstruction(new IIncInstruction(this.readMethod, insn.var));
        if (insn.var >= this.tracerLocalVarIndex)
            ++insn.var;
    }

    private void transformInsn(final InsnNode insn) {
        int arrayTraceSeqIndex = -1;
        int indexTraceSeqIndex = -1;

        switch (insn.getOpcode()) {
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
            // (add instruction *before* the current one
            this.instructionIterator.previous();
            this.instructionIterator.add(new InsnNode(DUP2));
            ++TracingMethodInstrumenter.statsArrayLoad;
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
            // (add instruction *before* the current one
            this.instructionIterator.previous();
            if (insn.getOpcode() == LASTORE || insn.getOpcode() == DASTORE) { // 2-word values
                this.instructionIterator.add(new InsnNode(DUP2_X2));
                this.instructionIterator.add(new InsnNode(POP2));
                this.instructionIterator.add(new InsnNode(DUP2_X2));
            } else {
                this.instructionIterator.add(new InsnNode(DUP_X2));
                this.instructionIterator.add(new InsnNode(POP));
                this.instructionIterator.add(new InsnNode(DUP2_X1));
            }
            ++TracingMethodInstrumenter.statsArrayStore;
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
            // the top two words on the stack are the array index and the array reference
            this.instructionIterator.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
            this.instructionIterator.add(new InsnNode(SWAP));
            this.instructionIterator.add(getIntConstInsn(indexTraceSeqIndex));
            this.instructionIterator.add(new MethodInsnNode(INVOKEVIRTUAL,
                    Type.getInternalName(ThreadTracer.class), "traceInt", "(II)V"));
            this.instructionIterator.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
            this.instructionIterator.add(new InsnNode(SWAP));
            this.instructionIterator.add(getIntConstInsn(arrayTraceSeqIndex));
            this.instructionIterator.add(new MethodInsnNode(INVOKEVIRTUAL,
                    Type.getInternalName(ThreadTracer.class), "traceObject", "(Ljava/lang/Object;I)V"));
            // and move to the position where it was before entering this method
            this.instructionIterator.next();

            registerInstruction(new ArrayInstruction(this.readMethod, insn.getOpcode(), arrayTraceSeqIndex, indexTraceSeqIndex));
        } else {
            assert indexTraceSeqIndex == -1;
            registerInstruction(new SimpleInstruction(this.readMethod, insn.getOpcode()));
        }
    }

    private void transformIntInsn(final IntInsnNode insn) {
        if (insn.getOpcode() == NEWARRAY) {
            registerInstruction(new NewArrayInstruction(this.readMethod, insn.operand));
        } else {
            assert insn.getOpcode() == BIPUSH || insn.getOpcode() == SIPUSH;
            registerInstruction(new IntPush(this.readMethod, insn.getOpcode(), insn.operand));
        }
    }

    private void transformLdcInsn(final LdcInsnNode insn) {
        registerInstruction(new LdcInstruction(this.readMethod, insn.cst));
    }

    private void transformLineNumber(final LineNumberNode lineNumber) {
        this.labelLineNumbers.put(lineNumber.start, lineNumber.line);
    }

    private void transformLookupSwitchInsn(final LookupSwitchInsnNode insn) {
        final IntegerMap<LabelNode> handlers = new IntegerMap<LabelNode>(insn.keys.size()*4/3+1);
        assert insn.keys.size() == insn.labels.size();
        for (int i = 0; i < insn.keys.size(); ++i)
            handlers.put((Integer)insn.keys.get(i), (LabelNode)insn.labels.get(i));
        final LookupSwitchInstruction instr = new LookupSwitchInstruction(this.readMethod, null, null);
        this.lookupSwitchInstructions.put(instr, new Pair<LabelNode, IntegerMap<LabelNode>>(insn.dflt, handlers));
        registerInstruction(instr);
    }

    private void transformMultiANewArrayInsn(final MultiANewArrayInsnNode insn) {
        registerInstruction(new MultiANewArrayInstruction(this.readMethod, insn.desc, insn.dims));
    }

    @SuppressWarnings("unchecked")
    private void transformTableSwitchInsn(final TableSwitchInsnNode insn) {
        assert insn.min + insn.labels.size() - 1 == insn.max;
        final TableSwitchInstruction instr = new TableSwitchInstruction(this.readMethod, insn.min, insn.max, null, null);
        this.tableSwitchInstructions.put(instr, new Pair<LabelNode, List<LabelNode>>(insn.dflt, insn.labels));
        registerInstruction(instr);
    }

    private void transformTypeInsn(final TypeInsnNode insn) {
        registerInstruction(new TypeInstruction(this.readMethod, insn.getOpcode(), insn.desc));
    }

    private void transformVarInsn(final VarInsnNode insn) {
        registerInstruction(new VarInstruction(this.readMethod, insn.getOpcode(), insn.var));
        if (insn.var >= this.tracerLocalVarIndex)
            ++insn.var;
    }

    private void transformLabel(final LabelNode label) {
        traceLabel(label);
    }

    private void traceLabel(final LabelNode label) {
        final int seq = this.tracer.newIntegerTraceSequence();
        final boolean isAdditionalLabel = label == null;
        final int labelNr = isAdditionalLabel ? this.nextAdditionalLabelNr-- : this.nextLabelNr++;
        final LabelMarker lm = new LabelMarker(this.readMethod, seq, isAdditionalLabel, labelNr);
        if (!isAdditionalLabel)
            this.labels.put(label, lm);

        // at runtime: push sequence index on the stack and call method to trace last executed instruction
        this.instructionIterator.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
        this.instructionIterator.add(getIntConstInsn(lm.getTraceSeqIndex()));
        this.instructionIterator.add(new MethodInsnNode(INVOKEVIRTUAL,
                Type.getInternalName(ThreadTracer.class), "traceLastInstructionIndex", "(I)V"));

        // stats
        if (isAdditionalLabel)
            TracingMethodInstrumenter.statsLabelsAdditional++;
        else
            TracingMethodInstrumenter.statsLabelsStd++;

        // do not use registerInstruction, because the code has to be inserted *after* the label
        this.readMethod.addInstruction(lm);
        if (Tracer.debug) {
            this.instructionIterator.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
            this.instructionIterator.add(getIntConstInsn(lm.getIndex()));
            this.instructionIterator.add(new MethodInsnNode(INVOKEVIRTUAL,
                    Type.getInternalName(ThreadTracer.class), "passInstruction", "(I)V"));
        }
        ++TracingMethodInstrumenter.statsInstructions;
    }

    private void registerInstruction(final AbstractInstruction instruction) {
        this.readMethod.addInstruction(instruction);
        this.instructionIterator.previous();
        this.instructionIterator.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
        this.instructionIterator.add(getIntConstInsn(instruction.getIndex()));
        this.instructionIterator.add(new MethodInsnNode(INVOKEVIRTUAL,
                Type.getInternalName(ThreadTracer.class), "passInstruction", "(I)V"));
        this.instructionIterator.next();
        ++TracingMethodInstrumenter.statsInstructions;
    }

    private AbstractInsnNode getIntConstInsn(final int value) {
        switch (value) {
        case -1:
            return new InsnNode(ICONST_M1);
        case 0:
            return new InsnNode(ICONST_0);
        case 1:
            return new InsnNode(ICONST_1);
        case 2:
            return new InsnNode(ICONST_2);
        case 3:
            return new InsnNode(ICONST_3);
        case 4:
            return new InsnNode(ICONST_4);
        case 5:
            return new InsnNode(ICONST_5);
        default:
            if ((byte)value == value)
                return new IntInsnNode(BIPUSH, value);
            else if ((short)value == value)
                return new IntInsnNode(SIPUSH, value);
            else
                return new LdcInsnNode(Integer.valueOf(value));
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
