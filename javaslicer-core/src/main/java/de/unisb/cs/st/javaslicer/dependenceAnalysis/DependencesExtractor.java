package de.unisb.cs.st.javaslicer.dependenceAnalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.hammacher.util.ArrayStack;
import de.hammacher.util.IntegerMap;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.InstructionInstance;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowAnalyser;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.DependencesVisitor.DataDependenceType;
import de.unisb.cs.st.javaslicer.instructionSimulation.Simulator;
import de.unisb.cs.st.javaslicer.traceResult.BackwardInstructionIterator;
import de.unisb.cs.st.javaslicer.traceResult.ThreadId;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.variableUsages.VariableUsages;
import de.unisb.cs.st.javaslicer.variables.ArrayElement;
import de.unisb.cs.st.javaslicer.variables.LocalVariable;
import de.unisb.cs.st.javaslicer.variables.ObjectField;
import de.unisb.cs.st.javaslicer.variables.StackEntry;
import de.unisb.cs.st.javaslicer.variables.Variable;


/**
 * This class iterates (backwards) through the execution trace and visits
 * all dynamic data and control dependences, and other events like method entries
 * or exits.
 *
 * @author Clemens Hammacher
 */
public class DependencesExtractor {

    private final TraceResult trace;
    private final Simulator simulator = new Simulator();
    private List<DependencesVisitor> dataDependenceVisitorsReadAfterWrite = null;
    private List<DependencesVisitor> dataDependenceVisitorsWriteAfterRead = null;
    private List<DependencesVisitor> controlDependenceVisitors = null;
    private List<DependencesVisitor> instructionVisitors = null;
    private List<DependencesVisitor> pendingDataDependenceVisitorsReadAfterWrite = null;
    private List<DependencesVisitor> pendingDataDependenceVisitorsWriteAfterRead = null;
    private List<DependencesVisitor> pendingControlDependenceVisitors = null;
    private List<DependencesVisitor> methodEntryLeaveVisitors = null;
    private List<DependencesVisitor> objectCreationVisitors = null;

    public DependencesExtractor(final TraceResult trace) {
        this.trace = trace;
    }

    /**
     * Registers a {@link DependencesVisitor} with this {@link DependencesExtractor}.
     * This method should only be called before {@link #processBackwardTrace(long)},
     * otherwise you might get {@link ConcurrentModificationException}s.
     *
     * @param visitor the {@link DependencesVisitor} to register
     * @param capabilities the capabilities of the visitor (determines which
     *                     methods are called on the visitor)
     * @return <code>true</code> if the visitor was registered with any capabilities
     */
    public boolean registerVisitor(final DependencesVisitor visitor, final VisitorCapability... capabilities) {
        boolean change = false;
        for (final VisitorCapability cap: capabilities) {
            switch (cap) {
            case DATA_DEPENDENCES_ALL:
                if (this.dataDependenceVisitorsReadAfterWrite == null)
                    this.dataDependenceVisitorsReadAfterWrite = new ArrayList<DependencesVisitor>();
                change |= this.dataDependenceVisitorsReadAfterWrite.add(visitor);

                if (this.dataDependenceVisitorsWriteAfterRead == null)
                    this.dataDependenceVisitorsWriteAfterRead = new ArrayList<DependencesVisitor>();
                change |= this.dataDependenceVisitorsWriteAfterRead.add(visitor);
                break;
            case DATA_DEPENDENCES_READ_AFTER_WRITE:
                if (this.dataDependenceVisitorsReadAfterWrite == null)
                    this.dataDependenceVisitorsReadAfterWrite = new ArrayList<DependencesVisitor>();
                change |= this.dataDependenceVisitorsReadAfterWrite.add(visitor);
                break;
            case DATA_DEPENDENCES_WRITE_AFTER_READ:
                if (this.dataDependenceVisitorsWriteAfterRead == null)
                    this.dataDependenceVisitorsWriteAfterRead = new ArrayList<DependencesVisitor>();
                change |= this.dataDependenceVisitorsWriteAfterRead.add(visitor);
                break;
            case CONTROL_DEPENDENCES:
                if (this.controlDependenceVisitors == null)
                    this.controlDependenceVisitors = new ArrayList<DependencesVisitor>();
                change |= this.controlDependenceVisitors.add(visitor);
                break;
            case INSTRUCTION_EXECUTIONS:
                if (this.instructionVisitors == null)
                    this.instructionVisitors = new ArrayList<DependencesVisitor>();
                change |= this.instructionVisitors.add(visitor);
                break;
            case PENDING_CONTROL_DEPENDENCES:
                if (this.pendingControlDependenceVisitors == null)
                    this.pendingControlDependenceVisitors = new ArrayList<DependencesVisitor>();
                change |= this.pendingControlDependenceVisitors.add(visitor);
                break;
            case PENDING_DATA_DEPENDENCES_ALL:
                if (this.pendingDataDependenceVisitorsReadAfterWrite == null)
                    this.pendingDataDependenceVisitorsReadAfterWrite = new ArrayList<DependencesVisitor>();
                change |= this.pendingDataDependenceVisitorsReadAfterWrite.add(visitor);

                if (this.pendingDataDependenceVisitorsWriteAfterRead == null)
                    this.pendingDataDependenceVisitorsWriteAfterRead = new ArrayList<DependencesVisitor>();
                change |= this.pendingDataDependenceVisitorsWriteAfterRead.add(visitor);
                break;
            case PENDING_DATA_DEPENDENCES_READ_AFTER_WRITE:
                if (this.pendingDataDependenceVisitorsReadAfterWrite == null)
                    this.pendingDataDependenceVisitorsReadAfterWrite = new ArrayList<DependencesVisitor>();
                change |= this.pendingDataDependenceVisitorsReadAfterWrite.add(visitor);
                break;
            case PENDING_DATA_DEPENDENCES_WRITE_AFTER_READ:
                if (this.pendingDataDependenceVisitorsWriteAfterRead == null)
                    this.pendingDataDependenceVisitorsWriteAfterRead = new ArrayList<DependencesVisitor>();
                change |= this.pendingDataDependenceVisitorsWriteAfterRead.add(visitor);
                break;
            case METHOD_ENTRY_LEAVE:
                if (this.methodEntryLeaveVisitors == null)
                    this.methodEntryLeaveVisitors = new ArrayList<DependencesVisitor>();
                change |= this.methodEntryLeaveVisitors.add(visitor);
                break;
            case OBJECT_CREATION:
                if (this.objectCreationVisitors == null)
                    this.objectCreationVisitors = new ArrayList<DependencesVisitor>();
                change |= this.objectCreationVisitors.add(visitor);
                break;
            }
        }
        return change;
    }

    public boolean unregisterVisitor(final DependencesVisitor visitor) {
        boolean change = false;
        if (this.dataDependenceVisitorsReadAfterWrite.remove(visitor)) {
            change = true;
            if (this.dataDependenceVisitorsReadAfterWrite.isEmpty())
                this.dataDependenceVisitorsReadAfterWrite = null;
        }
        if (this.dataDependenceVisitorsWriteAfterRead.remove(visitor)) {
            change = true;
            if (this.dataDependenceVisitorsWriteAfterRead.isEmpty())
                this.dataDependenceVisitorsWriteAfterRead = null;
        }
        if (this.controlDependenceVisitors.remove(visitor)) {
            change = true;
            if (this.controlDependenceVisitors.isEmpty())
                this.controlDependenceVisitors = null;
        }
        if (this.instructionVisitors.remove(visitor)) {
            change = true;
            if (this.instructionVisitors.isEmpty())
                this.instructionVisitors = null;
        }
        if (this.pendingDataDependenceVisitorsReadAfterWrite.remove(visitor)) {
            change = true;
            if (this.pendingDataDependenceVisitorsReadAfterWrite.isEmpty())
                this.pendingDataDependenceVisitorsReadAfterWrite = null;
        }
        if (this.pendingDataDependenceVisitorsWriteAfterRead.remove(visitor)) {
            change = true;
            if (this.pendingDataDependenceVisitorsWriteAfterRead.isEmpty())
                this.pendingDataDependenceVisitorsWriteAfterRead = null;
        }
        if (this.pendingControlDependenceVisitors.remove(visitor)) {
            change = true;
            if (this.pendingControlDependenceVisitors.isEmpty())
                this.pendingControlDependenceVisitors = null;
        }
        if (this.methodEntryLeaveVisitors.remove(visitor)) {
            change = true;
            if (this.methodEntryLeaveVisitors.isEmpty())
                this.methodEntryLeaveVisitors = null;
        }
        if (this.objectCreationVisitors.remove(visitor)) {
            change = true;
            if (this.objectCreationVisitors.isEmpty())
                this.objectCreationVisitors = null;
        }
        return change;
    }

    /**
     * Go backwards through the execution trace of the given threadId and extract
     * all dependences. {@link DependencesVisitor}s should have been added before
     * by calling {@link #registerVisitor(DependencesVisitor, VisitorCapability...)}.
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
     * all dependences. {@link DependencesVisitor}s should have been added before
     * by calling {@link #registerVisitor(DependencesVisitor, VisitorCapability...)}.
     *
     * @param threadId identifies the thread whose trace should be analyzed
     */
    public void processBackwardTrace(final ThreadId threadId) {

        final BackwardInstructionIterator backwardInsnItr =
            this.trace.getBackwardIterator(threadId, null);

        final IntegerMap<Set<Instruction>> controlDependences = new IntegerMap<Set<Instruction>>();

        final ArrayStack<ExecutionFrame> frames = new ArrayStack<ExecutionFrame>();
        ExecutionFrame currentFrame = null;

        for (final ReadMethod method: backwardInsnItr.getInitialStackMethods()) {
            currentFrame = new ExecutionFrame();
            currentFrame.method = method;
            frames.push(currentFrame);
            if (this.methodEntryLeaveVisitors != null)
                for (final DependencesVisitor vis: this.methodEntryLeaveVisitors)
                    vis.visitMethodLeave(method);
        }

        // the lastWriter is needed for WAR data dependences
        final Map<Variable, InstructionInstance> lastWriter = new HashMap<Variable, InstructionInstance>();
        // lastReaders are needed for RAW data dependences
        final Map<Variable, List<InstructionInstance>> lastReaders = new HashMap<Variable, List<InstructionInstance>>();

        // this set contains all objects that have been created during the backwards traversal
        // of the trace. so these objects cannot have any dependences any more. from time to time,
        // corresponding variables are delete from lastWriter and lastReader.
        final Set<Long> createdObjects = new HashSet<Long>();

        // these variables control when lastReaders and lastWriter are cleaned up based on createdObjects
        int nextCleanupOfLastWriter = 1<<16;
        int nextCleanupOfLastReaders = 1<<16;

        long stepNr = 0;

        while (backwardInsnItr.hasNext()) {
            final InstructionInstance instance = backwardInsnItr.next();
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
                        for (final DependencesVisitor vis: this.methodEntryLeaveVisitors)
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
                        for (final DependencesVisitor vis: this.methodEntryLeaveVisitors)
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
                    final ReadMethod newMethod = instruction.getMethod();
                    if (this.methodEntryLeaveVisitors != null)
                        for (final DependencesVisitor vis: this.methodEntryLeaveVisitors) {
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
                for (final DependencesVisitor vis: this.instructionVisitors)
                    vis.visitInstructionExecution(instance);

            /*
            // fill createdObjects
            {
                Collection<Long> tmp = dynInfo.getCreatedObjects();
                if (!tmp.isEmpty()) {
                    if (tmp.size() == 1)
                        createdObjects.add(tmp.iterator().next());
                    else
                        createdObjects.addAll(tmp);
                }
            }
            */

            // the computation of control dependences only has to be performed
            // if there are any controlDependenceVisitors
            if (this.controlDependenceVisitors != null) {
                Set<Instruction> instrControlDependences = controlDependences.get(instruction.getIndex());
                if (instrControlDependences == null) {
                    computeControlDependences(instruction.getMethod(), controlDependences);
                    instrControlDependences = controlDependences.get(instruction.getIndex());
                    assert instrControlDependences != null;
                }
                // get all interesting instructions, that are dependent on the current one
                Set<InstructionInstance> dependantInterestingInstances = getInstanceIntersection(instrControlDependences,
                        currentFrame.interestingInstances);
                if (currentFrame.throwsException) {
                    currentFrame.throwsException = false;
                    // in this case, we have an additional control dependence from the catching to
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
                    for (final InstructionInstance depend: dependantInterestingInstances) {
                        for (final DependencesVisitor vis: this.controlDependenceVisitors) {
                            vis.visitControlDependence(depend, instance);
                        }
                    }
                    currentFrame.interestingInstances.removeAll(dependantInterestingInstances);
                }
                currentFrame.interestingInstances.add(instance);
                if (this.pendingControlDependenceVisitors != null)
                    for (final DependencesVisitor vis: this.pendingControlDependenceVisitors)
                        vis.visitPendingControlDependence(instance);
            }
            if (this.pendingControlDependenceVisitors != null) {
                currentFrame.interestingInstances.add(instance);
                for (final DependencesVisitor vis: this.pendingControlDependenceVisitors)
                    vis.visitPendingControlDependence(instance);
            }

            if (!dynInfo.getDefinedVariables().isEmpty()) {
                if (this.dataDependenceVisitorsReadAfterWrite != null
                        || this.dataDependenceVisitorsWriteAfterRead != null
                        || this.pendingDataDependenceVisitorsWriteAfterRead != null) {
                    for (final Variable definedVariable: dynInfo.getDefinedVariables()) {
                        if (this.pendingDataDependenceVisitorsWriteAfterRead != null && !(definedVariable instanceof StackEntry)) {
                            // for each defined variable, we have a pending WAR dependence
                            // if the lastWriter is not null, we first discard old pending dependences
                            final InstructionInstance varLastWriter = lastWriter.put(definedVariable, instance);
                            for (final DependencesVisitor vis: this.pendingDataDependenceVisitorsWriteAfterRead) {
                                if (varLastWriter != null)
                                    vis.discardPendingDataDependence(varLastWriter, definedVariable, DataDependenceType.WRITE_AFTER_READ);
                                vis.visitPendingDataDependence(instance, definedVariable, DataDependenceType.WRITE_AFTER_READ);
                            }
                        // otherwise, if there are WAR visitors, we only update the lastWriter
                        } else if (this.dataDependenceVisitorsWriteAfterRead != null) {
                            lastWriter.put(definedVariable, instance);
                        }
                        // if we have RAW visitors, we need to analyse the lastReaders
                        if (this.dataDependenceVisitorsReadAfterWrite != null
                                || this.pendingDataDependenceVisitorsReadAfterWrite != null) {
                            final List<InstructionInstance> readers = lastReaders.remove(definedVariable);
                            if (readers != null)
                                for (final InstructionInstance reader: readers) {
                                    if (this.dataDependenceVisitorsReadAfterWrite != null)
                                        for (final DependencesVisitor vis: this.dataDependenceVisitorsReadAfterWrite)
                                            vis.visitDataDependence(reader, instance, definedVariable, DataDependenceType.READ_AFTER_WRITE);
                                    if (this.pendingDataDependenceVisitorsReadAfterWrite != null)
                                        for (final DependencesVisitor vis: this.pendingDataDependenceVisitorsReadAfterWrite)
                                            vis.discardPendingDataDependence(reader, definedVariable, DataDependenceType.READ_AFTER_WRITE);
                                }
                        }
                    }
                    if (lastWriter.size() > nextCleanupOfLastWriter) {
                        cleanUpMaps(lastWriter, lastReaders, createdObjects,
                            stepNr, frames, false);
                        nextCleanupOfLastWriter = Math.max(1<<16, 2*lastWriter.size());
                        nextCleanupOfLastReaders = Math.max(1<<16, 2*lastReaders.size());
                    }
                }
            }

            if (!dynInfo.getUsedVariables().isEmpty()) {
                if (this.dataDependenceVisitorsWriteAfterRead != null ||
                        this.dataDependenceVisitorsReadAfterWrite != null) {
                    for (final Variable usedVariable: dynInfo.getUsedVariables()) {
                        // if we have WAR visitors, we inform them about a new dependence
                        if (this.dataDependenceVisitorsWriteAfterRead != null && !(usedVariable instanceof StackEntry)) {
                            final InstructionInstance lastWriterInst = lastWriter.get(usedVariable);

                            if (lastWriterInst != null) {
                                for (final DependencesVisitor vis: this.dataDependenceVisitorsWriteAfterRead)
                                    vis.visitDataDependence(lastWriterInst, instance, usedVariable, DataDependenceType.WRITE_AFTER_READ);
                            }
                        }

                        // for RAW visitors, update the lastReaders
                        if (this.dataDependenceVisitorsReadAfterWrite != null
                                || this.pendingDataDependenceVisitorsReadAfterWrite != null) {
                            List<InstructionInstance> readers = lastReaders.get(usedVariable);
                            if (readers == null) {
                                readers = new ArrayList<InstructionInstance>(4);
                                lastReaders.put(usedVariable, readers);
                                if (lastReaders.size() > nextCleanupOfLastReaders) {
                                    cleanUpMaps(lastWriter, lastReaders, createdObjects,
                                        stepNr, frames, false);
                                    nextCleanupOfLastWriter = Math.max(1<<16, 2*lastWriter.size());
                                    nextCleanupOfLastReaders = Math.max(1<<16, 2*lastReaders.size());
                                }
                            }
                            readers.add(instance);
                            // for each used variable, we have a pending RAW dependence
                            if (this.pendingDataDependenceVisitorsReadAfterWrite != null) {
                                for (final DependencesVisitor vis: this.pendingDataDependenceVisitorsReadAfterWrite)
                                    vis.visitPendingDataDependence(instance, usedVariable, DataDependenceType.READ_AFTER_WRITE);
                            }
                        }
                    }
                }
            }

            if (dynInfo.isCatchBlock())
                currentFrame.atCacheBlockStart = instance;
            else if (currentFrame.atCacheBlockStart != null)
                currentFrame.atCacheBlockStart = null;

            ++stepNr;
        }
        cleanUpMaps(lastWriter, lastReaders, createdObjects, stepNr, frames, true);
    }

    private void cleanUpMaps(final Map<Variable, InstructionInstance> lastWriter,
            final Map<Variable, List<InstructionInstance>> lastReaders, final Set<Long> createdObjects,
            final long stepNr, final Collection<ExecutionFrame> activeFrames, final boolean cleanCompletely) {
        final Set<ExecutionFrame> activeFrameSet = new HashSet<ExecutionFrame>(activeFrames);

        final Iterator<Entry<Variable, InstructionInstance>> lastWriterIt = lastWriter.entrySet().iterator();
        while (lastWriterIt.hasNext()) {
            final Entry<Variable, InstructionInstance> e = lastWriterIt.next();
            final Variable var = e.getKey();
            if (cleanCompletely
                    || (var instanceof ArrayElement && createdObjects.contains(((ArrayElement)var).getArrayId()))
                    || (var instanceof ObjectField && createdObjects.contains(((ObjectField)var).getObjectId()))
                    || (var instanceof StackEntry && !activeFrameSet.contains(((StackEntry)var).getFrame()))
                    || (var instanceof LocalVariable && !activeFrameSet.contains(((LocalVariable)var).getFrame()))) {
                if (this.pendingDataDependenceVisitorsWriteAfterRead != null && !(var instanceof StackEntry))
                    for (final DependencesVisitor vis: this.pendingDataDependenceVisitorsWriteAfterRead)
                        vis.discardPendingDataDependence(e.getValue(), var, DataDependenceType.WRITE_AFTER_READ);
                lastWriterIt.remove();
            }
        }

        final Iterator<Entry<Variable, List<InstructionInstance>>> lastReadersIt = lastReaders.entrySet().iterator();
        while (lastReadersIt.hasNext()) {
            final Entry<Variable, List<InstructionInstance>> e = lastReadersIt.next();
            final Variable var = e.getKey();
            if (cleanCompletely
                    || (var instanceof ArrayElement && createdObjects.contains(((ArrayElement)var).getArrayId()))
                    || (var instanceof ObjectField && createdObjects.contains(((ObjectField)var).getObjectId()))
                    || (var instanceof StackEntry && !activeFrameSet.contains(((StackEntry)var).getFrame()))
                    || (var instanceof LocalVariable && !activeFrameSet.contains(((LocalVariable)var).getFrame()))) {
                if (this.pendingDataDependenceVisitorsReadAfterWrite != null)
                    for (final InstructionInstance inst: e.getValue())
                        for (final DependencesVisitor vis: this.pendingDataDependenceVisitorsReadAfterWrite)
                            vis.discardPendingDataDependence(inst, var, DataDependenceType.READ_AFTER_WRITE);
                lastReadersIt.remove();
            }
        }

        createdObjects.clear();
    }

    private Set<InstructionInstance> getInstanceIntersection(
            final Set<Instruction> instructions,
            final Set<InstructionInstance> instances) {
        if (instructions.isEmpty() || instances.isEmpty())
            return Collections.emptySet();

        // TODO make more efficient

        final Set<InstructionInstance> intersectInstances = new HashSet<InstructionInstance>();
        for (final InstructionInstance inst: instances) {
            if (instructions.contains(inst.getInstruction()))
                intersectInstances.add(inst);
        }

        return intersectInstances;
    }

    private void computeControlDependences(final ReadMethod method, final IntegerMap<Set<Instruction>> controlDependences) {
        final Map<Instruction, Set<Instruction>> deps = ControlFlowAnalyser.getInstance().getInvControlDependences(method);
        for (final Entry<Instruction, Set<Instruction>> entry: deps.entrySet()) {
            final int index = entry.getKey().getIndex();
            assert !controlDependences.containsKey(index);
            controlDependences.put(index, entry.getValue());
        }
    }

}
