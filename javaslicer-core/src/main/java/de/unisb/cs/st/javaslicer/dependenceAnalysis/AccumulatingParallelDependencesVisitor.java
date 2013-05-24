/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.dependenceAnalysis
 *    Class:     AccumulatingParallelDependencesVisitor
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/dependenceAnalysis/AccumulatingParallelDependencesVisitor.java
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
package de.unisb.cs.st.javaslicer.dependenceAnalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;

import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.variables.Variable;


public class AccumulatingParallelDependencesVisitor<InstanceType>
        implements DependencesVisitor<InstanceType> {


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

        @Override
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
    private static final byte UNTRACED_CALL = 13;

    private static class EventStamp<InstanceType> {
        private final byte[] events;
        private final InstanceType[] instructionInstances;
        private final ReadMethod[] methods;
        private final long[] longs;
        private final int[] ints;
        private final Variable[] variables;
        private volatile int remainingVisits;

		@SuppressWarnings("rawtypes")
		private static final AtomicIntegerFieldUpdater<EventStamp> remainingVisitsUpdater =
            AtomicIntegerFieldUpdater.newUpdater(EventStamp.class, "remainingVisits");

        public EventStamp(byte[] events,
                InstanceType[] instructionInstances,
                ReadMethod[] methods, long[] longs, int[] ints, Variable[] variables, int numVisitors) {
            this.events = events;
            this.instructionInstances = instructionInstances;
            this.methods = methods;
            this.longs = longs;
            this.ints = ints;
            this.variables = variables;
            this.remainingVisits = numVisitors;
        }

        public int getLength() {
            return this.events.length;
        }

        /**
         * Replay this stamp on a visitor.
         *
         * @return <code>true</code> if this was the last visitor on which the stamp had to be executed
         * @throws InterruptedException if any of the workers was interrupted
         */
        public boolean replay(DependencesVisitor<? super InstanceType> visitor) throws InterruptedException {
            // this synchronized is not there for locking (it is guaranteed that at most
            // one Thread calls replay() at any time), but for memory-synchronization
            synchronized (visitor) {
                int instructionPos = 0;
                int methodPos = 0;
                int longPos = 0;
                int intPos = 0;
                int variablePos = 0;
                byte[] events0 = this.events;
                InstanceType[] instructionInstances0 = this.instructionInstances;
                ReadMethod[] methods0 = this.methods;
                long[] longs0 = this.longs;
                int[] ints0 = this.ints;
                Variable[] variables0 = this.variables;
                int numEvents = events0.length;
                for (int eventPos = 0; eventPos < numEvents; ++eventPos) {
                    switch (events0[eventPos]) {
                        case INSTRUCTION_EXECUTION:
                            visitor.visitInstructionExecution(instructionInstances0[instructionPos++]);
                            break;
                        case METHOD_ENTRY:
                            visitor.visitMethodEntry(methods0[methodPos++], ints0[intPos++]);
                            break;
                        case METHOD_LEAVE:
                            visitor.visitMethodLeave(methods0[methodPos++], ints0[intPos++]);
                            break;
                        case OBJECT_CREATION:
                            visitor.visitObjectCreation(longs0[longPos++], instructionInstances0[instructionPos++]);
                            break;
                        case DATA_DEPENDENCE_RAW: {
                            List<Variable> fromVars;
                            int numVars = ints0[intPos++];
                            if (numVars == 0) {
                                fromVars = Collections.emptyList();
                            } else {
                                if (numVars == 1) {
                                    fromVars = Collections.singletonList(variables0[variablePos++]);
                                } else {
                                    fromVars = new ArrayList<Variable>(numVars);
                                    do {
                                        fromVars.add(variables0[variablePos++]);
                                    } while (--numVars != 0);
                                }
                            }
                            visitor.visitDataDependence(instructionInstances0[instructionPos++],
                                instructionInstances0[instructionPos++], fromVars, variables0[variablePos++],
                                DataDependenceType.READ_AFTER_WRITE);
                            break;
                        }
                        case DATA_DEPENDENCE_WAR:
                            visitor.visitDataDependence(instructionInstances0[instructionPos++],
                                instructionInstances0[instructionPos++], null, variables0[variablePos++],
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
                        case UNTRACED_CALL:
                            visitor.visitUntracedMethodCall(instructionInstances0[instructionPos++]);
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
        private final ConcurrentLinkedQueue<EventStamp<InstanceType>> stamps = new ConcurrentLinkedQueue<EventStamp<InstanceType>>();
        private final AtomicBoolean outstandingStamps = new AtomicBoolean(false);
        private final DependencesVisitor<? super InstanceType> visitor;
        private volatile CountDownLatch waitForFinishLatch = null;

        public OutstandingWork(DependencesVisitor<? super InstanceType> visitor) {
            assert visitor != null;
            this.visitor = visitor;
        }

        // returns true if this is the first stamp on the queue and there is no executing thread
        public boolean addWork(EventStamp<InstanceType> stamp) {
            this.stamps.add(stamp);
            return this.outstandingStamps.compareAndSet(false, true);
        }

        // executed by the worker threads:
        public void execute(Thread executingThread) throws InterruptedException {
            assert executingThread == Thread.currentThread();
            if (this.currentExecutingThread.compareAndSet(null, executingThread)) {
                while (true) {
                    EventStamp<InstanceType> stamp;
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
                                AccumulatingParallelDependencesVisitor.this.freeOutstanding.release(100 + stamp.getLength());
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

        public void finish() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            this.waitForFinishLatch = latch;
            if (!this.stamps.isEmpty() || this.currentExecutingThread != null) {
                latch.await();
            }
        }

    }

    private class Worker implements Runnable {

        public Worker() {
            // nop
        }

        @Override
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
            } catch (Throwable t) {
                AccumulatingParallelDependencesVisitor.this.workerException.compareAndSet(null, t);
            }
        }

    }

    private final Map<DependencesVisitor<? super InstanceType>, OutstandingWork> visitors
        = new HashMap<DependencesVisitor<? super InstanceType>, OutstandingWork>();

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

    private InstanceType[] instructionInstances = newInstanceTypeArray(1);
    private int instructionInstanceCount = 0;
    private ReadMethod[] methods = new ReadMethod[1];
    private int methodCount = 0;
    private long[] longs = new long[1];
    private int longCount = 0;
    private int[] ints = new int[1];
    private int intCount = 0;
    private Variable[] variables = new Variable[1];
    private int variableCount = 0;

    public AccumulatingParallelDependencesVisitor(int cacheSize, int maxOutstanding) {
        this(cacheSize, maxOutstanding, Runtime.getRuntime().availableProcessors(),
            Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority()-1));
    }

    @SuppressWarnings("unchecked")
    private InstanceType[] newInstanceTypeArray(int size) {
        return (InstanceType[]) new Object[size];
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

    public boolean addVisitor(DependencesVisitor<? super InstanceType> visitor) throws InterruptedException {
        if (this.visitors.containsKey(visitor))
            return false;

        if (this.eventCount > 0)
            flush();
        OutstandingWork newOW = new OutstandingWork(visitor);
        OutstandingWork oldValue = this.visitors.put(visitor, newOW);
        assert oldValue == null;
        return true;
    }

    public boolean removeVisitor(DependencesVisitor<? super InstanceType> visitor, boolean waitForFinish) throws InterruptedException {
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

    private EventStamp<InstanceType> toStamp() {
        byte[] stampEvents = new byte[this.eventCount];
        System.arraycopy(this.events, 0, stampEvents, 0, this.eventCount);
        this.eventCount = 0;

        InstanceType[] stampInstructionInstances = null;
        if (this.instructionInstanceCount > 0) {
            stampInstructionInstances = newInstanceTypeArray(this.instructionInstanceCount);
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

        int[] stampInts = null;
        if (this.intCount > 0) {
            stampInts = new int[this.intCount];
            System.arraycopy(this.ints, 0, stampInts, 0, this.intCount);
            this.intCount = 0;
        }

        Variable[] stampVariables = null;
        if (this.variableCount > 0) {
            stampVariables = new Variable[this.variableCount];
            System.arraycopy(this.variables, 0, stampVariables, 0, this.variableCount);
            this.variableCount = 0;
        }

        return new EventStamp<InstanceType>(stampEvents, stampInstructionInstances, stampMethods,
            stampLongs, stampInts, stampVariables, this.visitors.size());
    }

    @SuppressWarnings("unchecked")
    private void finish(final boolean interruptWorkers, boolean waitForWorkers) throws InterruptedException {
        if (interruptWorkers) {
            for (Thread t: this.workerThreads)
                t.interrupt();
            this.outstandingWorkQueue.clear();
        }

        this.outstandingWork.release(this.workerThreads.size());

        if (waitForWorkers) {
            for (Thread t: this.workerThreads)
                t.join();
            if (interruptWorkers)
                for (DependencesVisitor<? super InstanceType> vis : this.visitors.keySet())
                    vis.interrupted();
        } else {
            final Thread[] workerThreads0 = this.workerThreads.toArray(new Thread[this.workerThreads.size()]);
            final DependencesVisitor<? super InstanceType>[] visitors0 = this.visitors.keySet().toArray(
                (DependencesVisitor<? super InstanceType>[]) new DependencesVisitor<?>[this.visitors.size()]);
            new Thread(AccumulatingParallelDependencesVisitor.class.getSimpleName() + " visitor finish") {
                @Override
                public void run() {
                    try {
                        for (Thread t: workerThreads0)
                            t.join();
                        if (interruptWorkers)
                            for (DependencesVisitor<? super InstanceType> vis : visitors0)
                                vis.interrupted();
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }.start();
        }
    }

    @Override
	public void visitEnd(long numInstances) throws InterruptedException {
        if (!this.visitors.isEmpty()) {
            this.events[this.eventCount++] = END;
            addLong(numInstances);
            flush();
        }
        finish(false, true);
        checkException();
    }

    @Override
	public void visitInstructionExecution(InstanceType instance) throws InterruptedException {
        if (this.visitors.isEmpty())
            return;
        this.events[this.eventCount++] = INSTRUCTION_EXECUTION;
        addInstruction(instance);
        checkFull();
    }

    @Override
	public void visitMethodEntry(ReadMethod method, int stackDepth) throws InterruptedException {
        if (this.visitors.isEmpty())
            return;
        this.events[this.eventCount++] = METHOD_ENTRY;
        addMethod(method);
        addInt(stackDepth);
        checkFull();
    }

    @Override
	public void visitMethodLeave(ReadMethod method, int stackDepth) throws InterruptedException {
        if (this.visitors.isEmpty())
            return;
        this.events[this.eventCount++] = METHOD_LEAVE;
        addMethod(method);
        addInt(stackDepth);
        checkFull();
    }

    @Override
	public void visitObjectCreation(long objectId,
            InstanceType instrInstance) throws InterruptedException {
        if (this.visitors.isEmpty())
            return;
        this.events[this.eventCount++] = OBJECT_CREATION;
        addLong(objectId);
        addInstruction(instrInstance);
        checkFull();
    }

    @Override
	public void visitDataDependence(InstanceType from, InstanceType to,
            Collection<? extends Variable> fromVars, Variable toVar, DataDependenceType type) throws InterruptedException {
        if (this.visitors.isEmpty())
            return;
        if (type == DataDependenceType.READ_AFTER_WRITE) {
            this.events[this.eventCount++] = DATA_DEPENDENCE_RAW;
            int fromSize = fromVars.size();
            addInt(fromSize);
            if (fromSize != 0)
                for (Variable var : fromVars)
                    addVariable(var);
        } else {
            this.events[this.eventCount++] = DATA_DEPENDENCE_WAR;
        }
        addInstruction(from);
        addInstruction(to);
        addVariable(toVar);
        checkFull();
    }

    @Override
	public void visitPendingDataDependence(InstanceType from,
            Variable var, DataDependenceType type) throws InterruptedException {
        if (this.visitors.isEmpty())
            return;
        this.events[this.eventCount++] = type == DataDependenceType.READ_AFTER_WRITE
            ? PENDING_DATA_DEPENDENCE_RAW : PENDING_DATA_DEPENDENCE_WAR;
        addInstruction(from);
        addVariable(var);
        checkFull();
    }

    @Override
	public void discardPendingDataDependence(InstanceType from,
            Variable var, DataDependenceType type) throws InterruptedException {
        if (this.visitors.isEmpty())
            return;
        this.events[this.eventCount++] = type == DataDependenceType.READ_AFTER_WRITE
            ? DISCARD_PENDING_DATA_DEPENDENCE_RAW : DISCARD_PENDING_DATA_DEPENDENCE_WAR;
        addInstruction(from);
        addVariable(var);
        checkFull();
    }

    @Override
	public void visitControlDependence(InstanceType from, InstanceType to) throws InterruptedException {
        if (this.visitors.isEmpty())
            return;
        this.events[this.eventCount++] = CONTROL_DEPENDENCE;
        addInstruction(from);
        addInstruction(to);
        checkFull();
    }

    @Override
	public void visitPendingControlDependence(InstanceType from) throws InterruptedException {
        if (this.visitors.isEmpty())
            return;
        this.events[this.eventCount++] = PENDING_CONTROL_DEPENDENCE;
        addInstruction(from);
        checkFull();
    }

    @Override
	public void visitUntracedMethodCall(InstanceType instrInstance) throws InterruptedException {
        if (this.visitors.isEmpty())
            return;
        this.events[this.eventCount++] = UNTRACED_CALL;
        addInstruction(instrInstance);
        checkFull();
    }

    private void addInstruction(InstanceType instance) {
        if (this.instructionInstanceCount == this.instructionInstances.length) {
            InstanceType[] old = this.instructionInstances;
            this.instructionInstances = newInstanceTypeArray(2 * this.instructionInstanceCount);
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

    private void addInt(int value) {
        if (this.intCount == this.ints.length) {
            int[] old = this.ints;
            this.ints = new int[2 * this.intCount];
            System.arraycopy(old, 0, this.ints, 0, this.intCount);
        }
        this.ints[this.intCount++] = value;
    }

    private void addVariable(Variable var) {
        if (this.variableCount == this.variables.length) {
            Variable[] old = this.variables;
            this.variables = new Variable[2 * this.variableCount];
            System.arraycopy(old, 0, this.variables, 0, this.variableCount);
        }
        this.variables[this.variableCount++] = var;
    }

    private void checkFull() throws InterruptedException {
        if (this.eventCount == this.events.length) {
            flush();
        }
    }

    private void flush() throws InterruptedException {
        assert this.visitors.size() > 0;
        EventStamp<InstanceType> stamp = toStamp(); // resets all counters

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

        // each stamp itself counts for 100 events
        int permits = 100 + stamp.getLength();
        int numWorkers = this.workerThreads.size();
        if (numWorkers < this.maxNumWorkerThreads) {
            if (numWorkers == 0) {
                // create the first thread
                Thread newThread = this.threadFactory.newThread(new Worker());
                this.workerThreads.add(newThread);
                newThread.start();
                acquireAndCheckExceptions(permits);
            } else if (!this.freeOutstanding.tryAcquire(permits)) {
                // create a new thread
                Thread newThread = this.threadFactory.newThread(new Worker());
                this.workerThreads.add(newThread);
                newThread.start();
                // after the additional thread has started, we wait until the count
                // of outstanding work drops to 50%, but at most 2 seconds
                int limit = this.outstandingWork.availablePermits() / 2;
                acquireAndCheckExceptions(permits);
                long maxWait = System.currentTimeMillis() + 2000;
                while (System.currentTimeMillis() < maxWait &&
                        this.outstandingWork.availablePermits() > limit)
                    Thread.sleep(10);
            }
        } else {
            acquireAndCheckExceptions(permits);
        }

    }

    private void acquireAndCheckExceptions(int permits) throws InterruptedException {
        while (!this.freeOutstanding.tryAcquire(permits, 500, TimeUnit.MILLISECONDS))
            checkException();
    }

    private void checkException() throws InterruptedException {
        Throwable workerEx = this.workerException.get();
        if (workerEx != null) {
            finish(true, false);
            if (workerEx instanceof RuntimeException)
                throw (RuntimeException)workerEx;
            if (workerEx instanceof Error)
                throw (Error)workerEx;
            if (workerEx instanceof InterruptedException)
                throw (InterruptedException)workerEx;
            throw new RuntimeException(workerEx);
        }
    }

    @Override
	public void interrupted() throws InterruptedException {
        finish(true, false);
    }

}
