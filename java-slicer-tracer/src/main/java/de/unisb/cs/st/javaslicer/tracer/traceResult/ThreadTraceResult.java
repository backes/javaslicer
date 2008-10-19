package de.unisb.cs.st.javaslicer.tracer.traceResult;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.javaslicer.tracer.UntracedThread;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction.Type;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.AbstractInstruction;
import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences.ConstantThreadTraces;
import de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences.ConstantTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences.ConstantTraceSequence.ConstantIntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences.ConstantTraceSequence.ConstantLongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.IntegerMap;
import de.unisb.cs.st.javaslicer.tracer.util.IntegerToLongMap;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileReader;

public class ThreadTraceResult {

    public static final boolean WRITE_ITERATION_DEBUG_FILE = true; // TODO

    private final long threadId;
    private final String threadName;
    protected final IntegerMap<ConstantTraceSequence> sequences;
    protected final int lastInstructionIndex;
    private final int lastStackDepth;

    private final TraceResult traceResult;

    public ThreadTraceResult(final long threadId, final String threadName,
            final IntegerMap<ConstantTraceSequence> sequences, final int lastInstructionIndex,
            final TraceResult traceResult, final int lastStackDepth) {
        this.threadId = threadId;
        this.threadName = threadName;
        this.sequences = sequences;
        this.lastInstructionIndex = lastInstructionIndex;
        this.traceResult = traceResult;
        this.lastStackDepth = lastStackDepth;
    }

    public long getThreadId() {
        return this.threadId;
    }

    public String getThreadName() {
        return this.threadName;
    }

    public static ThreadTraceResult readFrom(final DataInputStream in, final TraceResult traceResult, final MultiplexedFileReader file) throws IOException {
        final long threadId = in.readLong();
        final String name = in.readUTF();
        final ConstantThreadTraces threadTraces = ConstantThreadTraces.readFrom(in);
        int numSequences = in.readInt();
        final IntegerMap<ConstantTraceSequence> sequences = new IntegerMap<ConstantTraceSequence>(numSequences*4/3+1);
        while (numSequences-- > 0) {
            final int nr = in.readInt();
            final ConstantTraceSequence seq = threadTraces.readSequence(in, file);
            if (sequences.put(nr, seq) != null)
                throw new IOException("corrupted data");
        }
        final int lastInstructionIndex = in.readInt();
        final int lastStackDepth = in.readInt();
        return new ThreadTraceResult(threadId, name, sequences, lastInstructionIndex, traceResult, lastStackDepth);
    }

    public Iterator<Instance> getBackwardIterator() {
        return new BackwardInstructionIterator();
    }

    public Instruction findInstruction(final int instructionIndex, final ReadClass tryClass, final ReadMethod tryMethod) {
        // note: when the instructionIndex is illegal, we will notice that in the last
        // step. all other steps should perform normally

        final ReadClass instrClass;
        final ReadMethod instrMethod;

        if (tryMethod == null || tryMethod.getInstructionNumberStart() > instructionIndex
                || tryMethod.getInstructionNumberEnd() <= instructionIndex) {
            int left, right, mid;
            if (tryClass == null || tryClass.getInstructionNumberStart() > instructionIndex
                    || tryClass.getInstructionNumberEnd() <= instructionIndex) {
                // first search for the correct class
                left = 0;
                right = this.traceResult.getReadClasses().size();
                while ((mid = (left + right) / 2) != left) {
                    final ReadClass midClass = this.traceResult.getReadClasses().get(mid);
                    if (midClass.getInstructionNumberStart() <= instructionIndex) {
                        left = mid;
                    } else { // midClass.getInstructionNumberStart() > instructionIndex
                        right = mid;
                    }
                }

                // now we know that mid and left both point to the class we need
                instrClass = this.traceResult.getReadClasses().get(left);
            } else
                instrClass = tryClass;
            final ArrayList<ReadMethod> methods = instrClass.getMethods();

            // and now we search for the correct method
            left = 0;
            right = methods.size();
            while ((mid = (left + right) / 2) != left) {
                final ReadMethod midMethod = methods.get(mid);
                if (midMethod.getInstructionNumberStart() <= instructionIndex) {
                    left = mid;
                } else { // midMethod.getInstructionNumberStart() > instructionIndex
                    right = mid;
                }
            }

            // yeah: we have the correct method
            instrMethod = methods.get(left);
        } else
            instrMethod = tryMethod;

        // now search for the instruction
        final List<AbstractInstruction> instructions = instrMethod.getInstructions();

        // we can just compute the offset of the instruction
        final int offset = instructionIndex - instrMethod.getInstructionNumberStart();
        if (offset < 0 || offset >= instructions.size())
            return null; // no instruction found

        final Instruction instr = instructions.get(offset);
        assert instr.getIndex() == instructionIndex;

        return instr;
    }

    public class BackwardInstructionIterator implements Iterator<Instance> {

        private Instance nextInstruction;
        private final IntegerMap<Iterator<Integer>> integerSequenceBackwardIterators;
        private final IntegerMap<Iterator<Long>> longSequenceBackwardIterators;
        private final IntegerToLongMap instructionNextOccurenceNumber;

        private int stackDepth;

        private int instructionCount = 0;
        private int additionalInstructionCount = 0;
        private final PrintWriter debugFileWriter;

        @SuppressWarnings("synthetic-access")
        public BackwardInstructionIterator() throws TracerException {
            this.integerSequenceBackwardIterators = new IntegerMap<Iterator<Integer>>();
            this.longSequenceBackwardIterators = new IntegerMap<Iterator<Long>>();
            this.instructionNextOccurenceNumber = new IntegerToLongMap();
            PrintWriter debugFileWriterTmp = null;
            if (WRITE_ITERATION_DEBUG_FILE) {
                try {
                    debugFileWriterTmp = new PrintWriter(new FileOutputStream(new File("iteration_debug.log")));
                    Runtime.getRuntime().addShutdownHook(new UntracedThread() {
                        @Override
                        public void run() {
                            BackwardInstructionIterator.this.debugFileWriter.close();
                        }
                    });
                } catch (final FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            this.debugFileWriter = debugFileWriterTmp;
            this.stackDepth = ThreadTraceResult.this.lastStackDepth;
            try {
                this.nextInstruction = getNextInstruction(null, ThreadTraceResult.this.lastInstructionIndex);
            } catch (final EOFException e) {
                this.nextInstruction = null;
            }
        }

        public boolean hasNext() {
            final boolean hasNext = this.nextInstruction != null;
            if (!hasNext && this.debugFileWriter != null)
                this.debugFileWriter.close();
            return hasNext;
        }

        public Instance next() throws TracerException {
            if (this.nextInstruction == null)
                throw new NoSuchElementException();
            final Instance old = this.nextInstruction;
            try {
                this.nextInstruction = getNextInstruction(this.nextInstruction);
            } catch (final EOFException e) {
                this.nextInstruction = null;
            }
            return old;
        }

        private Instance getNextInstruction(final Instruction old) throws TracerException, EOFException {
            return getNextInstruction(old.getMethod(), old.getBackwardInstructionIndex(this));
        }

        private Instance getNextInstruction(final ReadMethod oldMethod, final int nextIndex) throws TracerException, EOFException {
            int index = nextIndex;
            while (true) {
                if (WRITE_ITERATION_DEBUG_FILE) {
                    this.debugFileWriter.println(index);
                }
                final Instruction backwardInstruction = findInstruction(index,
                        oldMethod == null ? null : oldMethod.getReadClass(), oldMethod);
                if (backwardInstruction == null)
                    return null;
                if (backwardInstruction == backwardInstruction.getMethod().getMethodEntryLabel()) {
                    --this.stackDepth;
                    assert this.stackDepth >= 0 : "enter method occured more often than leave method";
                } else if (backwardInstruction == backwardInstruction.getMethod().getMethodLeaveLabel()) {
                    ++this.stackDepth;
                } else if (backwardInstruction.getType() == Type.SIMPLE) {
                    switch (backwardInstruction.getOpcode()) {
                    case Opcodes.IRETURN: case Opcodes.LRETURN: case Opcodes.FRETURN:
                    case Opcodes.DRETURN: case Opcodes.ARETURN: case Opcodes.RETURN:
                        ++this.stackDepth;
                    }
                }
                final Instance instance = backwardInstruction.getNextInstance(this);

                if (instance == null) {
                    ++this.additionalInstructionCount;
                } else {
                    ++this.instructionCount;
                    return instance;
                }
                index = backwardInstruction.getBackwardInstructionIndex(this);
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public long getNextLong(final int seqIndex) throws TracerException {
            Iterator<Long> it = this.longSequenceBackwardIterators.get(seqIndex);
            if (it == null) {
                try {
                    it = ((ConstantLongTraceSequence)ThreadTraceResult.this.sequences.get(seqIndex)).backwardIterator();
                } catch (final IOException e) {
                    throw new TracerException(e);
                }
                this.longSequenceBackwardIterators.put(seqIndex, it);
            }
            if (!it.hasNext())
                throw new TracerException("corrupted data (cannot trace backwards)");
            return it.next();
        }

        public int getNextInteger(final int seqIndex) throws TracerException {
            Iterator<Integer> it = this.integerSequenceBackwardIterators.get(seqIndex);
            if (it == null) {
                try {
                    it = ((ConstantIntegerTraceSequence)ThreadTraceResult.this.sequences.get(seqIndex)).backwardIterator();
                } catch (final IOException e) {
                    throw new TracerException(e);
                }
                this.integerSequenceBackwardIterators.put(seqIndex, it);
            }
            if (!it.hasNext())
                throw new TracerException("corrupted data (cannot trace backwards)");
            return it.next();
        }

        public long getNextInstructionOccurenceNumber(final int instructionIndex) {
            final long nr = this.instructionNextOccurenceNumber.incrementAndGet(instructionIndex, 1);
            return nr - 1;
        }

        public int getNoInstructions() {
            return this.instructionCount;
        }

        public int getNoAdditionalInstructions() {
            return this.additionalInstructionCount;
        }

        public int getStackDepth() {
            return this.stackDepth;
        }

    }

}
