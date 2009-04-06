package de.unisb.cs.st.javaslicer.dependenceAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import de.hammacher.util.ArrayStack;
import de.hammacher.util.collections.BlockwiseSynchronizedBuffer;
import de.hammacher.util.maps.IntegerMap;
import de.unisb.cs.st.javaslicer.common.classRepresentation.AbstractInstructionInstanceFactory;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstanceFactory;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;
import de.unisb.cs.st.javaslicer.common.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowAnalyser;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.DependencesVisitor.DataDependenceType;
import de.unisb.cs.st.javaslicer.instructionSimulation.DynamicInformation;
import de.unisb.cs.st.javaslicer.instructionSimulation.ExecutionFrame;
import de.unisb.cs.st.javaslicer.instructionSimulation.Simulator;
import de.unisb.cs.st.javaslicer.traceResult.BackwardTraceIterator;
import de.unisb.cs.st.javaslicer.traceResult.ThreadId;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.variables.ArrayElement;
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
    private final Simulator simulator;

    private final Set<DependencesVisitor> dataDependenceVisitorsReadAfterWrite = new HashSet<DependencesVisitor>();
    private final Set<DependencesVisitor> dataDependenceVisitorsWriteAfterRead = new HashSet<DependencesVisitor>();
    private final Set<DependencesVisitor> controlDependenceVisitors = new HashSet<DependencesVisitor>();
    private final Set<DependencesVisitor> instructionVisitors = new HashSet<DependencesVisitor>();
    private final Set<DependencesVisitor> pendingDataDependenceVisitorsReadAfterWrite = new HashSet<DependencesVisitor>();
    private final Set<DependencesVisitor> pendingDataDependenceVisitorsWriteAfterRead = new HashSet<DependencesVisitor>();
    private final Set<DependencesVisitor> pendingControlDependenceVisitors = new HashSet<DependencesVisitor>();
    private final Set<DependencesVisitor> methodEntryLeaveVisitors = new HashSet<DependencesVisitor>();
    private final Set<DependencesVisitor> objectCreationVisitors = new HashSet<DependencesVisitor>();

    private final InstructionInstanceFactory instanceFactory;


    public DependencesExtractor(final TraceResult trace) {
        this(trace, new AbstractInstructionInstanceFactory());
    }

    public DependencesExtractor(final TraceResult trace, InstructionInstanceFactory instanceFactory) {
        this.trace = trace;
        this.simulator = new Simulator(trace);
        this.instanceFactory = instanceFactory;
    }

    /**
     * Registers a {@link DependencesVisitor} with this {@link DependencesExtractor}.
     * This method should only be called before {@link #processBackwardTrace(long)}.
     *
     * During traversal, calles to this method and {@link #unregisterVisitor(DependencesVisitor)}
     * have no influence to this traversal any more.
     *
     * @param visitor the {@link DependencesVisitor} to register
     * @param capabilities the capabilities of the visitor (determines which
     *                     methods are called on the visitor)
     * @return <code>true</code> if the visitor was registered with any new capability
     */
    public boolean registerVisitor(final DependencesVisitor visitor, final VisitorCapability... capabilities) {
        boolean change = false;
        for (final VisitorCapability cap: capabilities) {
            switch (cap) {
            case DATA_DEPENDENCES_ALL:
                change |= this.dataDependenceVisitorsReadAfterWrite.add(visitor);
                change |= this.dataDependenceVisitorsWriteAfterRead.add(visitor);
                break;
            case DATA_DEPENDENCES_READ_AFTER_WRITE:
                change |= this.dataDependenceVisitorsReadAfterWrite.add(visitor);
                break;
            case DATA_DEPENDENCES_WRITE_AFTER_READ:
                change |= this.dataDependenceVisitorsWriteAfterRead.add(visitor);
                break;
            case CONTROL_DEPENDENCES:
                change |= this.controlDependenceVisitors.add(visitor);
                break;
            case INSTRUCTION_EXECUTIONS:
                change |= this.instructionVisitors.add(visitor);
                break;
            case PENDING_CONTROL_DEPENDENCES:
                change |= this.pendingControlDependenceVisitors.add(visitor);
                break;
            case PENDING_DATA_DEPENDENCES_ALL:
                change |= this.pendingDataDependenceVisitorsReadAfterWrite.add(visitor);
                change |= this.pendingDataDependenceVisitorsWriteAfterRead.add(visitor);
                break;
            case PENDING_DATA_DEPENDENCES_READ_AFTER_WRITE:
                change |= this.pendingDataDependenceVisitorsReadAfterWrite.add(visitor);
                break;
            case PENDING_DATA_DEPENDENCES_WRITE_AFTER_READ:
                change |= this.pendingDataDependenceVisitorsWriteAfterRead.add(visitor);
                break;
            case METHOD_ENTRY_LEAVE:
                change |= this.methodEntryLeaveVisitors.add(visitor);
                break;
            case OBJECT_CREATION:
                change |= this.objectCreationVisitors.add(visitor);
                break;
            }
        }
        return change;
    }

    /**
     * Unregisters a {@link DependencesVisitor} with all registered capabilities.
     * This method should only be called before {@link #processBackwardTrace(long)}.
     *
     * During traversal, calles to this method and {@link #registerVisitor(DependencesVisitor, VisitorCapability...)}
     * have no influence to this traversal any more.
     *
     * @param visitor the {@link DependencesVisitor} to unregister
     * @return <code>true</code> if the visitor was registered with any capabilities
     */
    public boolean unregisterVisitor(final DependencesVisitor visitor) {
        boolean change = false;
        change |= this.dataDependenceVisitorsReadAfterWrite.remove(visitor);
        change |= this.dataDependenceVisitorsWriteAfterRead.remove(visitor);
        change |= this.controlDependenceVisitors.remove(visitor);
        change |= this.instructionVisitors.remove(visitor);
        change |= this.pendingDataDependenceVisitorsReadAfterWrite.remove(visitor);
        change |= this.pendingDataDependenceVisitorsWriteAfterRead.remove(visitor);
        change |= this.pendingControlDependenceVisitors.remove(visitor);
        change |= this.methodEntryLeaveVisitors.remove(visitor);
        change |= this.objectCreationVisitors.remove(visitor);
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
     * Calls {@link #processBackwardTrace(ThreadId, boolean)} with multithreaded == false
     *
     * @see #processBackwardTrace(ThreadId, boolean)
     */
    public void processBackwardTrace(ThreadId threadId) {
        processBackwardTrace(threadId, false);
    }

    /**
     * Go backwards through the execution trace of the given threadId and extract
     * all dependences. {@link DependencesVisitor}s should have been added before
     * by calling {@link #registerVisitor(DependencesVisitor, VisitorCapability...)}.
     *
     * @param threadId identifies the thread whose trace should be analyzed
     * @param multithreaded use an extra thread to traverse the trace
     */
    public void processBackwardTrace(ThreadId threadId, boolean multithreaded) {

        final BackwardTraceIterator backwardInsnItr =
            this.trace.getBackwardIterator(threadId, null, this.instanceFactory);

        // store the current set of visitors of each capability in an array for better
        // performance and faster empty-check (null reference if empty)
        final DependencesVisitor[] dataDependenceVisitorsReadAfterWrite0 = this.dataDependenceVisitorsReadAfterWrite.isEmpty()
            ? null : this.dataDependenceVisitorsReadAfterWrite.toArray(
                new DependencesVisitor[this.dataDependenceVisitorsReadAfterWrite.size()]);
        final DependencesVisitor[] dataDependenceVisitorsWriteAfterRead0 = this.dataDependenceVisitorsWriteAfterRead.isEmpty()
            ? null : this.dataDependenceVisitorsWriteAfterRead.toArray(
                new DependencesVisitor[this.dataDependenceVisitorsWriteAfterRead.size()]);
        final DependencesVisitor[] controlDependenceVisitors0 = this.controlDependenceVisitors.isEmpty()
            ? null : this.controlDependenceVisitors.toArray(
                new DependencesVisitor[this.controlDependenceVisitors.size()]);
        final DependencesVisitor[] instructionVisitors0 = this.instructionVisitors.isEmpty()
            ? null : this.instructionVisitors.toArray(
                new DependencesVisitor[this.instructionVisitors.size()]);
        final DependencesVisitor[] pendingDataDependenceVisitorsReadAfterWrite0 = this.pendingDataDependenceVisitorsReadAfterWrite.isEmpty()
            ? null : this.pendingDataDependenceVisitorsReadAfterWrite.toArray(
                new DependencesVisitor[this.pendingDataDependenceVisitorsReadAfterWrite.size()]);
        final DependencesVisitor[] pendingDataDependenceVisitorsWriteAfterRead0 = this.pendingDataDependenceVisitorsWriteAfterRead.isEmpty()
            ? null : this.pendingDataDependenceVisitorsWriteAfterRead.toArray(
                new DependencesVisitor[this.pendingDataDependenceVisitorsWriteAfterRead.size()]);
        final DependencesVisitor[] pendingControlDependenceVisitors0 = this.pendingControlDependenceVisitors.isEmpty()
            ? null : this.pendingControlDependenceVisitors.toArray(
                new DependencesVisitor[this.pendingControlDependenceVisitors.size()]);
        final DependencesVisitor[] methodEntryLeaveVisitors0 = this.methodEntryLeaveVisitors.isEmpty()
            ? null : this.methodEntryLeaveVisitors.toArray(
                new DependencesVisitor[this.methodEntryLeaveVisitors.size()]);
        final DependencesVisitor[] objectCreationVisitors0 = this.objectCreationVisitors.isEmpty()
            ? null : this.objectCreationVisitors.toArray(
                new DependencesVisitor[this.objectCreationVisitors.size()]);

        final IntegerMap<Set<Instruction>> controlDependences = new IntegerMap<Set<Instruction>>();

        final ArrayStack<ExecutionFrame> frames = new ArrayStack<ExecutionFrame>();
        ExecutionFrame currentFrame = null;

        for (final ReadMethod method: backwardInsnItr.getInitialStackMethods()) {
            currentFrame = new ExecutionFrame();
            currentFrame.method = method;
            currentFrame.interruptedControlFlow = true;
            frames.push(currentFrame);
            if (methodEntryLeaveVisitors0 != null)
                for (final DependencesVisitor vis: methodEntryLeaveVisitors0)
                    vis.visitMethodLeave(method);
        }

        final Iterator<InstructionInstance> instanceIterator;
        Thread iteratorThread = null;
        if (multithreaded) {
            final BlockwiseSynchronizedBuffer<InstructionInstance> buffer = new BlockwiseSynchronizedBuffer<InstructionInstance>(1<<16, 1<<20);
            final InstructionInstance firstInstance = backwardInsnItr.hasNext() ? backwardInsnItr.next() : null;
            final AtomicReference<Throwable> iteratorException = new AtomicReference<Throwable>(null);
            iteratorThread = new Thread("Trace iterator") {
                @Override
                public void run() {
                    try {
                        while (backwardInsnItr.hasNext())
                            buffer.put(backwardInsnItr.next());
                    } catch (Throwable t) {
                        iteratorException.set(t);
                    } finally {
                        try {
                            buffer.put(firstInstance); // to signal that this is the end of the trace
                            buffer.flush();
                        } catch (InterruptedException e) {
                            throw new RuntimeException("This private thread should never get interrupted", e);
                        }
                    }
                }
            };
            iteratorThread.start();
            instanceIterator = new Iterator<InstructionInstance>() {

                private InstructionInstance next = firstInstance;

                public boolean hasNext() {
                    if (this.next == null) {
                        boolean interrupted = false;
                        while (true) {
                            try {
                                this.next = buffer.take();
                                if (this.next == firstInstance) {
                                    this.next = null;
                                    Throwable t = iteratorException.get();
                                    if (t instanceof RuntimeException)
                                        throw (RuntimeException)t;
                                    else if (t != null) {
                                        throw new TracerException(
                                            "Iterator should not throw anything but RuntimeExceptions",
                                            t);
                                    }
                                }
                                break;
                            } catch (InterruptedException e) {
                                interrupted = true;
                            }
                        }
                        if (interrupted)
                            Thread.currentThread().interrupt();
                    }
                    return this.next != null;
                }

                public InstructionInstance next() {
                    if (!hasNext())
                        throw new NoSuchElementException();
                    InstructionInstance ret = this.next;
                    this.next = null;
                    return ret;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

            };
        } else {
            instanceIterator = backwardInsnItr;
        }

        // the lastWriter is needed for WAR data dependences
        final Map<Variable, InstructionInstance> lastWriter = new HashMap<Variable, InstructionInstance>();
        // lastReaders are needed for RAW data dependences
        final Map<Variable, List<InstructionInstance>> lastReaders = new HashMap<Variable, List<InstructionInstance>>();

        /*
        HashSet<Long> createdObjects = new HashSet<Long>();
        HashSet<Long> seenObjects = new HashSet<Long>();
        */

        InstructionInstance instance = null;
        Instruction instruction = null;

        try {
            while (instanceIterator.hasNext()) {
                instance = instanceIterator.next();
                instruction = instance.getInstruction();

                /*
                if (instance.getInstanceNr() % 1000000 == 0) {
                    System.out.format("%5de6: %s%n", instance.getInstanceNr() / 1000000, new Date());
                }
                */

                ExecutionFrame removedFrame = null;
                final int stackDepth = instance.getStackDepth();
                assert stackDepth > 0;

                if (frames.size() != stackDepth) {
                    if (frames.size() > stackDepth) {
                        assert frames.size() == stackDepth+1;
                        removedFrame = frames.pop();
                        assert removedFrame.method != null;
                        if (methodEntryLeaveVisitors0 != null)
                            for (final DependencesVisitor vis: methodEntryLeaveVisitors0)
                                vis.visitMethodEntry(removedFrame.method);
                        currentFrame = frames.peek();
                    } else {
                        // in all steps, the stackDepth can change by at most 1
                        assert frames.size() == stackDepth-1;
                        final ExecutionFrame newFrame = new ExecutionFrame();
                        // assertion: if the current frame catched an exception, then the new frame
                        // must have thrown it
                        assert currentFrame == null || currentFrame.atCatchBlockStart == null
                            || instruction == instruction.getMethod().getAbnormalTerminationLabel();
                        newFrame.method = instruction.getMethod();
                        if (instruction == newFrame.method.getAbnormalTerminationLabel()) {
                            newFrame.throwsException = newFrame.interruptedControlFlow = true;
                        }
                        frames.push(newFrame);
                        if (methodEntryLeaveVisitors0 != null)
                            for (final DependencesVisitor vis: methodEntryLeaveVisitors0)
                                vis.visitMethodLeave(newFrame.method);
                        currentFrame = newFrame;
                    }
                }
                assert currentFrame != null;

                // it is possible that we see successive instructions of different methods,
                // e.g. when called from native code
                if (currentFrame.method  == null) {
                    assert currentFrame.returnValue == null;
                    currentFrame.method = instruction.getMethod();
                } else if (currentFrame.finished || currentFrame.method != instruction.getMethod()) {
                    final ReadMethod newMethod = instruction.getMethod();
                    if (methodEntryLeaveVisitors0 != null)
                        for (final DependencesVisitor vis: methodEntryLeaveVisitors0) {
                            vis.visitMethodEntry(currentFrame.method);
                            vis.visitMethodLeave(newMethod);
                        }
                    cleanUpExecutionFrame(currentFrame, lastReaders, lastWriter,
                        pendingDataDependenceVisitorsWriteAfterRead0, pendingDataDependenceVisitorsReadAfterWrite0,
                        dataDependenceVisitorsWriteAfterRead0, dataDependenceVisitorsReadAfterWrite0);
                    currentFrame = new ExecutionFrame();
                    currentFrame.method = newMethod;
                    frames.set(stackDepth-1, currentFrame);
                }

                if (instruction == instruction.getMethod().getMethodEntryLabel())
                    currentFrame.finished = true;
                currentFrame.lastInstruction = instruction;

                /*
                if (stackDepth == 1) {
                    System.out.format("%3d    %3d   %s%n", stepNr, currentFrame.operandStack.intValue(), instance);
                }
                */

                final DynamicInformation dynInfo = this.simulator.simulateInstruction(instance, currentFrame,
                        removedFrame, frames);

                if (instructionVisitors0 != null)
                    for (final DependencesVisitor vis: instructionVisitors0)
                        vis.visitInstructionExecution(instance);

                // the computation of control dependences only has to be performed
                // if there are any controlDependenceVisitors
                if (controlDependenceVisitors0 != null) {
                    Set<Instruction> instrControlDependences = controlDependences.get(instruction.getIndex());
                    if (instrControlDependences == null) {
                        computeControlDependences(instruction.getMethod(), controlDependences);
                        instrControlDependences = controlDependences.get(instruction.getIndex());
                        assert instrControlDependences != null;
                    }
                    final boolean isExceptionsThrowingInstruction = currentFrame.throwsException &&
                        (instruction.getType() != InstructionType.LABEL || !((LabelMarker)instruction).isAdditionalLabel());
                    // get all interesting instructions, that are dependent on the current one
                    Set<InstructionInstance> dependantInterestingInstances = currentFrame.interestingInstances == null
                        ? Collections.<InstructionInstance>emptySet()
                        : getInstanceIntersection(instrControlDependences, currentFrame.interestingInstances);
                    if (isExceptionsThrowingInstruction) {
                        currentFrame.throwsException = false;
                        // in this case, we have an additional control dependence from the catching to
                        // the throwing instruction
                        for (int i = stackDepth-2; i >= 0; --i) {
                            final ExecutionFrame f = frames.get(i);
                            if (f.atCatchBlockStart != null) {
                                if (f.interestingInstances != null && f.interestingInstances.contains(f.atCatchBlockStart)) {
                                    if (dependantInterestingInstances.isEmpty())
                                        dependantInterestingInstances = Collections.singleton(f.atCatchBlockStart);
                                    else
                                        dependantInterestingInstances.add(f.atCatchBlockStart);
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
                        if (currentFrame.interestingInstances != null)
                            currentFrame.interestingInstances.removeAll(dependantInterestingInstances);
                    }
                    if (currentFrame.interestingInstances == null)
                        currentFrame.interestingInstances = new HashSet<InstructionInstance>();
                    currentFrame.interestingInstances.add(instance);
                }
                // TODO check this:
                if (pendingControlDependenceVisitors0 != null) {
                    if (currentFrame.interestingInstances == null)
                        currentFrame.interestingInstances = new HashSet<InstructionInstance>();
                    currentFrame.interestingInstances.add(instance);
                    for (final DependencesVisitor vis: pendingControlDependenceVisitors0)
                        vis.visitPendingControlDependence(instance);
                }

                if (!dynInfo.getDefinedVariables().isEmpty()) {
                    /*
                    for (final Variable definedVariable: dynInfo.getDefinedVariables()) {
                        if (definedVariable instanceof ObjectField) {
                            seenObjects.add(((ObjectField)definedVariable).getObjectId());
                            assert !createdObjects.contains(((ObjectField)definedVariable).getObjectId());
                        }
                        if (definedVariable instanceof ArrayElement) {
                            seenObjects.add(((ArrayElement)definedVariable).getArrayId());
                            assert !createdObjects.contains(((ArrayElement)definedVariable).getArrayId());
                        }
                    }
                    */
                    if (dataDependenceVisitorsReadAfterWrite0 != null
                            || dataDependenceVisitorsWriteAfterRead0 != null
                            || pendingDataDependenceVisitorsWriteAfterRead0 != null) {
                        for (final Variable definedVariable: dynInfo.getDefinedVariables()) {
                            if (!(definedVariable instanceof StackEntry)) {
                                // we ignore WAR dependences over the stack!
                                if (pendingDataDependenceVisitorsWriteAfterRead0 != null) {
                                    // for each defined variable, we have a pending WAR dependence
                                    // if the lastWriter is not null, we first discard old pending dependences
                                    final InstructionInstance varLastWriter = lastWriter.put(definedVariable, instance);
                                    for (final DependencesVisitor vis: pendingDataDependenceVisitorsWriteAfterRead0) {
                                        if (varLastWriter != null)
                                            vis.discardPendingDataDependence(varLastWriter, definedVariable, DataDependenceType.WRITE_AFTER_READ);
                                        vis.visitPendingDataDependence(instance, definedVariable, DataDependenceType.WRITE_AFTER_READ);
                                    }
                                // otherwise, if there are WAR visitors, we only update the lastWriter
                                } else if (dataDependenceVisitorsWriteAfterRead0 != null) {
                                    lastWriter.put(definedVariable, instance);
                                }
                            }
                            // if we have RAW visitors, we need to analyse the lastReaders
                            if (dataDependenceVisitorsReadAfterWrite0 != null
                                    || pendingDataDependenceVisitorsReadAfterWrite0 != null) {
                                final List<InstructionInstance> readers = lastReaders.remove(definedVariable);
                                if (readers != null)
                                    for (final InstructionInstance reader: readers) {
                                        if (dataDependenceVisitorsReadAfterWrite0 != null)
                                            for (final DependencesVisitor vis: dataDependenceVisitorsReadAfterWrite0)
                                                vis.visitDataDependence(reader, instance, definedVariable, DataDependenceType.READ_AFTER_WRITE);
                                        if (pendingDataDependenceVisitorsReadAfterWrite0 != null)
                                            for (final DependencesVisitor vis: pendingDataDependenceVisitorsReadAfterWrite0)
                                                vis.discardPendingDataDependence(reader, definedVariable, DataDependenceType.READ_AFTER_WRITE);
                                    }
                            }
                        }
                    }
                }

                if (!dynInfo.getUsedVariables().isEmpty()) {
                    /*
                    for (final Variable usedVariable: dynInfo.getUsedVariables()) {
                        if (usedVariable instanceof ObjectField) {
                            seenObjects.add(((ObjectField)usedVariable).getObjectId());
                            assert !createdObjects.contains(((ObjectField)usedVariable).getObjectId());
                        }
                        if (usedVariable instanceof ArrayElement) {
                            seenObjects.add(((ArrayElement)usedVariable).getArrayId());
                            assert !createdObjects.contains(((ArrayElement)usedVariable).getArrayId());
                        }
                    }
                    */

                    if (dataDependenceVisitorsWriteAfterRead0 != null ||
                            dataDependenceVisitorsReadAfterWrite0 != null ||
                            pendingDataDependenceVisitorsReadAfterWrite0 != null) {
                        for (final Variable usedVariable: dynInfo.getUsedVariables()) {
                            // if we have WAR visitors, we inform them about a new dependence
                            if (dataDependenceVisitorsWriteAfterRead0 != null && !(usedVariable instanceof StackEntry)) {
                                final InstructionInstance lastWriterInst = lastWriter.get(usedVariable);

                                // avoid self-loops in the DDG (e.g. for IINC, which reads and writes to the same variable)
                                if (lastWriterInst != null && lastWriterInst != instance) {
                                    for (final DependencesVisitor vis: dataDependenceVisitorsWriteAfterRead0)
                                        vis.visitDataDependence(lastWriterInst, instance, usedVariable, DataDependenceType.WRITE_AFTER_READ);
                                }
                            }

                            // for RAW visitors, update the lastReaders
                            if (dataDependenceVisitorsReadAfterWrite0 != null
                                    || pendingDataDependenceVisitorsReadAfterWrite0 != null) {
                                List<InstructionInstance> readers = lastReaders.get(usedVariable);
                                if (readers == null) {
                                    readers = new ArrayList<InstructionInstance>(4);
                                    lastReaders.put(usedVariable, readers);
                                }
                                readers.add(instance);
                                // for each used variable, we have a pending RAW dependence
                                if (pendingDataDependenceVisitorsReadAfterWrite0 != null) {
                                    for (final DependencesVisitor vis: pendingDataDependenceVisitorsReadAfterWrite0)
                                        vis.visitPendingDataDependence(instance, usedVariable, DataDependenceType.READ_AFTER_WRITE);
                                }
                            }
                        }
                    }
                }

                for (final Entry<Long, Collection<Variable>> e: dynInfo.getCreatedObjects().entrySet()) {
                    /*
                    boolean added = createdObjects.add(e.getKey());
                    assert added;
                    */

                    for (final Variable var: e.getValue()) {
                        assert var instanceof ObjectField || var instanceof ArrayElement;
                        // clean up lastWriter if we have any WAR visitors
                        if (pendingDataDependenceVisitorsWriteAfterRead0 != null) {
                            InstructionInstance inst;
                            if ((inst = lastWriter.remove(var)) != null)
                                for (final DependencesVisitor vis: pendingDataDependenceVisitorsWriteAfterRead0)
                                    vis.discardPendingDataDependence(inst, var, DataDependenceType.WRITE_AFTER_READ);
                        } else if (dataDependenceVisitorsWriteAfterRead0 != null)
                            lastWriter.remove(var);
                        // clean up lastReaders if we have any RAW visitors
                        if (dataDependenceVisitorsReadAfterWrite0 != null || pendingDataDependenceVisitorsReadAfterWrite0 != null) {
                            List<InstructionInstance> instList;
                            if ((instList = lastReaders.remove(var)) != null) {
                                if (dataDependenceVisitorsReadAfterWrite0 != null)
                                    for (final DependencesVisitor vis: dataDependenceVisitorsReadAfterWrite0)
                                        for (final InstructionInstance instrInst: instList)
                                            vis.visitDataDependence(instrInst, instance, var, DataDependenceType.READ_AFTER_WRITE);
                                if (pendingDataDependenceVisitorsReadAfterWrite0 != null)
                                    for (final DependencesVisitor vis: pendingDataDependenceVisitorsReadAfterWrite0)
                                        for (final InstructionInstance instrInst: instList)
                                            vis.discardPendingDataDependence(instrInst, var, DataDependenceType.READ_AFTER_WRITE);
                            }
                        }
                    }
                    if (objectCreationVisitors0 != null)
                        for (final DependencesVisitor vis: objectCreationVisitors0)
                            vis.visitObjectCreation(e.getKey(), instance);
                }

                if (dynInfo.isCatchBlock()) {
                    currentFrame.atCatchBlockStart = instance;
                    currentFrame.interruptedControlFlow = true;
                } else if (currentFrame.atCatchBlockStart != null) {
                    currentFrame.atCatchBlockStart = null;
                }

                if (removedFrame != null)
                    cleanUpExecutionFrame(removedFrame, lastReaders, lastWriter,
                        pendingDataDependenceVisitorsWriteAfterRead0, pendingDataDependenceVisitorsReadAfterWrite0,
                        dataDependenceVisitorsWriteAfterRead0, dataDependenceVisitorsReadAfterWrite0);

                /*
                if (instance.getInstanceNr() % 1000000 == 0) {
                    for (Variable var: lastReaders.keySet()) {
                        if (var instanceof ObjectField) {
                            assert seenObjects.contains(((ObjectField)var).getObjectId());
                            assert !createdObjects.contains(((ObjectField)var).getObjectId());
                        }
                        if (var instanceof ArrayElement) {
                            assert seenObjects.contains(((ArrayElement)var).getArrayId());
                            assert !createdObjects.contains(((ArrayElement)var).getArrayId());
                        }
                        if (var instanceof StackEntry)
                            assert frames.contains(((StackEntry)var).getFrame());
                        if (var instanceof LocalVariable) {
                            assert frames.contains(((LocalVariable)var).getFrame());
                        }
                    }
                    for (Variable var: lastWriter.keySet()) {
                        if (var instanceof ObjectField) {
                            assert seenObjects.contains(((ObjectField)var).getObjectId());
                            assert !createdObjects.contains(((ObjectField)var).getObjectId());
                        }
                        if (var instanceof ArrayElement) {
                            assert seenObjects.contains(((ArrayElement)var).getArrayId());
                            assert !createdObjects.contains(((ArrayElement)var).getArrayId());
                        }
                        // we do not store the last writer of a stack entry
                        assert !(var instanceof StackEntry);
                        if (var instanceof LocalVariable) {
                            assert frames.contains(((LocalVariable)var).getFrame());
                        }
                    }
                }
                */
            }

            cleanUpMaps(lastWriter, lastReaders, pendingDataDependenceVisitorsWriteAfterRead0, pendingDataDependenceVisitorsReadAfterWrite0);

            Set<DependencesVisitor> allVisitors = new HashSet<DependencesVisitor>();
            if (dataDependenceVisitorsReadAfterWrite0 != null)
                allVisitors.addAll(Arrays.asList(dataDependenceVisitorsReadAfterWrite0));
            if (dataDependenceVisitorsWriteAfterRead0 != null)
                allVisitors.addAll(Arrays.asList(dataDependenceVisitorsWriteAfterRead0));
            if (controlDependenceVisitors0 != null)
                allVisitors.addAll(Arrays.asList(controlDependenceVisitors0));
            if (instructionVisitors0 != null)
                allVisitors.addAll(Arrays.asList(instructionVisitors0));
            if (pendingDataDependenceVisitorsReadAfterWrite0 != null)
                allVisitors.addAll(Arrays.asList(pendingDataDependenceVisitorsReadAfterWrite0));
            if (pendingDataDependenceVisitorsWriteAfterRead0 != null)
                allVisitors.addAll(Arrays.asList(pendingDataDependenceVisitorsWriteAfterRead0));
            if (pendingControlDependenceVisitors0 != null)
                allVisitors.addAll(Arrays.asList(pendingControlDependenceVisitors0));
            if (methodEntryLeaveVisitors0 != null)
                allVisitors.addAll(Arrays.asList(methodEntryLeaveVisitors0));
            if (objectCreationVisitors0 != null)
                allVisitors.addAll(Arrays.asList(objectCreationVisitors0));

            for (DependencesVisitor vis: allVisitors)
                vis.visitEnd(instance == null ? 0 : instance.getInstanceNr());
        } finally {
            if (iteratorThread != null)
                iteratorThread.interrupt();
        }
    }

    private static void cleanUpExecutionFrame(ExecutionFrame frame,
            Map<Variable, List<InstructionInstance>> lastReaders,
            Map<Variable, InstructionInstance> lastWriter,
            DependencesVisitor[] pendingDataDependenceVisitorsWriteAfterRead0,
            DependencesVisitor[] pendingDataDependenceVisitorsReadAfterWrite0,
            DependencesVisitor[] dataDependenceVisitorsWriteAfterRead0,
            DependencesVisitor[] dataDependenceVisitorsReadAfterWrite0) {
        for (Variable var: frame.getAllVariables()) {
            // lastWriter does not contain stack entries
            if (!(var instanceof StackEntry)) {
                if (pendingDataDependenceVisitorsWriteAfterRead0 != null) {
                    InstructionInstance inst = lastWriter.remove(var);
                    if (inst != null)
                        for (final DependencesVisitor vis: pendingDataDependenceVisitorsWriteAfterRead0)
                            vis.discardPendingDataDependence(inst, var, DataDependenceType.WRITE_AFTER_READ);
                } else if (dataDependenceVisitorsWriteAfterRead0 != null)
                    lastWriter.remove(var);
            }
            if (pendingDataDependenceVisitorsReadAfterWrite0 != null) {
                List<InstructionInstance> instList = lastReaders.remove(var);
                if (instList != null)
                    for (final DependencesVisitor vis: pendingDataDependenceVisitorsReadAfterWrite0)
                        for (final InstructionInstance instrInst: instList)
                            vis.discardPendingDataDependence(instrInst, var, DataDependenceType.READ_AFTER_WRITE);
            } else if (dataDependenceVisitorsReadAfterWrite0 != null)
                lastReaders.remove(var);
        }
    }

    private static void cleanUpMaps(final Map<Variable, InstructionInstance> lastWriter,
            final Map<Variable, List<InstructionInstance>> lastReaders,
            DependencesVisitor[] pendingDataDependenceVisitorsWriteAfterRead0,
            DependencesVisitor[] pendingDataDependenceVisitorsReadAfterWrite0) {
        if (pendingDataDependenceVisitorsWriteAfterRead0 != null) {
            for (final Entry<Variable, InstructionInstance> e: lastWriter.entrySet()) {
                final Variable var = e.getKey();
                assert !(var instanceof StackEntry);
                for (final DependencesVisitor vis: pendingDataDependenceVisitorsWriteAfterRead0)
                    vis.discardPendingDataDependence(e.getValue(), var, DataDependenceType.WRITE_AFTER_READ);
            }
        }
        lastWriter.clear();

        if (pendingDataDependenceVisitorsReadAfterWrite0 != null) {
            for (final Entry<Variable, List<InstructionInstance>> e: lastReaders.entrySet()) {
                final Variable var = e.getKey();
                for (final InstructionInstance inst: e.getValue())
                    for (final DependencesVisitor vis: pendingDataDependenceVisitorsReadAfterWrite0)
                        vis.discardPendingDataDependence(inst, var, DataDependenceType.READ_AFTER_WRITE);
            }
        }
        lastReaders.clear();
    }

    private Set<InstructionInstance> getInstanceIntersection(
            final Set<Instruction> instructions,
            final Set<InstructionInstance> instances) {

        if (instructions.isEmpty() || instances.isEmpty())
            return Collections.emptySet();

        Iterator<InstructionInstance> instanceIterator = instances.iterator();

        while (instanceIterator.hasNext()) {
            InstructionInstance inst = instanceIterator.next();
            if (instructions.contains(inst.getInstruction())) {
                HashSet<InstructionInstance> intersectInstances = new HashSet<InstructionInstance>();
                intersectInstances.add(inst);
                while (instanceIterator.hasNext()) {
                    inst = instanceIterator.next();
                    if (instructions.contains(inst.getInstruction()))
                        intersectInstances.add(inst);
                }
                return intersectInstances;
            }
        }

        return Collections.emptySet();
    }

    private static void computeControlDependences(final ReadMethod method, final IntegerMap<Set<Instruction>> controlDependences) {
        final Map<Instruction, Set<Instruction>> deps = ControlFlowAnalyser.getInstance().getInvControlDependences(method);
        for (final Entry<Instruction, Set<Instruction>> entry: deps.entrySet()) {
            final int index = entry.getKey().getIndex();
            assert !controlDependences.containsKey(index);
            controlDependences.put(index, entry.getValue());
        }
    }

}
