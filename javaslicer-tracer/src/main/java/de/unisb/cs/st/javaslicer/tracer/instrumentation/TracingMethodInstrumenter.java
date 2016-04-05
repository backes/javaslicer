/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer.instrumentation
 *    Class:     TracingMethodInstrumenter
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/instrumentation/TracingMethodInstrumenter.java
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
package de.unisb.cs.st.javaslicer.tracer.instrumentation;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

import de.hammacher.util.Pair;
import de.hammacher.util.maps.IntegerMap;
import de.unisb.cs.st.javaslicer.common.classRepresentation.LocalVariable;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.TryCatchBlock;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.AbstractInstruction;
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
import de.unisb.cs.st.javaslicer.common.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.ThreadTracer;
import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.TracingThreadTracer;

public class TracingMethodInstrumenter implements Opcodes {

    public static class FixedInstructionIterator implements ListIterator<AbstractInsnNode> {

        private final InsnList insnList;
        private ListIterator<AbstractInsnNode> iterator;

        @SuppressWarnings("unchecked")
        public FixedInstructionIterator(final InsnList insnList) {
            this.insnList = insnList;
            this.iterator = insnList.iterator();
        }

        @SuppressWarnings("unchecked")
        public FixedInstructionIterator(final InsnList insnList, final int index) {
            this.insnList = insnList;
            this.iterator = insnList.iterator(index);
        }

        @Override
		@SuppressWarnings("unchecked")
        public void add(final AbstractInsnNode e) {
            if (this.iterator.hasNext())
                this.iterator.add(e);
            else {
                this.insnList.add(e);
                this.iterator = this.insnList.iterator(this.insnList.size());
            }
        }

        @Override
		public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
		public boolean hasPrevious() {
            return this.iterator.hasPrevious();
        }

        @Override
		public AbstractInsnNode next() {
            return this.iterator.next();
        }

        @Override
		public int nextIndex() {
            return this.iterator.nextIndex();
        }

        @Override
		public AbstractInsnNode previous() {
            return this.iterator.previous();
        }

        @Override
		public int previousIndex() {
            return this.iterator.previousIndex();
        }

        @Override
		public void remove() {
            this.iterator.remove();
        }

        @Override
		public void set(final AbstractInsnNode e) {
            this.iterator.set(e);
        }

    }

    /*
     * An instruction is safe if it is assured that the following instruction is always executed,
     * so there is no jump and no exception.
     */
    private static enum InstructionType { METHODENTRY, METHODEXIT, SAFE, UNSAFE }

    private final Tracer tracer;
    private final ReadMethod readMethod;
    private final ClassNode classNode;
    private final MethodNode methodNode;

    private final int tracerLocalVarIndex;

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
    private static int statsLabelsJumpTargets = 0;
    private static int statsLabelsAdditional = 0;
    private static int statsArrayStore = 0;
    private static int statsArrayLoad = 0;
    private static int statsGetField = 0;
    private static int statsPutField = 0;
    private ListIterator<AbstractInsnNode> instructionIterator;
    private Set<LabelNode> jumpTargetLabels;
    private Map<LabelNode, Integer> labelLineNumbers;
    private int currentLine = -1;
    private int firstLine = -1;
    private int outstandingInitializations = 0;

    public TracingMethodInstrumenter(final Tracer tracer, final ReadMethod readMethod, final ClassNode classNode, final MethodNode methodNode) {
        this.tracer = tracer;
        this.readMethod = readMethod;
        this.classNode = classNode;
        this.methodNode = methodNode;
        int usedLocalVars = ((readMethod.getAccess() & Opcodes.ACC_STATIC) == 0 ? 1 : 0);
        for (final Type t: Type.getArgumentTypes(readMethod.getDesc()))
            usedLocalVars += t.getSize();
        if (methodNode.localVariables != null) {
            for (final Object locVarNode: methodNode.localVariables) {
                final LocalVariableNode locVar = (LocalVariableNode)locVarNode;
                final int index = locVar.index + Type.getType(locVar.desc).getSize();
                if (usedLocalVars < index)
                    usedLocalVars = index;
            }
        }
        this.tracerLocalVarIndex = usedLocalVars;
        ++statsMethods;
        if (lastClass != readMethod.getReadClass()) {
            lastClass = readMethod.getReadClass();
            ++statsClasses;
        }
    }

    @SuppressWarnings("unchecked")
    public void transform(final ListIterator<MethodNode> methodIt) {

        // do not modify abstract or native methods
        if ((this.methodNode.access & ACC_ABSTRACT) != 0 || (this.methodNode.access & ACC_NATIVE) != 0)
            return;

        // check out what labels are jump targets (only these have to be traced)
        analyze(this.methodNode);

        this.instructionIterator = new FixedInstructionIterator(this.methodNode.instructions);
        // in the old method, initialize the new local variable for the threadtracer
        this.instructionIterator.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Tracer.class),
                "getInstance", "()L"+Type.getInternalName(Tracer.class)+";", false));
        this.instructionIterator.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Tracer.class),
                "getThreadTracer", "()L"+Type.getInternalName(ThreadTracer.class)+";", false));
        this.instructionIterator.add(new InsnNode(DUP));
        this.instructionIterator.add(new VarInsnNode(ASTORE, this.tracerLocalVarIndex));

        this.instructionIterator.add(new MethodInsnNode(INVOKEINTERFACE,
                Type.getInternalName(ThreadTracer.class), "isPaused", "()Z", true));
        final LabelNode noTracingLabel = new LabelNode();
        this.instructionIterator.add(new JumpInsnNode(IFNE, noTracingLabel));
        // create a copy of the (uninstrumented) instructions (later, while iterating through the instructions)
        final InsnList oldInstructions = new InsnList();
        final Map<LabelNode, LabelNode> labelCopies = new LazyLabelMap();
        // copy the try-catch-blocks
        final Object[] oldTryCatchblockNodes = this.methodNode.tryCatchBlocks.toArray();
        for (final Object o: oldTryCatchblockNodes) {
            final TryCatchBlockNode tcb = (TryCatchBlockNode) o;
            final TryCatchBlockNode newTcb = new TryCatchBlockNode(
                    labelCopies.get(tcb.start),
                    labelCopies.get(tcb.end),
                    labelCopies.get(tcb.handler),
                    tcb.type);
            this.methodNode.tryCatchBlocks.add(newTcb);
        }


        // increment number of local variables by one (for the threadtracer)
        ++this.methodNode.maxLocals;

        // and increment all local variable indexes after the new one by one
        for (final Object o: this.methodNode.localVariables) {
            final LocalVariableNode localVar = (LocalVariableNode) o;
            if (localVar.index >= this.tracerLocalVarIndex)
                ++localVar.index;
        }

        // store information about local variables in the ReadMethod object
        List<LocalVariable> localVariables = new ArrayList<LocalVariable>();
        for (final Object o: this.methodNode.localVariables) {
            final LocalVariableNode localVar = (LocalVariableNode) o;
            while (localVariables.size() <= localVar.index)
            	localVariables.add(null);
			localVariables.set(localVar.index, new LocalVariable(localVar.index, localVar.name, localVar.desc));
        }
        this.readMethod.setLocalVariables(localVariables.toArray(new LocalVariable[localVariables.size()]));
        localVariables = null;

        // each method must start with a (dedicated) label:
        assert this.readMethod.getInstructions().isEmpty();
        traceLabel(null, InstructionType.METHODENTRY);
        assert this.readMethod.getInstructions().size() == 1
            && this.readMethod.getInstructions().get(0) instanceof LabelMarker
            && ((LabelMarker)this.readMethod.getInstructions().get(0)).isAdditionalLabel();
        this.readMethod.setMethodEntryLabel((LabelMarker) this.readMethod.getInstructions().get(0));

        // needed later:
        final LabelNode l0 = new LabelNode();
        this.instructionIterator.add(l0);

        // then, visit the instructions that were in the method before
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
            case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
            	//TODO
            	// ignore
            	break;
            case AbstractInsnNode.FRAME:
                // ignore
                break;
            case AbstractInsnNode.LINE:
                // ignore
                break;
            default:
                throw new RuntimeException("Unknown instruction type " + insnNode.getType()
                        + " (" + insnNode.getClass().getSimpleName()+")");
            }
            oldInstructions.add(insnNode.clone(labelCopies));
        }

        assert this.outstandingInitializations == 0;

        // add the (old) try-catch blocks to the readMethods
        // (can only be done down here since we use the information in the
        // labels map)
        for (final Object o: oldTryCatchblockNodes) {
            final TryCatchBlockNode tcb = (TryCatchBlockNode) o;
            this.readMethod.addTryCatchBlock(new TryCatchBlock(
                this.labels.get(tcb.start), this.labels.get(tcb.end),
                this.labels.get(tcb.handler), tcb.type));
        }

        final LabelNode l1 = new LabelNode();
        this.instructionIterator.add(l1);
        final int newPos = this.readMethod.getInstructions().size();
        traceLabel(null, InstructionType.METHODEXIT);
        assert this.readMethod.getInstructions().size() == newPos+1;
        final AbstractInstruction abnormalTerminationLabel = this.readMethod.getInstructions().get(newPos);
        assert abnormalTerminationLabel instanceof LabelMarker;
        this.readMethod.setAbnormalTerminationLabel((LabelMarker) abnormalTerminationLabel);
        this.methodNode.instructions.add(new InsnNode(ATHROW));

        // add a try catch block around the method so that we can trace when this method is left
        // by a thrown exception
        this.methodNode.tryCatchBlocks.add(new TryCatchBlockNode(l0, l1, l1, null));

        // now add the code that is executed if no tracing should be performed
        this.methodNode.instructions.add(noTracingLabel);
        if (this.firstLine != -1)
            this.methodNode.instructions.add(new LineNumberNode(this.firstLine, noTracingLabel));
        this.methodNode.instructions.add(new InsnNode(ACONST_NULL));
        this.methodNode.instructions.add(new VarInsnNode(ASTORE, this.tracerLocalVarIndex));
        this.methodNode.instructions.add(oldInstructions);

        // finally: create a copy of the method that gets the ThreadTracer as argument
        // this is only necessary for private methods or "<init>"
        if (this.tracer.wasRedefined(this.readMethod.getReadClass().getName()) &&
                (this.methodNode.access & ACC_PRIVATE) != 0) {
            final Type[] oldMethodArguments = Type.getArgumentTypes(this.methodNode.desc);
            final Type[] newMethodArguments = Arrays.copyOf(oldMethodArguments, oldMethodArguments.length+1);
            newMethodArguments[oldMethodArguments.length] = Type.getType(ThreadTracer.class);
            final String newMethodDesc = Type.getMethodDescriptor(Type.getReturnType(this.methodNode.desc), newMethodArguments);
            final MethodNode newMethod = new MethodNode(this.methodNode.access, this.methodNode.name, newMethodDesc,
                    this.methodNode.signature, (String[]) this.methodNode.exceptions.toArray(new String[this.methodNode.exceptions.size()]));
            methodIt.add(newMethod);

            int threadTracerParamPos = ((this.readMethod.getAccess() & Opcodes.ACC_STATIC) == 0 ? 1 : 0);
            for (final Type t: oldMethodArguments)
                threadTracerParamPos += t.getSize();

            final Map<LabelNode, LabelNode> newMethodLabels = new LazyLabelMap();

            // copy the local variables information to the new method
            for (final Object o: this.methodNode.localVariables) {
                final LocalVariableNode lv = (LocalVariableNode) o;
                newMethod.localVariables.add(new LocalVariableNode(
                    lv.name, lv.desc, lv.signature, newMethodLabels.get(lv.start),
                    newMethodLabels.get(lv.end), lv.index
                ));
            }


            newMethod.maxLocals = this.methodNode.maxLocals;
            newMethod.maxStack = this.methodNode.maxStack;

            // copy the try-catch-blocks
            for (final Object o: this.methodNode.tryCatchBlocks) {
                final TryCatchBlockNode tcb = (TryCatchBlockNode) o;
                newMethod.tryCatchBlocks.add(new TryCatchBlockNode(
                        newMethodLabels.get(tcb.start),
                        newMethodLabels.get(tcb.end),
                        newMethodLabels.get(tcb.handler),
                        tcb.type));
            }

            // skip the first 6 instructions, replace them with these:
            newMethod.instructions.add(new VarInsnNode(ALOAD, threadTracerParamPos));
            newMethod.instructions.add(new InsnNode(DUP));
            newMethod.instructions.add(new VarInsnNode(ASTORE, this.tracerLocalVarIndex));
            newMethod.instructions.add(new JumpInsnNode(IFNULL, newMethodLabels.get(noTracingLabel)));
            final Iterator<AbstractInsnNode> oldInsnIt = this.methodNode.instructions.iterator(6);
            // and add all the other instructions
            while (oldInsnIt.hasNext()) {
                final AbstractInsnNode insn = oldInsnIt.next();
                newMethod.instructions.add(insn.clone(newMethodLabels));
            }
        }

        ready();
    }

    private void analyze(final MethodNode method) {
        this.jumpTargetLabels = new HashSet<LabelNode>();
        this.labelLineNumbers = new HashMap<LabelNode, Integer>();
        final Iterator<?> insnIt = method.instructions.iterator();
        while (insnIt.hasNext()) {
            final AbstractInsnNode insn = (AbstractInsnNode) insnIt.next();
            switch (insn.getType()) {
            case AbstractInsnNode.JUMP_INSN:
                this.jumpTargetLabels.add(((JumpInsnNode)insn).label);
                break;
            case AbstractInsnNode.LOOKUPSWITCH_INSN:
                this.jumpTargetLabels.add(((LookupSwitchInsnNode)insn).dflt);
                for (final Object o: ((LookupSwitchInsnNode)insn).labels)
                    this.jumpTargetLabels.add((LabelNode)o);
                break;
            case AbstractInsnNode.TABLESWITCH_INSN:
                this.jumpTargetLabels.add(((TableSwitchInsnNode)insn).dflt);
                for (final Object o: ((TableSwitchInsnNode)insn).labels)
                    this.jumpTargetLabels.add((LabelNode)o);
                break;
            case AbstractInsnNode.LINE:
                final LineNumberNode lnn = (LineNumberNode)insn;
                if (this.labelLineNumbers.isEmpty())
                    this.firstLine = lnn.line;
                this.labelLineNumbers.put(lnn.start, lnn.line);
                break;
            }
        }

        for (final Object o: method.tryCatchBlocks) {
            // start and end are not really jump targets, but we add them nevertheless ;)
            final TryCatchBlockNode tcb = (TryCatchBlockNode) o;
            this.jumpTargetLabels.add(tcb.start);
            this.jumpTargetLabels.add(tcb.end);
            this.jumpTargetLabels.add(tcb.handler);
        }
    }

    private void transformJumpInsn(final JumpInsnNode insn) {
        final JumpInstruction jumpInstr = new JumpInstruction(this.readMethod, insn.getOpcode(), this.currentLine, null);
        this.jumpInstructions.put(jumpInstr, insn.label);
        registerInstruction(jumpInstr, InstructionType.UNSAFE);
        if (insn.getOpcode() == JSR) {
            // JSR is like a method call, so we have to add a label after this instruction
            addLabelIfNecessary(insn);
        }
    }

    private void transformMethodInsn(final MethodInsnNode insn) {
        registerInstruction(new MethodInvocationInstruction(this.readMethod, insn.getOpcode(), this.currentLine, insn.owner, insn.name, insn.desc), InstructionType.UNSAFE);

        MethodInsnNode thisInsn = insn;
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
            this.instructionIterator.set(thisInsn = new MethodInsnNode(insn.getOpcode(), insn.owner, insn.name, newDesc, insn.itf));
        }

        // if the method was a constructor, but it is no delegating constructor call and no super constructor call,
        // then call the tracer for the initialized object
        if (this.outstandingInitializations > 0 && insn.name.equals("<init>")) {
            this.instructionIterator.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
            this.instructionIterator.add(new InsnNode(SWAP));
            this.instructionIterator.add(new MethodInsnNode(INVOKEINTERFACE,
                Type.getInternalName(ThreadTracer.class), "objectInitialized", "(Ljava/lang/Object;)V", true));
            --this.outstandingInitializations;
        }

        // if the next instruction is no label, we have to add one after the instruction
        addLabelIfNecessary(thisInsn);
    }

    private void addLabelIfNecessary(final AbstractInsnNode insn) {
        AbstractInsnNode next = insn.getNext();
        while (next != null) {
            if (next instanceof LabelNode && this.jumpTargetLabels.contains(next))
                return;
            if (next instanceof FrameNode || next instanceof LineNumberNode)
                next = next.getNext();
            else
                break;
        }
        traceLabel(null, InstructionType.SAFE);
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

        // now set label references
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
            // do not trace assignments or usages of "this$..." and "val$..." fields
            if (!insn.name.contains("$")) { // TODO can we lift this?
                // top item on stack is the object reference: duplicate it
                // (add instruction *before* the current one
                this.instructionIterator.previous();
                this.instructionIterator.add(new InsnNode(DUP));
                objectTraceSeqIndex = this.tracer.newLongTraceSequence();
                ++TracingMethodInstrumenter.statsGetField;
                //System.out.println("seq " + index + ": getField " + name + " in method " + readMethod.getReadClass().getClassName() + "." + readMethod.getName());
            }
            break;

        case PUTFIELD:
            // do not trace assignments or usages of "this$..." and "val$..." fields
            if (!insn.name.contains("$")) { // TODO can we lift this?
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
                objectTraceSeqIndex = this.tracer.newLongTraceSequence();
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
                    INVOKEINTERFACE, Type.getInternalName(ThreadTracer.class),
                    "traceObject", "(Ljava/lang/Object;I)V", true));
            // and move to the position where it was before entering this method
            this.instructionIterator.next();
        }

        registerInstruction(new FieldInstruction(this.readMethod, insn.getOpcode(), this.currentLine,
                insn.owner, insn.name, insn.desc, objectTraceSeqIndex), InstructionType.UNSAFE);
    }

    private void transformIincInsn(final IincInsnNode insn) {
        registerInstruction(new IIncInstruction(this.readMethod, insn.var, insn.incr, this.currentLine), InstructionType.SAFE);
        if (insn.var >= this.tracerLocalVarIndex)
            ++insn.var;
    }

    private void transformInsn(final InsnNode insn) {
        int arrayTraceSeqIndex = -1;
        int indexTraceSeqIndex = -1;

        final InstructionType type;
        switch (insn.getOpcode()) {
        // the not interesting ones:
        case NOP:
        // constants:
        case ACONST_NULL: case ICONST_M1: case ICONST_0: case ICONST_1: case ICONST_2: case ICONST_3: case ICONST_4:
        case ICONST_5: case LCONST_0: case LCONST_1: case FCONST_0: case FCONST_1: case FCONST_2: case DCONST_0:
        case DCONST_1:
            type = InstructionType.SAFE;
            break;

        // array load:
        case IALOAD: case LALOAD: case FALOAD: case DALOAD: case AALOAD: case BALOAD: case CALOAD: case SALOAD:
            type = InstructionType.UNSAFE;
            // to trace array manipulations, we need two traces: one for the array, one for the index
            arrayTraceSeqIndex = this.tracer.newLongTraceSequence();
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
            type = InstructionType.UNSAFE;
            // to trace array manipulations, we need two traces: one for the array, one for the index
            arrayTraceSeqIndex = this.tracer.newLongTraceSequence();
            indexTraceSeqIndex = this.tracer.newIntegerTraceSequence();
            //System.out.println("seq " + arrayTraceIndex + ": array in method " + readMethod.getReadClass().getClassName() + "." + readMethod.getName());
            //System.out.println("seq " + indexTraceIndex + ": arrayindex in method " + readMethod.getReadClass().getClassName() + "." + readMethod.getName());
            // top three words on the stack: value, array index, array reference
            // after our manipulation: array index, array reference, value, array index, array reference
            // (add instruction *before* the current one)
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
        case POP: case POP2: case DUP: case DUP2:
            type = InstructionType.SAFE;
            break;

        case DUP_X1: case DUP_X2: case DUP2_X1: case DUP2_X2: case SWAP:
            // i would like to throw an error here, since this stack manipulation is problematic.
            // but unfortunately, it is used in bytecode of the standard api on linux and mac, version 1.6.17.
//            if (this.tracer.debug) {
//                Transformer.printMethod(System.err, this.methodNode);
//                System.err.println("Error at instruction " + this.instructionIterator.previousIndex());
//            }
//            throw new TracerException("Method "+this.classNode.name+"."+this.methodNode.name+
//                " uses unsupported stack manipulation. Maybe it was compiled for JRE < 1.5?");
            type = InstructionType.SAFE;
            break;

        // safe arithmetic instructions:
        case IADD: case LADD: case FADD: case DADD: case ISUB: case LSUB: case FSUB: case DSUB: case IMUL: case LMUL:
        case FMUL: case DMUL: case FDIV: case DDIV: case FREM: case DREM:
        case INEG: case LNEG: case FNEG: case DNEG: case ISHL: case LSHL: case ISHR: case LSHR: case IUSHR: case LUSHR:
        case IAND: case LAND: case IOR: case LOR: case IXOR: case LXOR:
            type = InstructionType.SAFE;
            break;

        // unsafe arithmetic instructions:
        case IDIV: case LDIV: case IREM: case LREM:
            type = InstructionType.UNSAFE;
            break;

        // type conversions:
        case I2L: case I2F: case I2D: case L2I: case L2F: case L2D: case F2I: case F2L: case F2D: case D2I: case D2L:
        case D2F: case I2B: case I2C: case I2S:
            type = InstructionType.SAFE;
            break;

        // comparison:
        case LCMP: case FCMPL: case FCMPG: case DCMPL: case DCMPG:
            type = InstructionType.SAFE;
            break;

        // control-flow statements:
        case IRETURN: case LRETURN: case FRETURN: case DRETURN: case ARETURN: case RETURN:
            type = InstructionType.METHODEXIT;
            break;

        // special things
        case ARRAYLENGTH: case ATHROW: case MONITORENTER: case MONITOREXIT:
            type = InstructionType.UNSAFE;
            break;

        default:
            type = InstructionType.UNSAFE;
            assert false;
        }

        if (arrayTraceSeqIndex != -1) {
            assert indexTraceSeqIndex != -1;
            // the top two words on the stack are the array index and the array reference
            this.instructionIterator.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
            this.instructionIterator.add(new InsnNode(SWAP));
            this.instructionIterator.add(getIntConstInsn(indexTraceSeqIndex));
            this.instructionIterator.add(new MethodInsnNode(INVOKEINTERFACE,
                    Type.getInternalName(ThreadTracer.class), "traceInt", "(II)V", true));
            this.instructionIterator.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
            this.instructionIterator.add(new InsnNode(SWAP));
            this.instructionIterator.add(getIntConstInsn(arrayTraceSeqIndex));
            this.instructionIterator.add(new MethodInsnNode(INVOKEINTERFACE,
                    Type.getInternalName(ThreadTracer.class), "traceObject", "(Ljava/lang/Object;I)V", true));
            // and move to the position where it was before entering this method
            this.instructionIterator.next();

            registerInstruction(new ArrayInstruction(this.readMethod, insn.getOpcode(), this.currentLine,
                    arrayTraceSeqIndex, indexTraceSeqIndex), type);
        } else {
            assert indexTraceSeqIndex == -1;
            registerInstruction(new SimpleInstruction(this.readMethod, insn.getOpcode(), this.currentLine), type);
        }
    }

	private void transformIntInsn(final IntInsnNode insn) {
        if (insn.getOpcode() == NEWARRAY) {
            int newObjectIdSeqIndex = this.tracer.newLongTraceSequence();
            transformArrayAllocation(new NewArrayInstruction(this.readMethod,
                this.currentLine, insn.operand, newObjectIdSeqIndex), newObjectIdSeqIndex);
        } else {
            assert insn.getOpcode() == BIPUSH || insn.getOpcode() == SIPUSH;
            registerInstruction(new IntPush(this.readMethod, insn.getOpcode(), insn.operand, this.currentLine), InstructionType.SAFE);
        }
    }

    @SuppressWarnings("unchecked")
    private void transformArrayAllocation(AbstractInstruction insn, int newObjectIdSeqIndex) {
        // add a try/catch around the instruction to catch OutOfMemoryErrors.
        // place the exception handler right after the instruction, such that
        // surrounding exception handlers are still triggered after the inserted one.
        LabelNode startLabel = new LabelNode();
        LabelNode endLabel = new LabelNode();
        LabelNode handlerLabel = new LabelNode();
        LabelNode continuationLabel = new LabelNode();
        registerInstruction(insn, InstructionType.UNSAFE);
        this.instructionIterator.previous();
        this.instructionIterator.add(startLabel);
        this.instructionIterator.next();
        this.instructionIterator.add(endLabel);
        this.instructionIterator.add(new InsnNode(DUP));
        this.instructionIterator.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
        this.instructionIterator.add(new InsnNode(SWAP));
        this.instructionIterator.add(getIntConstInsn(newObjectIdSeqIndex));
        this.instructionIterator.add(new MethodInsnNode(INVOKEINTERFACE,
            Type.getInternalName(ThreadTracer.class), "traceObject",
            "(Ljava/lang/Object;I)V", true));
        this.instructionIterator.add(new JumpInsnNode(GOTO, continuationLabel));
        // add the exception handler *before* any other exception handler.
        // the Java VM Spec (paragraph 3.10) specified that they are
        // executed in the order in which they appear in the bytecode
        this.methodNode.tryCatchBlocks.add(0, new TryCatchBlockNode(startLabel,
            endLabel, handlerLabel, "java/lang/OutOfMemoryError"));
        this.instructionIterator.add(handlerLabel);
        this.instructionIterator.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
        this.instructionIterator.add(new InsnNode(ACONST_NULL));
        this.instructionIterator.add(getIntConstInsn(newObjectIdSeqIndex));
        this.instructionIterator.add(new MethodInsnNode(INVOKEINTERFACE,
            Type.getInternalName(ThreadTracer.class), "traceObject",
                "(Ljava/lang/Object;I)V", true));
        this.instructionIterator.add(new InsnNode(ATHROW));
        this.instructionIterator.add(continuationLabel);
    }

    private void transformLdcInsn(final LdcInsnNode insn) {
        registerInstruction(new LdcInstruction(this.readMethod, this.currentLine, insn.cst),
                insn.cst instanceof Number ? InstructionType.SAFE : InstructionType.UNSAFE);
    }

    private void transformLookupSwitchInsn(final LookupSwitchInsnNode insn) {
        final IntegerMap<LabelNode> handlers = new IntegerMap<LabelNode>(insn.keys.size()*4/3+1);
        assert insn.keys.size() == insn.labels.size();
        for (int i = 0; i < insn.keys.size(); ++i)
            handlers.put((Integer)insn.keys.get(i), (LabelNode)insn.labels.get(i));
        final LookupSwitchInstruction instr = new LookupSwitchInstruction(this.readMethod, this.currentLine, null, null);
        this.lookupSwitchInstructions.put(instr, new Pair<LabelNode, IntegerMap<LabelNode>>(insn.dflt, handlers));
        registerInstruction(instr, InstructionType.UNSAFE);
    }

    private void transformMultiANewArrayInsn(final MultiANewArrayInsnNode insn) {
        // create a new int array to hold the dimensions and fill the dimensions in there.
        // then push the dimensions back onto the stack, call the MULTIANEWARRAY instruction
        // and afterward, call a method that gets the dimensions array and the newly
        // created array and traces the object ids.
        this.instructionIterator.previous();
        this.instructionIterator.add(getIntConstInsn(insn.dims));
        this.instructionIterator.add(new IntInsnNode(NEWARRAY, Opcodes.T_INT));
        // now fill in the dimensions
        for (int dim = insn.dims-1; dim >= 0; --dim) {
            this.instructionIterator.add(new InsnNode(DUP_X1));
            this.instructionIterator.add(new InsnNode(SWAP));
            this.instructionIterator.add(getIntConstInsn(dim));
            this.instructionIterator.add(new InsnNode(SWAP));
            this.instructionIterator.add(new InsnNode(IASTORE));
        }
        // duplicate the array reference
        this.instructionIterator.add(new InsnNode(DUP));
        // push the dimensions back onto the stack
        for (int dim = 0; dim < insn.dims; ++dim) {
            // don't duplicate if this is the last entry
            if (dim != insn.dims-1)
                this.instructionIterator.add(new InsnNode(DUP));
            this.instructionIterator.add(getIntConstInsn(dim));
            this.instructionIterator.add(new InsnNode(IALOAD));
            // swap with the reference below us
            if (dim != insn.dims-1)
                this.instructionIterator.add(new InsnNode(SWAP));
        }
        this.instructionIterator.next();
        final int newObjCountSeqIndex = this.tracer.newIntegerTraceSequence();
        final int newObjIdSeqIndex = this.tracer.newLongTraceSequence();
        // now call the original MULTIANEWARRAY instruction
        registerInstruction(new MultiANewArrayInstruction(this.readMethod, this.currentLine, insn.desc, insn.dims, newObjCountSeqIndex, newObjIdSeqIndex),
                InstructionType.UNSAFE);
        // and now call a tracing method that gets the dimensions array, the newly
        // created multi-dimensional array, and the sequence ids
        this.instructionIterator.add(new InsnNode(DUP_X1));
        this.instructionIterator.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
        this.instructionIterator.add(getIntConstInsn(newObjCountSeqIndex));
        this.instructionIterator.add(getIntConstInsn(newObjIdSeqIndex));
        this.instructionIterator.add(new MethodInsnNode(INVOKESTATIC,
            Type.getInternalName(TracingMethodInstrumenter.class), "traceMultiANewArray",
            "([I[Ljava/lang/Object;"+Type.getDescriptor(ThreadTracer.class)+"II)V", false));
    }

    public static void traceMultiANewArray(final int[] dimensions, final Object[] newArray,
            final ThreadTracer threadTracer, final int newObjCountSeqIndex, final int newObjIdSeqIndex) {
        int totalCount = 1;
        int fac = 1;
        for (int i = 0; i < dimensions.length-1; ++i) {
            fac *= dimensions[i];
            totalCount += fac;
        }
        threadTracer.traceInt(totalCount, newObjCountSeqIndex);
        Object[][] queue = new Object[1][];
        queue[0] = newArray;
        threadTracer.traceObject(newArray, newObjIdSeqIndex);
        for (int i = 0; i < dimensions.length-1; ++i) {
            final Object[][] newQueue = i == dimensions.length-2 ? null : new Object[dimensions[i]][];
            int nqPos = 0;
            assert queue != null;
            for (final Object[] o: queue) {
                assert o != null;
                assert o.length == dimensions[i];
                if (newQueue != null) {
                    System.arraycopy(o, 0, newQueue, nqPos, dimensions[i]);
                    nqPos += dimensions[i];
                }
                for (int m = 0; m < dimensions[i]; ++m)
                    threadTracer.traceObject(o[m], newObjIdSeqIndex);
            }
            queue = newQueue;
        }
    }

    @SuppressWarnings("unchecked")
    private void transformTableSwitchInsn(final TableSwitchInsnNode insn) {
        assert insn.min + insn.labels.size() - 1 == insn.max;
        final TableSwitchInstruction instr = new TableSwitchInstruction(this.readMethod, this.currentLine,
                insn.min, insn.max, null, null);
        this.tableSwitchInstructions.put(instr, new Pair<LabelNode, List<LabelNode>>(insn.dflt, insn.labels));
        registerInstruction(instr, InstructionType.UNSAFE);
    }

	private void transformTypeInsn(final TypeInsnNode insn) {
        if (insn.getOpcode() == ANEWARRAY) {
            int newObjectIdSeqIndex = this.tracer.newLongTraceSequence();
            transformArrayAllocation(new TypeInstruction(this.readMethod, insn.getOpcode(),
                this.currentLine, insn.desc, newObjectIdSeqIndex), newObjectIdSeqIndex);
        } else if (insn.getOpcode() == NEW) {
            // after a NEW, we store the sequence number in the ThreadTracer object.
            // after the constructor has been called (which is guaranteed), its
            // id is written to the stored sequence number
            final int newObjectIdSeqIndex = this.tracer.newLongTraceSequence();
            final AbstractInstruction instruction = new TypeInstruction(this.readMethod, insn.getOpcode(),
                this.currentLine, insn.desc, newObjectIdSeqIndex);
            if (!this.instructionIterator.hasNext()) {
                if (this.tracer.debug)
                    Transformer.printMethod(System.err, this.methodNode);
                throw new TracerException("Bytecode of method "+this.classNode.name+"."+this.methodNode.name+
                    " has unsupported form (constructor call does not immediately follow NEW instruction). " +
                    "Maybe it was compiled for JRE < 1.5?");
            } else if (this.instructionIterator.next().getOpcode() != DUP) {
            	// this code handles a special kind of object initialization, which is sometimes used
            	// in a catch block which throws a new exceptions which encapsulates the old one.
            	// the bytecode of this construct looks like this:
            	//   NEW <newExceptionType>
            	//   DUP_X1
            	//   SWAP
            	//   INVOKESPECIAL <newExceptionType>.<init> (Ljava/lang/Throwable;)V
            	//   ATHROW
                this.instructionIterator.previous();
                AbstractInsnNode dup_x1 = this.instructionIterator.next();

                boolean matches = false;
                if (dup_x1.getOpcode() == DUP_X1 && this.instructionIterator.hasNext()) {
                    AbstractInsnNode swap = this.instructionIterator.next();
                    if (swap.getOpcode() == SWAP && this.instructionIterator.hasNext()) {
                        matches = true;
                    }
                }

                if (matches) {
                    // add another DUP_X1 before the existing one
                    this.instructionIterator.previous();
                    this.instructionIterator.previous();
                    this.instructionIterator.add(new InsnNode(DUP_X1));
                    //if (this.tracer.debug) {
                    //    System.err.println("Added DUP_X1 at position " + this.instructionIterator.previousIndex() + "!");
                    //    Transformer.printMethod(System.err, this.methodNode);
                    //}
                } else {
                    if (this.tracer.debug)
                        Transformer.printMethod(System.err, this.methodNode);
                    throw new TracerException("Bytecode of method "+this.classNode.name+"."+this.methodNode.name+
                        " has unsupported form (constructor call does not immediately follow NEW instruction). " +
                        "Maybe it was compiled for JRE < 1.5?");
                }
            } else {
                this.instructionIterator.previous();
                this.instructionIterator.add(new InsnNode(DUP));
            }
            ++this.outstandingInitializations;
            // modified code of registerInstruction():
            this.readMethod.addInstruction(instruction);
            this.instructionIterator.previous();
            this.instructionIterator.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
            this.instructionIterator.add(getIntConstInsn(instruction.getIndex()));
            this.instructionIterator.add(getIntConstInsn(newObjectIdSeqIndex));
            this.instructionIterator.add(new MethodInsnNode(INVOKEINTERFACE,
                    Type.getInternalName(ThreadTracer.class), "objectAllocated",
                    "(II)V", true));
            this.instructionIterator.next();
            ++TracingMethodInstrumenter.statsInstructions;
        } else {
            registerInstruction(new TypeInstruction(this.readMethod, insn.getOpcode(), this.currentLine,
                insn.desc, -1),
                InstructionType.UNSAFE);
        }
    }

    private void transformVarInsn(final VarInsnNode insn) {
        registerInstruction(new VarInstruction(this.readMethod, insn.getOpcode(), this.currentLine, insn.var),
                insn.getOpcode() == RET ? InstructionType.UNSAFE : InstructionType.SAFE);
        if (insn.var >= this.tracerLocalVarIndex)
            ++insn.var;
    }

    private void transformLabel(final LabelNode label) {
        final Integer line = this.labelLineNumbers.get(label);
        if (line != null)
            this.currentLine = line;
        traceLabel(label, InstructionType.UNSAFE);
    }

    private void traceLabel(final LabelNode label, final InstructionType type) {
        if (label == null || this.jumpTargetLabels.contains(label)) {
            final int seq = this.tracer.newIntegerTraceSequence();
            final boolean isAdditionalLabel = label == null;
            final int labelNr = isAdditionalLabel ? this.nextAdditionalLabelNr-- : this.nextLabelNr++;
            boolean isCatchBlock = false;
            if (!isAdditionalLabel) {
                for (final Object tcbObj: this.methodNode.tryCatchBlocks) {
                    if (((TryCatchBlockNode)tcbObj).handler == label) {
                        isCatchBlock = true;
                        break;
                    }
                }
            }
            final LabelMarker lm = new LabelMarker(this.readMethod, seq, this.currentLine, isAdditionalLabel,
                    isCatchBlock, labelNr);
            if (!isAdditionalLabel)
                this.labels.put(label, lm);

            // at runtime: push sequence index on the stack and call method to trace last executed instruction
            this.instructionIterator.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
            this.instructionIterator.add(getIntConstInsn(lm.getTraceSeqIndex()));
            this.instructionIterator.add(new MethodInsnNode(INVOKEINTERFACE,
                    Type.getInternalName(ThreadTracer.class), "traceLastInstructionIndex", "(I)V", true));

            // stats
            if (isAdditionalLabel)
                TracingMethodInstrumenter.statsLabelsAdditional++;
            else
                TracingMethodInstrumenter.statsLabelsJumpTargets++;

            // do not use registerInstruction, because the code has to be inserted *after* the label
            this.readMethod.addInstruction(lm);
            String methodName = null;
            switch (type) {
            case METHODENTRY:
                methodName = "enterMethod";
                break;
            case METHODEXIT:
                methodName = "leaveMethod";
                break;
            case UNSAFE:
                methodName = "passInstruction";
                break;
            case SAFE:
                if (TracingThreadTracer.DEBUG_TRACE_FILE || followedByJumpLabel(this.instructionIterator))
                    methodName = "passInstruction";
                break;
            }
            if (methodName != null) {
                this.instructionIterator.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
                this.instructionIterator.add(getIntConstInsn(lm.getIndex()));
                this.instructionIterator.add(new MethodInsnNode(INVOKEINTERFACE,
                        Type.getInternalName(ThreadTracer.class), methodName, "(I)V", true));
            }
            ++TracingMethodInstrumenter.statsInstructions;
        } else {
            TracingMethodInstrumenter.statsLabelsStd++;
        }
    }

    private void registerInstruction(final AbstractInstruction instruction, final InstructionType type) {
        this.readMethod.addInstruction(instruction);
        String methodName = null;
        switch (type) {
        case METHODENTRY:
            methodName = "enterMethod";
            break;
        case METHODEXIT:
            methodName = "leaveMethod";
            break;
        case UNSAFE:
            methodName = "passInstruction";
            break;
        case SAFE:
            if (TracingThreadTracer.DEBUG_TRACE_FILE || followedByJumpLabel(this.instructionIterator))
                methodName = "passInstruction";
            break;
        }
        if (methodName != null) {
            this.instructionIterator.previous();
            this.instructionIterator.add(new VarInsnNode(ALOAD, this.tracerLocalVarIndex));
            this.instructionIterator.add(getIntConstInsn(instruction.getIndex()));
            this.instructionIterator.add(new MethodInsnNode(INVOKEINTERFACE,
                    Type.getInternalName(ThreadTracer.class), methodName, "(I)V", true));
            this.instructionIterator.next();
        }
        ++TracingMethodInstrumenter.statsInstructions;
    }

    private boolean followedByJumpLabel(final ListIterator<AbstractInsnNode> iterator) {
        if (!iterator.hasNext())
            return false;
        final AbstractInsnNode nextInsn = iterator.next();
        iterator.previous();
        for (AbstractInsnNode insn = nextInsn; insn != null; insn = insn.getNext()) {
            if (insn instanceof LabelNode && this.jumpTargetLabels.contains(insn))
                return true;
            if (!(insn instanceof FrameNode || insn instanceof LineNumberNode))
                break;
        }
        return false;
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
        final String format = "%-23s%10d%n";
        out.println("Instrumentation statistics:");
        out.format(format, "classes", statsClasses);
        out.format(format, "methods", statsMethods);
        out.format(format, "instructions", statsInstructions);
        out.format(format, "labels (no jump target)", statsLabelsStd);
        out.format(format, "labels (jump target)", statsLabelsJumpTargets);
        out.format(format, "labels (additional)", statsLabelsAdditional);
        out.format(format, "array store", statsArrayStore);
        out.format(format, "array load", statsArrayLoad);
        out.format(format, "get field", statsGetField);
        out.format(format, "put field", statsPutField);
        out.println("----------------------------------------------------");
        out.println();
    }

}
