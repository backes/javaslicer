package de.unisb.cs.st.javaslicer;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.hammacher.util.Diff;
import de.hammacher.util.Diff.change;
import de.hammacher.util.DiffPrint;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.slicing.DirectSlicer;
import de.unisb.cs.st.javaslicer.slicing.SliceInstructionsCollector;
import de.unisb.cs.st.javaslicer.slicing.Slicer;
import de.unisb.cs.st.javaslicer.slicing.SlicingCriterion;
import de.unisb.cs.st.javaslicer.slicing.StaticSlicingCriterion;
import de.unisb.cs.st.javaslicer.traceResult.ThreadId;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;

public class Utils {

    protected static List<Instruction> getSlice(File traceFile, String thread, String criterion)
            throws IOException {
        TraceResult trace = TraceResult.readFrom(traceFile);

        List<SlicingCriterion> sc = StaticSlicingCriterion.parseAll(criterion, trace.getReadClasses());

        ThreadId threadId = null;
        for (ThreadId t: trace.getThreads()) {
            if (thread.equals(t.getThreadName())) {
                    threadId = t;
                    break;
            }
        }

        assertTrue("Thread not found", threadId != null);

        long beforeFirstRun = System.nanoTime();
        Slicer slicer = new Slicer(trace);
        SliceInstructionsCollector collector = new SliceInstructionsCollector();
        slicer.addSliceVisitor(collector);
        try {
            slicer.process(threadId, sc, true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("interrupted");
        }
        Set<Instruction> slice = new TreeSet<Instruction>(collector.getDynamicSlice());
        long afterFirstRun = System.nanoTime();

        Set<Instruction> slice2 = new TreeSet<Instruction>(new DirectSlicer(trace).getDynamicSlice(threadId, sc));
        long afterSecondRun = System.nanoTime();

        System.out.format("Slicer runtime: %.3f ms, direct slicer: %.3f ms%n",
            1e-6*(afterFirstRun-beforeFirstRun), 1e-6*(afterSecondRun-afterFirstRun));

        SliceEntry[] arr1 = new SliceEntry[slice.size()];
        Iterator<Instruction> sliceIt = slice.iterator();
        for (int i = 0; i < slice.size(); ++i) {
            arr1[i] = new SliceEntry(sliceIt.next());
        }
        SliceEntry[] arr2 = new SliceEntry[slice2.size()];
        Iterator<Instruction> sliceIt2 = slice2.iterator();
        for (int i = 0; i < slice2.size(); ++i) {
            arr2[i] = new SliceEntry(sliceIt2.next());
        }

        Diff differ = new Diff(arr1, arr2);
        change diff = differ.diff_2(false);
        if (diff != null) {
            StringWriter sw = new StringWriter();
            sw.append("both slicing methods should yield the same result!\n" +
            	"diff (Slicer -> DirectSlicer):\n\n");
            DiffPrint.SimplestPrint pr = new DiffPrint.SimplestPrint(arr1, arr2);
            pr.setOutput(sw);
            pr.print_script(diff);
            sw.append("\n\nSlicer:\n");
            for (SliceEntry s: arr1)
            	sw.append(s.toString()).append("\n");
            sw.append("\nDirectSlicer:\n");
            for (SliceEntry s: arr2)
            	sw.append(s.toString()).append("\n");
            fail(sw.toString());
        }
        assertTrue("diff should already have checked this: both slicing methods should yield the same result",
            new HashSet<Instruction>(slice).equals(new HashSet<Instruction>(slice2)));

        return new ArrayList<Instruction>(slice);
    }

}
