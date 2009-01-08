package de.unisb.cs.st.javaslicer.dependencyAnalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.hammacher.util.ArrayStack;
import de.hammacher.util.IntegerMap;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowAnalyser;
import de.unisb.cs.st.javaslicer.dependencyAnalysis.DependencyVisitor.DataDependencyType;
import de.unisb.cs.st.javaslicer.instructionSimulation.Simulator;
import de.unisb.cs.st.javaslicer.traceResult.BackwardInstructionIterator;
import de.unisb.cs.st.javaslicer.traceResult.ThreadId;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.variableUsages.VariableUsages;
import de.unisb.cs.st.javaslicer.variables.Variable;

public class DependencyExtractor {

    private final TraceResult trace;
    private final Simulator simulator = new Simulator();
    private ArrayList<DependencyVisitor> dataDependencyVisitorsReadAfterWrite = null;
    private ArrayList<DependencyVisitor> dataDependencyVisitorsWriteAfterRead = null;
    private ArrayList<DependencyVisitor> controlDependencyVisitors = null;
    private ArrayList<DependencyVisitor> instructionVisitors = null;
    private ArrayList<DependencyVisitor> pendingDataDependencyVisitorsReadAfterWrite = null;
    private ArrayList<DependencyVisitor> pendingDataDependencyVisitorsWriteAfterRead = null;
    private ArrayList<DependencyVisitor> pendingControlDependencyVisitors = null;
    private ArrayList<DependencyVisitor> methodEntryLeaveVisitors = null;

    public DependencyExtractor(final TraceResult trace) {
        this.trace = trace;
    }

    public boolean registerVisitor(final DependencyVisitor visitor, final VisitorCapabilities... capabilities) {
        boolean change = false;
        for (final VisitorCapabilities cap: capabilities) {
            switch (cap) {
            case DATA_DEPENDENCIES_ALL:
                if (this.dataDependencyVisitorsReadAfterWrite == null)
                    this.dataDependencyVisitorsReadAfterWrite = new ArrayList<DependencyVisitor>();
                change |= this.dataDependencyVisitorsReadAfterWrite.add(visitor);

                if (this.dataDependencyVisitorsWriteAfterRead == null)
                    this.dataDependencyVisitorsWriteAfterRead = new ArrayList<DependencyVisitor>();
                change |= this.dataDependencyVisitorsWriteAfterRead.add(visitor);
                break;
            case DATA_DEPENDENCIES_READ_AFTER_WRITE:
                if (this.dataDependencyVisitorsReadAfterWrite == null)
                    this.dataDependencyVisitorsReadAfterWrite = new ArrayList<DependencyVisitor>();
                change |= this.dataDependencyVisitorsReadAfterWrite.add(visitor);
                break;
            case DATA_DEPENDENCIES_WRITE_AFTER_READ:
                if (this.dataDependencyVisitorsWriteAfterRead == null)
                    this.dataDependencyVisitorsWriteAfterRead = new ArrayList<DependencyVisitor>();
                change |= this.dataDependencyVisitorsWriteAfterRead.add(visitor);
                break;
            case CONTROL_DEPENDENCIES:
                if (this.controlDependencyVisitors == null)
                    this.controlDependencyVisitors = new ArrayList<DependencyVisitor>();
                change |= this.controlDependencyVisitors.add(visitor);
                break;
            case INSTRUCTION_EXECUTIONS:
                if (this.instructionVisitors == null)
                    this.instructionVisitors = new ArrayList<DependencyVisitor>();
                change |= this.instructionVisitors.add(visitor);
                break;
            case PENDING_CONTROL_DEPENDENCIES:
                if (this.pendingControlDependencyVisitors == null)
                    this.pendingControlDependencyVisitors = new ArrayList<DependencyVisitor>();
                change |= this.pendingControlDependencyVisitors.add(visitor);
                break;
            case PENDING_DATA_DEPENDENCIES_ALL:
                if (this.pendingDataDependencyVisitorsReadAfterWrite == null)
                    this.pendingDataDependencyVisitorsReadAfterWrite = new ArrayList<DependencyVisitor>();
                change |= this.pendingDataDependencyVisitorsReadAfterWrite.add(visitor);

                if (this.pendingDataDependencyVisitorsWriteAfterRead == null)
                    this.pendingDataDependencyVisitorsWriteAfterRead = new ArrayList<DependencyVisitor>();
                change |= this.pendingDataDependencyVisitorsWriteAfterRead.add(visitor);
                break;
            case PENDING_DATA_DEPENDENCIES_READ_AFTER_WRITE:
                if (this.pendingDataDependencyVisitorsReadAfterWrite == null)
                    this.pendingDataDependencyVisitorsReadAfterWrite = new ArrayList<DependencyVisitor>();
                change |= this.pendingDataDependencyVisitorsReadAfterWrite.add(visitor);
                break;
            case PENDING_DATA_DEPENDENCIES_WRITE_AFTER_READ:
                if (this.pendingDataDependencyVisitorsWriteAfterRead == null)
                    this.pendingDataDependencyVisitorsWriteAfterRead = new ArrayList<DependencyVisitor>();
                change |= this.pendingDataDependencyVisitorsWriteAfterRead.add(visitor);
                break;
            case METHOD_ENTRY_LEAVE:
                if (this.methodEntryLeaveVisitors == null)
                    this.methodEntryLeaveVisitors = new ArrayList<DependencyVisitor>();
                change |= this.methodEntryLeaveVisitors.add(visitor);
                break;
            }
        }
        return change;
    }

    public boolean unregisterVisitor(final DependencyVisitor visitor) {
        boolean change = false;
        if (this.dataDependencyVisitorsReadAfterWrite.remove(visitor)) {
            change = true;
            if (this.dataDependencyVisitorsReadAfterWrite.isEmpty())
                this.dataDependencyVisitorsReadAfterWrite = null;
        }
        if (this.dataDependencyVisitorsWriteAfterRead.remove(visitor)) {
            change = true;
            if (this.dataDependencyVisitorsWriteAfterRead.isEmpty())
                this.dataDependencyVisitorsWriteAfterRead = null;
        }
        if (this.controlDependencyVisitors.remove(visitor)) {
            change = true;
            if (this.controlDependencyVisitors.isEmpty())
                this.controlDependencyVisitors = null;
        }
        if (this.instructionVisitors.remove(visitor)) {
            change = true;
            if (this.instructionVisitors.isEmpty())
                this.instructionVisitors = null;
        }
        if (this.pendingControlDependencyVisitors.remove(visitor)) {
            change = true;
            if (this.pendingControlDependencyVisitors.isEmpty())
                this.pendingControlDependencyVisitors = null;
        }
        if (this.pendingDataDependencyVisitorsReadAfterWrite.remove(visitor)) {
            change = true;
            if (this.pendingDataDependencyVisitorsReadAfterWrite.isEmpty())
                this.pendingDataDependencyVisitorsReadAfterWrite = null;
        }
        if (this.methodEntryLeaveVisitors.remove(visitor)) {
            change = true;
            if (this.methodEntryLeaveVisitors.isEmpty())
                this.methodEntryLeaveVisitors = null;
        }
        return change;
    }

    /**
     * Go backwards through the execution trace of the given threadId and extract
     * all dependencies. {@link DependencyVisitor}s should have been added before
     * by calling {@link #registerVisitor(DependencyVisitor, VisitorCapabilities...)}.
     *
     * If you know the exact {@link ThreadId} of the thread to process, you should
     * use {@link #processBackwardTrace(ThreadId)} instead, since a java thread id
     * does not have to be unique.
     *
     * @param javaThreadId identifies the thread whose trace should be analyzed
     */
    public void processBackwardTrace(final long javaThreadId) {
        final ThreadId id = this.trace.getThreadId(javaThreadId);
        if (id != null)
            processBackwardTrace(id);
    }

    /**
     * Go backwards through the execution trace of the given threadId and extract
     * all dependencies. {@link DependencyVisitor}s should have been added before
     * by calling {@link #registerVisitor(DependencyVisitor, VisitorCapabilities...)}.
     *
     * @param threadId identifies the thread whose trace should be analyzed
     */
    public void processBackwardTrace(final ThreadId threadId) {
        final BackwardInstructionIterator backwardInsnItr =
            this.trace.getBackwardIterator(threadId, null);

        final IntegerMap<Set<Instruction>> controlDependencies = new IntegerMap<Set<Instruction>>();

        final ArrayStack<ExecutionFrame> frames = new ArrayStack<ExecutionFrame>();
        ExecutionFrame currentFrame = null;

        for (ReadMethod method: backwardInsnItr.getInitialStackMethods()) {
            currentFrame = new ExecutionFrame();
            currentFrame.method = method;
            frames.push(currentFrame);
            if (this.methodEntryLeaveVisitors != null)
                for (DependencyVisitor vis: this.methodEntryLeaveVisitors)
                    vis.visitMethodLeave(method);
        }

        // the lastWriter is needed for WAR data dependencies
        final Map<Variable, Instance> lastWriter = new HashMap<Variable, Instance>();
        // lastReaders are needed for RAW data dependencies
        final Map<Variable, List<Instance>> lastReaders = new HashMap<Variable, List<Instance>>();

        while (backwardInsnItr.hasNext()) {
            final Instance instance = backwardInsnItr.next();
            final Instruction instruction = instance.getInstruction();

            ExecutionFrame removedFrame = null;
            final int stackDepth = instance.getStackDepth();
            assert stackDepth > 0;

            if (frames.size() != stackDepth) {
                if (frames.size() > stackDepth) {
                    assert frames.size() == stackDepth+1;
                    removedFrame = frames.pop();
                    assert removedFrame.method != null;
                    if (this.methodEntryLeaveVisitors != null)
                        for (DependencyVisitor vis: this.methodEntryLeaveVisitors)
                            vis.visitMethodEntry(removedFrame.method);
                } else {
                    // in all steps, the stackDepth can change by at most 1
                    assert frames.size() == stackDepth-1;
                    final ExecutionFrame newFrame = new ExecutionFrame();
                    if (frames.size() > 0 && frames.peek().atCacheBlockStart != null)
                        newFrame.throwsException = true;
                    newFrame.method = instruction.getMethod();
                    frames.push(newFrame);
                    if (this.methodEntryLeaveVisitors != null)
                        for (DependencyVisitor vis: this.methodEntryLeaveVisitors)
                            vis.visitMethodLeave(newFrame.method);
                }
                currentFrame = frames.peek();
            }
            assert currentFrame != null;

            // it is possible that we see successive instructions of different methods,
            // e.g. when called from native code
            if (currentFrame.method != instruction.getMethod()) {
                if (currentFrame.method == null) {
                    currentFrame.method = instruction.getMethod();
                } else {
                    ReadMethod newMethod = instruction.getMethod();
                    if (this.methodEntryLeaveVisitors != null)
                        for (DependencyVisitor vis: this.methodEntryLeaveVisitors) {
                            vis.visitMethodEntry(currentFrame.method);
                            vis.visitMethodLeave(newMethod);
                        }
                    currentFrame = new ExecutionFrame();
                    currentFrame.method = newMethod;
                    frames.set(stackDepth-1, currentFrame);
                }
            }

            final VariableUsages dynInfo = this.simulator.simulateInstruction(instance, currentFrame,
                    removedFrame, frames);

            if (this.instructionVisitors != null)
                for (final DependencyVisitor vis: this.instructionVisitors)
                    vis.visitInstructionExecution(instance);

            // the computation of control dependencies only has to be performed
            // if there are any controlDependencyVisitors
            if (this.controlDependencyVisitors != null) {
                Set<Instruction> instrControlDependencies = controlDependencies.get(instruction.getIndex());
                if (instrControlDependencies == null) {
                    computeControlDependencies(instruction.getMethod(), controlDependencies);
                    instrControlDependencies = controlDependencies.get(instruction.getIndex());
                    assert instrControlDependencies != null;
                }
                // get all interesting instructions, that are dependent on the current one
                Set<Instance> dependantInterestingInstances = getInstanceIntersection(instrControlDependencies,
                        currentFrame.interestingInstances);
                if (currentFrame.throwsException) {
                    currentFrame.throwsException = false;
                    // in this case, we have an additional control dependency from the catching to
                    // the throwing instruction
                    for (int i = stackDepth-2; i >= 0; --i) {
                        final ExecutionFrame f = frames.get(i);
                        if (f.atCacheBlockStart != null) {
                            if (f.interestingInstances.contains(f.atCacheBlockStart)) {
                                if (dependantInterestingInstances.isEmpty())
                                    dependantInterestingInstances = Collections.singleton(f.atCacheBlockStart);
                                else
                                    dependantInterestingInstances.add(f.atCacheBlockStart);
                            }
                            break;
                        }
                    }
                }
                if (!dependantInterestingInstances.isEmpty()) {
                    for (final Instance depend: dependantInterestingInstances) {
                        for (final DependencyVisitor vis: this.controlDependencyVisitors) {
                            vis.visitControlDependency(depend, instance);
                        }
                    }
                    currentFrame.interestingInstances.removeAll(dependantInterestingInstances);
                }
                currentFrame.interestingInstances.add(instance);
                if (this.pendingControlDependencyVisitors != null)
                    for (DependencyVisitor vis: this.pendingControlDependencyVisitors)
                        vis.visitPendingControlDependency(instance);
            }
            if (this.pendingControlDependencyVisitors != null) {
                currentFrame.interestingInstances.add(instance);
                for (DependencyVisitor vis: this.pendingControlDependencyVisitors)
                    vis.visitPendingControlDependency(instance);
            }

            if (!dynInfo.getDefinedVariables().isEmpty()) {
                // for each defined variable, we have a pending WAR dependency
                if (this.pendingDataDependencyVisitorsWriteAfterRead != null) {
                    for (final Variable definedVariable: dynInfo.getDefinedVariables()) {
                        // if the lastWriter is not null, we first discard old pending dependencies
                        Instance varLastWriter = lastWriter.get(definedVariable);
                        lastWriter.put(definedVariable, instance);
                        for (DependencyVisitor vis: this.pendingDataDependencyVisitorsWriteAfterRead) {
                            if (varLastWriter != null)
                                vis.discardPendingDataDependency(varLastWriter, definedVariable, DataDependencyType.WRITE_AFTER_READ);
                            vis.visitPendingDataDependency(instance, definedVariable, DataDependencyType.WRITE_AFTER_READ);
                        }
                    }
                }
                // if we have RAW visitors, we need to analyse the lastReaders
                if (this.dataDependencyVisitorsReadAfterWrite != null) {
                    for (final Variable definedVariable: dynInfo.getDefinedVariables()) {
                        final List<Instance> readers = lastReaders.get(definedVariable);
                        lastReaders.remove(definedVariable);
                        if (readers != null)
                            for (final Instance reader: readers) {
                                for (final DependencyVisitor vis: this.dataDependencyVisitorsReadAfterWrite)
                                    vis.visitDataDependency(reader, instance, definedVariable, DataDependencyType.READ_AFTER_WRITE);
                            }
                        if (this.dataDependencyVisitorsWriteAfterRead != null)
                            lastWriter.put(definedVariable, instance);
                    }
                // otherwise, if there are WAR visitors, we only update the lastWriter
                } else if (this.dataDependencyVisitorsWriteAfterRead != null) {
                    for (final Variable definedVariable: dynInfo.getDefinedVariables()) {
                        lastWriter.put(definedVariable, instance);
                    }
                }
            }

            if (!dynInfo.getUsedVariables().isEmpty()) {
                // if we have WAR visitors, we inform them about a new dependency
                if (this.dataDependencyVisitorsWriteAfterRead != null) {
                    for (final Variable usedVariable: dynInfo.getUsedVariables()) {
                        final Instance lastWriterInst = lastWriter.get(usedVariable);

                        if (lastWriterInst != null) {
                            for (final DependencyVisitor vis: this.dataDependencyVisitorsWriteAfterRead)
                                vis.visitDataDependency(lastWriterInst, instance, usedVariable, DataDependencyType.WRITE_AFTER_READ);
                        }

                        // for RAW visitors, update the lastReaders
                        if (this.dataDependencyVisitorsReadAfterWrite != null) {
                            List<Instance> readers = lastReaders.get(usedVariable);
                            if (readers == null) {
                                readers = new ArrayList<Instance>(4);
                                lastReaders.put(usedVariable, readers);
                            }
                            readers.add(instance);
                        }
                    }
                // for RAW visitors, update the lastReaders
                } else if (this.dataDependencyVisitorsReadAfterWrite != null) {
                    for (final Variable usedVariable: dynInfo.getUsedVariables()) {
                        List<Instance> readers = lastReaders.get(usedVariable);
                        if (readers == null) {
                            readers = new ArrayList<Instance>(4);
                            lastReaders.put(usedVariable, readers);
                        }
                        readers.add(instance);
                    }
                }
                // for each used variable, we have a pending RAW dependency
                if (this.pendingDataDependencyVisitorsReadAfterWrite != null) {
                    for (final Variable usedVariable: dynInfo.getUsedVariables())
                        for (DependencyVisitor vis: this.pendingDataDependencyVisitorsReadAfterWrite)
                            vis.visitPendingDataDependency(instance, usedVariable, DataDependencyType.READ_AFTER_WRITE);
                }
            }

            if (dynInfo.isCatchBlock())
                currentFrame.atCacheBlockStart = instance;
            else if (currentFrame.atCacheBlockStart != null)
                currentFrame.atCacheBlockStart = null;

        }
    }

    private Set<Instance> getInstanceIntersection(
            Set<Instruction> instructions,
            Set<Instance> instances) {
        if (instructions.isEmpty() || instances.isEmpty())
            return Collections.emptySet();

        // TODO make more efficient

        Set<Instance> intersectInstances = new HashSet<Instance>();
        for (Instance inst: instances) {
            if (instructions.contains(inst.getInstruction()))
                intersectInstances.add(inst);
        }

        return intersectInstances;
    }

    private void computeControlDependencies(final ReadMethod method, final IntegerMap<Set<Instruction>> controlDependencies) {
        final Map<Instruction, Set<Instruction>> deps = ControlFlowAnalyser.getInstance().getInvControlDependencies(method);
        for (final Entry<Instruction, Set<Instruction>> entry: deps.entrySet()) {
            final int index = entry.getKey().getIndex();
            assert !controlDependencies.containsKey(index);
            controlDependencies.put(index, entry.getValue());
        }
    }

}
