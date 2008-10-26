package de.unisb.cs.st.javaslicer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.tracer.traceResult.TraceResult.ThreadId;

public abstract class AbstractSlicingTest {

    protected static List<Instruction> getSlice(final File traceFile, final String thread, final String criterion)
            throws IllegalParameterException, IOException {
        final TraceResult trace = TraceResult.readFrom(traceFile);

        final SlicingCriterion sc = Slicer.readSlicingCriteria(criterion, trace.getReadClasses());

        long threadId = -1;
        for (final ThreadId t: trace.getThreads()) {
            if (thread.equals(t.getThreadName())) {
                    threadId = t.getThreadId();
                    break;
            }
        }

        assertTrue("Thread not found", threadId != -1);

        final Set<Instruction> slice = new Slicer(trace).getDynamicSlice(threadId, sc.getInstance());

        final List<Instruction> sliceList = new ArrayList<Instruction>(slice);
        Collections.sort(sliceList);

        return sliceList;
    }

    protected static void checkSlice(final List<Instruction> slice, final String[][] expected) {
        assertEquals("Slice size doesn't match", expected.length, slice.size());

        for (int i = 0; i < expected.length; ++i) {
            final Instruction instr = slice.get(i);
            final String[] expectedInst = expected[i];
            assertEquals("Internal test error", 3, expectedInst.length);

            assertEquals("Wrong method", expectedInst[0], instr.getMethod().getReadClass().getName()+"."+instr.getMethod().getName());
            assertEquals("Wrong line", expectedInst[1], Integer.toString(instr.getLineNumber()));
            assertEquals("Wrong instruction", expectedInst[2], instr.toString());
        }
    }
}
