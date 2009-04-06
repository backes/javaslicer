package de.unisb.cs.st.javaslicer.dependenceAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;

import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.variables.Variable;


public class AccumulatingParallelDependencesVisitor implements
        DependencesVisitor {


    private static class DefaultThreadFactory implements ThreadFactory {

        private static final AtomicInteger nextFactoryId = new AtomicInteger(0);
        private final int factoryId = nextFactoryId.getAndIncrement();

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
                                  "ParallelDependencesVisitor"+this.factoryId+" Worker"+this.threadNumber.getAndIncrement());
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
        private volatile int remainingVisits;

        private static final AtomicIntegerFieldUpdater<EventStamp> remainingVisitsUpdater =
            AtomicIntegerFieldUpdater.newUpdater(EventStamp.class, "remainingVisits");

        public EventStamp(byte[] events,
                InstructionInstance[] instructionInstances,
                ReadMethod[] methods, long[] longs, Variable[] variables, int numVisitors) {
            this.events = events;
            this.instructionInstances = instructionInstances;
            this.methods = methods;
            this.longs = longs;
            this.variables = variables;
            this.remainingVisits = numVisitors;
        }

        public int getLength() {
            return this.events.length;
        }

        /**
         * Replay this stamp on a visitor.
         *
         * @param visitor
         * @return <code>true</code> if this was the last visitor on which the stamp had to be executed
         */
        public boolean replay(DependencesVisitor visitor) {
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
                assert this.remainingVisits > 0;
                return remainingVisitsUpdater.decrementAndGet(this) == 0;
            }
        }
    }

    private class OutstandingWork {

        private final AtomicReference<Thread> currentExecutingThread = new AtomicReference<Thread>(null);
        private final ConcurrentLinkedQueue<EventStamp> stamps = new ConcurrentLinkedQueue<EventStamp>();
        private final AtomicBoolean outstandingStamps = new AtomicBoolean(false);
        private final DependencesVisitor visitor;
        private volatile CountDownLatch waitForFinishLatch = null;

        public OutstandingWork(DependencesVisitor visitor) {
            assert visitor != null;
            this.visitor = visitor;
        }

        // returns true if this is the first stamp on the queue and there is no executing thread
        public boolean addWork(EventStamp stamp) {
            this.stamps.add(stamp);
            return this.outstandingStamps.compareAndSet(false, true);
        }

        // executed by the worker threads:
        public void execute(Thread executingThread) {
            assert executingThread == Thread.currentThread();
            if (this.currentExecutingThread.compareAndSet(null, executingThread)) {
                while (true) {
                    EventStamp stamp;
                    while ((stamp = this.stamps.poll()) != null) {
                        boolean stampReady = false;
                        try {
                            stampReady = stamp.replay(this.visitor);
                        } catch (RuntimeException e) {
                            stampReady = true;
                            AccumulatingParallelDependencesVisitor.this.workerException.compareAndSet(null, e);
                            throw e;
                        } catch (Error e) {
                            stampReady = true;
                            AccumulatingParallelDependencesVisitor.this.workerException.compareAndSet(null, e);
                            throw e;
                        } finally {
                            if (stampReady)
                                AccumulatingParallelDependencesVisitor.this.freeOutstanding.release(stamp.getLength());
                        }
                    }
                    assert this.currentExecutingThread.get() == executingThread;
                    this.currentExecutingThread.set(null);
                    this.outstandingStamps.set(false);
                    CountDownLatch latch = this.waitForFinishLatch;
                    if (latch != null && this.stamps.isEmpty())
                        latch.countDown();
                    // now check again whether there really is no more work (otherwise it could happen that
                    // this OutstandingWork is not appended to the outstandingWork queue even if there is
                    // more work to do and nobody executes it)
                    if (this.stamps.isEmpty() || !this.currentExecutingThread.compareAndSet(null, executingThread))
                        break;
                }
            }
        }

        public void finish() {
            CountDownLatch latch = new CountDownLatch(1);
            this.waitForFinishLatch = latch;
            if (!this.stamps.isEmpty() || this.currentExecutingThread != null) {
                boolean interrupted = false;
                while (true) {
                    try {
                        latch.await();
                        break;
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
                if (interrupted)
                    Thread.currentThread().interrupt();
            }
        }

    }

    private class Worker implements Runnable {

        public Worker() {
            // nop
        }

        public void run() {
            Thread currentThread = Thread.currentThread();
            try {
                while (true) {
                    AccumulatingParallelDependencesVisitor.this.outstandingWork.acquire();
                    OutstandingWork work = AccumulatingParallelDependencesVisitor.this.outstandingWorkQueue.poll();
                    if (work == null)
                        break; // this is the signal for the worker thread to terminate
                    work.execute(currentThread);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Worker threads should never be interrupted", e);
            } catch (Throwable t) {
                AccumulatingParallelDependencesVisitor.this.workerException.compareAndSet(null, t);
            }
        }

    }

    private final Map<DependencesVisitor, OutstandingWork> visitors
        = new HashMap<DependencesVisitor, OutstandingWork>();

    private final ThreadFactory threadFactory;
    private final List<Thread> workerThreads = new ArrayList<Thread>(4);
    private final int maxNumWorkerThreads;

    // package-visible
    final AtomicReference<Throwable> workerException = new AtomicReference<Throwable>();

    // package-visible
    final Semaphore freeOutstanding;
    final Queue<OutstandingWork> outstandingWorkQueue = new ConcurrentLinkedQueue<OutstandingWork>();
    final Semaphore outstandingWork = new Semaphore(0);

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

    public AccumulatingParallelDependencesVisitor(int cacheSize, int maxOutstanding) {
        this(cacheSize, maxOutstanding, Runtime.getRuntime().availableProcessors(),
            Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority()-1));
    }

    public AccumulatingParallelDependencesVisitor(int cacheSize, int maxOutstanding,
            int maxNumWorkerThreads, int workerThreadPriority) {
        this(cacheSize, maxOutstanding, maxNumWorkerThreads, new DefaultThreadFactory(workerThreadPriority));
    }

    public AccumulatingParallelDependencesVisitor(int cacheSize, int maxOutstanding,
            int maxNumWorkerThreads, ThreadFactory threadFactory) {
        if (maxOutstanding < cacheSize)
            throw new IllegalArgumentException("maxOutstanding must be >= cacheSize");
        this.events = new byte[cacheSize];
        this.freeOutstanding = new Semaphore(maxOutstanding);
        this.threadFactory = threadFactory;
        this.maxNumWorkerThreads = maxNumWorkerThreads;
    }

    public boolean addVisitor(DependencesVisitor visitor) {
        if (this.visitors.containsKey(visitor))
            return false;

        if (this.eventCount > 0)
            flush();
        OutstandingWork newOW = new OutstandingWork(visitor);
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

        if (waitForFinish)
            ow.finish();

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
            stampLongs, stampVariables, this.visitors.size());
    }

    private void finish() {
        this.outstandingWork.release(this.workerThreads.size());

        boolean interrupted = false;
        for (Thread t: this.workerThreads) {
            while (true) {
                try {
                    t.join();
                    break;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        }
        if (interrupted)
            Thread.currentThread().interrupt();
    }

    public void visitEnd(long numInstances) {
        if (!this.visitors.isEmpty()) {
            this.events[this.eventCount++] = END;
            addLong(numInstances);
            flush();
        }
        finish();
        checkException();
    }

    public void visitInstructionExecution(InstructionInstance instance) {
        if (this.visitors.isEmpty())
            return;
        this.events[this.eventCount++] = INSTRUCTION_EXECUTION;
        addInstruction(instance);
        checkFull();
    }

    public void visitMethodEntry(ReadMethod method) {
        if (this.visitors.isEmpty())
            return;
        this.events[this.eventCount++] = METHOD_ENTRY;
        addMethod(method);
        checkFull();
    }

    public void visitMethodLeave(ReadMethod method) {
        if (this.visitors.isEmpty())
            return;
        this.events[this.eventCount++] = METHOD_LEAVE;
        addMethod(method);
        checkFull();
    }

    public void visitObjectCreation(long objectId,
            InstructionInstance instrInstance) {
        if (this.visitors.isEmpty())
            return;
        this.events[this.eventCount++] = OBJECT_CREATION;
        addLong(objectId);
        addInstruction(instrInstance);
        checkFull();
    }

    public void visitDataDependence(InstructionInstance from,
            InstructionInstance to, Variable var, DataDependenceType type) {
        if (this.visitors.isEmpty())
            return;
        this.events[this.eventCount++] = type == DataDependenceType.READ_AFTER_WRITE
            ? DATA_DEPENDENCE_RAW : DATA_DEPENDENCE_WAR;
        addInstruction(from);
        addInstruction(to);
        addVariable(var);
        checkFull();
    }

    public void visitPendingDataDependence(InstructionInstance from,
            Variable var, DataDependenceType type) {
        if (this.visitors.isEmpty())
            return;
        this.events[this.eventCount++] = type == DataDependenceType.READ_AFTER_WRITE
            ? PENDING_DATA_DEPENDENCE_RAW : PENDING_DATA_DEPENDENCE_WAR;
        addInstruction(from);
        addVariable(var);
        checkFull();
    }

    public void discardPendingDataDependence(InstructionInstance from,
            Variable var, DataDependenceType type) {
        if (this.visitors.isEmpty())
            return;
        this.events[this.eventCount++] = type == DataDependenceType.READ_AFTER_WRITE
            ? DISCARD_PENDING_DATA_DEPENDENCE_RAW : DISCARD_PENDING_DATA_DEPENDENCE_WAR;
        addInstruction(from);
        addVariable(var);
        checkFull();
    }

    public void visitControlDependence(InstructionInstance from,
            InstructionInstance to) {
        if (this.visitors.isEmpty())
            return;
        this.events[this.eventCount++] = CONTROL_DEPENDENCE;
        addInstruction(from);
        addInstruction(to);
        checkFull();
    }

    public void visitPendingControlDependence(InstructionInstance from) {
        if (this.visitors.isEmpty())
            return;
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
        assert this.visitors.size() > 0;
        EventStamp stamp = toStamp(); // resets all counters

        // the memory for the new stamp has already been allocated, so we can first
        // add it to the work queue, and afterwards claim the freeOutstanding space
        int added = 0;
        for (OutstandingWork ow: this.visitors.values()) {
            if (ow.addWork(stamp)) {
                this.outstandingWorkQueue.add(ow);
                ++added;
            }
        }

        if (added != 0)
            this.outstandingWork.release(added);

        checkException();

        int permits = stamp.getLength();
        int numWorkers = this.workerThreads.size();
        if (numWorkers < this.maxNumWorkerThreads) {
            if (numWorkers == 0) {
                // create the first thread
                Thread newThread = this.threadFactory.newThread(new Worker());
                this.workerThreads.add(newThread);
                newThread.start();
                this.freeOutstanding.acquireUninterruptibly(permits);
            } else if (!this.freeOutstanding.tryAcquire(permits)) {
                // create a new thread
                Thread newThread = this.threadFactory.newThread(new Worker());
                this.workerThreads.add(newThread);
                newThread.start();
                // after the additional thread has started, we wait until the count
                // of outstanding work drops to 50%, but at most 2 seconds
                int limit = this.outstandingWork.availablePermits() / 2;
                this.freeOutstanding.acquireUninterruptibly(permits);
                long maxWait = System.currentTimeMillis() + 2000;
                try {
                    while (System.currentTimeMillis() < maxWait &&
                            this.outstandingWork.availablePermits() > limit) {
                        Thread.sleep(10);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } else {
            this.freeOutstanding.acquireUninterruptibly(permits);
        }

    }

    private void checkException() {
        Throwable workerEx = this.workerException.get();
        if (workerEx != null) {
            finish();
            if (workerEx instanceof RuntimeException)
                throw (RuntimeException)workerEx;
            if (workerEx instanceof Error)
                throw (Error)workerEx;
            throw new RuntimeException(workerEx);
        }
    }

}
