package de.unisb.cs.st.javaslicer.dependenceAnalysis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.variables.Variable;


public class AccumulatingParallelDependencesVisitor implements
        DependencesVisitor {


    private static class DefaultThreadFactory implements ThreadFactory {

        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final int newThreadPriority;

        public DefaultThreadFactory(int newThreadPriority) {
            SecurityManager s = System.getSecurityManager();
            this.group = (s != null)? s.getThreadGroup() :
                                 Thread.currentThread().getThreadGroup();
            this.newThreadPriority = newThreadPriority;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(this.group, r,
                                  "ParallelDependencesVisitor Worker " + this.threadNumber.getAndIncrement());
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != this.newThreadPriority)
                t.setPriority(this.newThreadPriority);
            return t;
        }

    }

    // event types:
    private static final byte INSTRUCTION_EXECUTION = 0;
    private static final byte METHOD_ENTRY = 1;
    private static final byte METHOD_LEAVE = 2;
    private static final byte OBJECT_CREATION = 3;
    private static final byte DATA_DEPENDENCE_RAW = 4;
    private static final byte DATA_DEPENDENCE_WAR = 5;
    private static final byte PENDING_DATA_DEPENDENCE_RAW = 6;
    private static final byte PENDING_DATA_DEPENDENCE_WAR = 7;
    private static final byte DISCARD_PENDING_DATA_DEPENDENCE_RAW = 8;
    private static final byte DISCARD_PENDING_DATA_DEPENDENCE_WAR = 9;
    private static final byte CONTROL_DEPENDENCE = 10;
    private static final byte PENDING_CONTROL_DEPENDENCE = 11;
    private static final byte END = 12;

    private static class EventStamp {
        private final byte[] events;
        private final InstructionInstance[] instructionInstances;
        private final ReadMethod[] methods;
        private final long[] longs;
        private final Variable[] variables;

        public EventStamp(byte[] events,
                InstructionInstance[] instructionInstances,
                ReadMethod[] methods, long[] longs, Variable[] variables) {
            this.events = events;
            this.instructionInstances = instructionInstances;
            this.methods = methods;
            this.longs = longs;
            this.variables = variables;
        }

        public int getLength() {
            return this.events.length;
        }

        public void replay(DependencesVisitor visitor) {
            // TODO check if this is necessary
            synchronized (visitor) {
                int instructionPos = 0;
                int methodPos = 0;
                int longPos = 0;
                int variablePos = 0;
                byte[] events0 = this.events;
                InstructionInstance[] instructionInstances0 = this.instructionInstances;
                ReadMethod[] methods0 = this.methods;
                long[] longs0 = this.longs;
                Variable[] variables0 = this.variables;
                int numEvents = events0.length;
                for (int eventPos = 0; eventPos < numEvents; ++eventPos) {
                    switch (events0[eventPos]) {
                        case INSTRUCTION_EXECUTION:
                            visitor.visitInstructionExecution(instructionInstances0[instructionPos++]);
                            break;
                        case METHOD_ENTRY:
                            visitor.visitMethodEntry(methods0[methodPos++]);
                            break;
                        case METHOD_LEAVE:
                            visitor.visitMethodLeave(methods0[methodPos++]);
                            break;
                        case OBJECT_CREATION:
                            visitor.visitObjectCreation(longs0[longPos++], instructionInstances0[instructionPos++]);
                            break;
                        case DATA_DEPENDENCE_RAW:
                            visitor.visitDataDependence(instructionInstances0[instructionPos++],
                                instructionInstances0[instructionPos++], variables0[variablePos++],
                                DataDependenceType.READ_AFTER_WRITE);
                            break;
                        case DATA_DEPENDENCE_WAR:
                            visitor.visitDataDependence(instructionInstances0[instructionPos++],
                                instructionInstances0[instructionPos++], variables0[variablePos++],
                                DataDependenceType.WRITE_AFTER_READ);
                            break;
                        case PENDING_DATA_DEPENDENCE_RAW:
                            visitor.visitPendingDataDependence(instructionInstances0[instructionPos++],
                                variables0[variablePos++], DataDependenceType.READ_AFTER_WRITE);
                            break;
                        case PENDING_DATA_DEPENDENCE_WAR:
                            visitor.visitPendingDataDependence(instructionInstances0[instructionPos++],
                                variables0[variablePos++], DataDependenceType.WRITE_AFTER_READ);
                            break;
                        case DISCARD_PENDING_DATA_DEPENDENCE_RAW:
                            visitor.discardPendingDataDependence(instructionInstances0[instructionPos++],
                                variables0[variablePos++], DataDependenceType.READ_AFTER_WRITE);
                            break;
                        case DISCARD_PENDING_DATA_DEPENDENCE_WAR:
                            visitor.discardPendingDataDependence(instructionInstances0[instructionPos++],
                                variables0[variablePos++], DataDependenceType.WRITE_AFTER_READ);
                            break;
                        case CONTROL_DEPENDENCE:
                            visitor.visitControlDependence(instructionInstances0[instructionPos++],
                                instructionInstances0[instructionPos++]);
                            break;
                        case PENDING_CONTROL_DEPENDENCE:
                            visitor.visitPendingControlDependence(instructionInstances0[instructionPos++]);
                            break;
                        case END:
                            visitor.visitEnd(longs0[longPos++]);
                            break;
                        default:
                            assert false;
                            break;
                    }
                }
                assert instructionPos == (this.instructionInstances == null ? 0 : this.instructionInstances.length);
                assert methodPos == (this.methods == null ? 0 : this.methods.length);
                assert longPos == (this.longs == null ? 0 : this.longs.length);
                assert variablePos == (this.variables == null ? 0 : this.variables.length);
            }
        }
    }

    private static class OutstandingWork implements Runnable {

        private final Semaphore freeBuffer;
        private final AtomicReference<Thread> executingThread = new AtomicReference<Thread>(null);
        private final ConcurrentLinkedQueue<EventStamp> workQueue = new ConcurrentLinkedQueue<EventStamp>();
        private final AtomicInteger workQueueLength = new AtomicInteger(0);
        private final DependencesVisitor visitor;

        public OutstandingWork(int maxBufferLength, DependencesVisitor visitor) {
            this.freeBuffer = new Semaphore(maxBufferLength);
            this.visitor = visitor;
        }

        // returns true if this is the first stamp on the queue and there is no executing thread
        public boolean addWork(EventStamp stamp) {
            int newEvents = stamp.getLength();
            acquireBufferSpace(newEvents);
            this.workQueue.add(stamp);
            int oldLength = this.workQueueLength.getAndIncrement();
            return oldLength == 0 && this.executingThread.get() == null;
        }

        private void acquireBufferSpace(int space) {
            if (!this.freeBuffer.tryAcquire(space)) {
                Thread thisThread = Thread.currentThread();
                if (this.executingThread.compareAndSet(null, thisThread)) {
                    int released = 0;
                    while (released < space && !this.freeBuffer.tryAcquire(space - released)) {
                        EventStamp execStamp = this.workQueue.poll();
                        assert execStamp != null;
                        execStamp.replay(this.visitor);
                        released += execStamp.getLength();
                    }
                    if (released > space)
                        this.freeBuffer.release(released - space);
                    assert this.executingThread.get() == thisThread;
                    this.executingThread.set(null);
                } else {
                    this.freeBuffer.acquireUninterruptibly(space);
                }
            }
        }

        // executed by the worker threads:
        public void run() {
            Thread thisThread = Thread.currentThread();
            if (this.executingThread.compareAndSet(null, thisThread)) {
                while (true) {
                    EventStamp stamp;
                    while ((stamp = this.workQueue.poll()) != null) {
                        int newQueueLength = this.workQueueLength.decrementAndGet();
                        assert newQueueLength >= 0;
                        stamp.replay(this.visitor);
                        this.freeBuffer.release(stamp.getLength());
                    }
                    assert this.executingThread.get() == thisThread;
                    this.executingThread.set(null);
                    // now check again whether there really is no more work (otherwise this OutstandingWork
                    // could not be appended to the outstandingWork queue)
                    if (this.workQueue.isEmpty() || !this.executingThread.compareAndSet(null, thisThread))
                        break;
                }
            }
        }

        public void finish(int maxBufferLength) {
            acquireBufferSpace(maxBufferLength);
        }

    }


    private final Map<DependencesVisitor, OutstandingWork> visitors
        = new HashMap<DependencesVisitor, OutstandingWork>();

    private final ExecutorService executor;

    private final int maxBufferLength;

    private final byte[] events;
    private int eventCount = 0;

    private InstructionInstance[] instructionInstances = new InstructionInstance[1];
    private int instructionInstanceCount = 0;
    private ReadMethod[] methods = new ReadMethod[1];
    private int methodCount = 0;
    private long[] longs = new long[1];
    private int longCount = 0;
    private Variable[] variables = new Variable[1];
    private int variableCount = 0;

    public AccumulatingParallelDependencesVisitor(int cacheSize, int maxBufferLength) {
        this(cacheSize, maxBufferLength, Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority()-1));
    }

    public AccumulatingParallelDependencesVisitor(int cacheSize, int maxBufferLength, int workerThreadPriority) {
        this(cacheSize, maxBufferLength, new ThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors(),
          60L, TimeUnit.SECONDS,
          new LinkedBlockingQueue<Runnable>(),
          new DefaultThreadFactory(workerThreadPriority)));
    }

    public AccumulatingParallelDependencesVisitor(int cacheSize, int maxBufferLength, ExecutorService executor) {
        if (maxBufferLength < cacheSize)
            throw new IllegalArgumentException("maxBufferLength must be >= cacheSize");
        this.events = new byte[cacheSize];
        this.maxBufferLength = maxBufferLength;
        this.executor = executor;
    }

    public boolean addVisitor(DependencesVisitor visitor) {
        if (this.visitors.containsKey(visitor))
            return false;

        if (this.eventCount > 0)
            flush();
        OutstandingWork newOW = new OutstandingWork(this.maxBufferLength, visitor);
        OutstandingWork oldValue = this.visitors.put(visitor, newOW);
        assert oldValue == null;
        return true;
    }

    public boolean removeVisitor(DependencesVisitor visitor, boolean waitForFinish) {
        if (!this.visitors.containsKey(visitor))
            return false;

        if (this.eventCount > 0)
            flush();

        // can only be removed AFTER flush()!!
        OutstandingWork ow = this.visitors.remove(visitor);
        if (ow == null) {
            assert false;
            return false;
        }

        if (waitForFinish) {
            ow.finish(this.maxBufferLength);
        }

        return true;
    }

    private EventStamp toStamp() {
        byte[] stampEvents = new byte[this.eventCount];
        System.arraycopy(this.events, 0, stampEvents, 0, this.eventCount);
        this.eventCount = 0;

        InstructionInstance[] stampInstructionInstances = null;
        if (this.instructionInstanceCount > 0) {
            stampInstructionInstances = new InstructionInstance[this.instructionInstanceCount];
            System.arraycopy(this.instructionInstances, 0, stampInstructionInstances, 0, this.instructionInstanceCount);
            this.instructionInstanceCount = 0;
        }

        ReadMethod[] stampMethods = null;
        if (this.methodCount > 0) {
            stampMethods = new ReadMethod[this.methodCount];
            System.arraycopy(this.methods, 0, stampMethods, 0, this.methodCount);
            this.methodCount = 0;
        }

        long[] stampLongs = null;
        if (this.longCount > 0) {
            stampLongs = new long[this.longCount];
            System.arraycopy(this.longs, 0, stampLongs, 0, this.longCount);
            this.longCount = 0;
        }

        Variable[] stampVariables = null;
        if (this.variableCount > 0) {
            stampVariables = new Variable[this.variableCount];
            System.arraycopy(this.variables, 0, stampVariables, 0, this.variableCount);
            this.variableCount = 0;
        }

        return new EventStamp(stampEvents, stampInstructionInstances, stampMethods,
            stampLongs, stampVariables);
    }

    private void finish() {
        this.executor.shutdown();
        boolean interrupted = false;
        while (!this.executor.isTerminated()) {
            try {
                this.executor.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted)
            Thread.currentThread().interrupt();
    }

    public void visitEnd(long numInstances) {
        this.events[this.eventCount++] = END;
        addLong(numInstances);
        flush();
        finish();
    }

    public void visitInstructionExecution(InstructionInstance instance) {
        this.events[this.eventCount++] = INSTRUCTION_EXECUTION;
        addInstruction(instance);
        checkFull();
    }

    public void visitMethodEntry(ReadMethod method) {
        this.events[this.eventCount++] = METHOD_ENTRY;
        addMethod(method);
        checkFull();
    }

    public void visitMethodLeave(ReadMethod method) {
        this.events[this.eventCount++] = METHOD_LEAVE;
        addMethod(method);
        checkFull();
    }

    public void visitObjectCreation(long objectId,
            InstructionInstance instrInstance) {
        this.events[this.eventCount++] = OBJECT_CREATION;
        addLong(objectId);
        addInstruction(instrInstance);
        checkFull();
    }

    public void visitDataDependence(InstructionInstance from,
            InstructionInstance to, Variable var, DataDependenceType type) {
        this.events[this.eventCount++] = type == DataDependenceType.READ_AFTER_WRITE
            ? DATA_DEPENDENCE_RAW : DATA_DEPENDENCE_WAR;
        addInstruction(from);
        addInstruction(to);
        addVariable(var);
        checkFull();
    }

    public void visitPendingDataDependence(InstructionInstance from,
            Variable var, DataDependenceType type) {
        this.events[this.eventCount++] = type == DataDependenceType.READ_AFTER_WRITE
            ? PENDING_DATA_DEPENDENCE_RAW : PENDING_DATA_DEPENDENCE_WAR;
        addInstruction(from);
        addVariable(var);
        checkFull();
    }

    public void discardPendingDataDependence(InstructionInstance from,
            Variable var, DataDependenceType type) {
        this.events[this.eventCount++] = type == DataDependenceType.READ_AFTER_WRITE
            ? DISCARD_PENDING_DATA_DEPENDENCE_RAW : DISCARD_PENDING_DATA_DEPENDENCE_WAR;
        addInstruction(from);
        addVariable(var);
        checkFull();
    }

    public void visitControlDependence(InstructionInstance from,
            InstructionInstance to) {
        this.events[this.eventCount++] = CONTROL_DEPENDENCE;
        addInstruction(from);
        addInstruction(to);
        checkFull();
    }

    public void visitPendingControlDependence(InstructionInstance from) {
        this.events[this.eventCount++] = PENDING_CONTROL_DEPENDENCE;
        addInstruction(from);
        checkFull();
    }

    private void addInstruction(InstructionInstance instance) {
        if (this.instructionInstanceCount == this.instructionInstances.length) {
            InstructionInstance[] old = this.instructionInstances;
            this.instructionInstances = new InstructionInstance[2 * this.instructionInstanceCount];
            System.arraycopy(old, 0, this.instructionInstances, 0, this.instructionInstanceCount);
        }
        this.instructionInstances[this.instructionInstanceCount++] = instance;
    }

    private void addMethod(ReadMethod method) {
        if (this.methodCount == this.methods.length) {
            ReadMethod[] old = this.methods;
            this.methods = new ReadMethod[2 * this.methodCount];
            System.arraycopy(old, 0, this.methods, 0, this.methodCount);
        }
        this.methods[this.methodCount++] = method;
    }

    private void addLong(long value) {
        if (this.longCount == this.longs.length) {
            long[] old = this.longs;
            this.longs = new long[2 * this.longCount];
            System.arraycopy(old, 0, this.longs, 0, this.longCount);
        }
        this.longs[this.longCount++] = value;
    }

    private void addVariable(Variable var) {
        if (this.variableCount == this.variables.length) {
            Variable[] old = this.variables;
            this.variables = new Variable[2 * this.variableCount];
            System.arraycopy(old, 0, this.variables, 0, this.variableCount);
        }
        this.variables[this.variableCount++] = var;
    }

    private void checkFull() {
        if (this.eventCount == this.events.length) {
            flush();
        }
    }

    private void flush() {
        EventStamp stamp = toStamp(); // resets all counters
        for (OutstandingWork ow: this.visitors.values()) {
            if (ow.addWork(stamp))
                this.executor.execute(ow);
        }
    }

}
