/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer
 *    Class:     AbstractSlicingTest
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/AbstractSlicingTest.java
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
package de.unisb.cs.st.javaslicer;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;
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

public abstract class AbstractSlicingTest {

    protected interface SliceEntryFilter {
        boolean keepEntry(SliceEntry entry);
    }

    protected static class SliceEntry {

        public final String method;
        public final String line;
        public final String instr;

        public SliceEntry(String method, String line, String instr) {
            this.method = method;
            this.line = line;
            this.instr = instr;
        }

        @Override
        public String toString() {
            return this.method+":"+this.line+" "+this.instr;
        }

        @Override
        public int hashCode() {
            int prime = 31;
            int result = 1;
            result = prime * result + this.instr.hashCode();
            result = prime * result + this.line.hashCode();
            result = prime * result + this.method.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SliceEntry other = (SliceEntry) obj;
            if (!this.instr.equals(other.instr))
                return false;
            if (!this.line.equals(other.line))
                return false;
            if (!this.method.equals(other.method))
                return false;
            return true;
        }

    }

    protected static List<Instruction> getSlice(String traceFilename, String thread, String criterion)
            throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        File traceFile = new File(AbstractSlicingTest.class.getResource(traceFilename).toURI());
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
        slicer.process(threadId, sc, true);
        Set<Instruction> slice = new TreeSet<Instruction>(collector.getDynamicSlice());
        long afterFirstRun = System.nanoTime();

        Set<Instruction> slice2 = new TreeSet<Instruction>(new DirectSlicer(trace).getDynamicSlice(threadId, sc));
        long afterSecondRun = System.nanoTime();

        System.out.format("Slicer runtime: %.3f ms, direct slicer: %.3f ms%n",
            1e-6*(afterFirstRun-beforeFirstRun), 1e-6*(afterSecondRun-afterFirstRun));

        SliceEntry[] arr1 = new SliceEntry[slice.size()];
        Iterator<Instruction> sliceIt = slice.iterator();
        for (int i = 0; i < slice.size(); ++i) {
            arr1[i] = instrToSliceEntry(sliceIt.next());
        }
        SliceEntry[] arr2 = new SliceEntry[slice2.size()];
        Iterator<Instruction> sliceIt2 = slice2.iterator();
        for (int i = 0; i < slice2.size(); ++i) {
            arr2[i] = instrToSliceEntry(sliceIt2.next());
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
            Assert.fail(sw.toString());
        }
        assertTrue("diff should already have checked this: both slicing methods should yield the same result",
            new HashSet<Instruction>(slice).equals(new HashSet<Instruction>(slice2)));

        return new ArrayList<Instruction>(slice);
    }

    private static Pattern sliceEntryPattern = Pattern.compile("^([^ ]+):([0-9]+) (.*)$");

    protected void checkSlice(List<Instruction> slice, String[] expected) {
        checkSlice("", slice, expected);
    }

    protected void checkSlice(String prefix, List<Instruction> slice, String[] expected) {
        SliceEntry[] gotEntries = new SliceEntry[slice.size()];
        for (int i = 0; i < slice.size(); ++i) {
            gotEntries[i] = instrToSliceEntry(slice.get(i));
        }

        SliceEntryFilter filter = getSliceEntryFilter();
        if (filter != null) {
            int keptEntries = 0;
            for (int idx = 0; idx < gotEntries.length; ++idx) {
                if (!filter.keepEntry(gotEntries[idx]))
                    continue;
                if (keptEntries != idx)
                    gotEntries[keptEntries] = gotEntries[idx];
                ++keptEntries;
            }
            if (keptEntries != gotEntries.length)
                gotEntries = Arrays.copyOf(gotEntries, keptEntries);
        }

        SliceEntry[] expectedEntries = new SliceEntry[expected.length];
        for (int i = 0; i < expected.length; ++i) {
            Matcher m = sliceEntryPattern.matcher(expected[i]);
            Assert.assertTrue(prefix + "Error in expected output", m.matches());
            expectedEntries[i] = new SliceEntry(m.group(1), m.group(2), m.group(3));
        }

        Diff differ = new Diff(expectedEntries, gotEntries);
        change diff = differ.diff_2(false);
        if (diff == null)
            return;

        StringWriter output = new StringWriter();
        output.append(prefix + "Slice differs from expected slice:").append(System.getProperty("line.separator"));

        if (expectedEntries.length != gotEntries.length) {
            output.append("Expected " + expectedEntries.length + " entries, got " + gotEntries.length + "." +
                    System.getProperty("line.separator"));
        }

        DiffPrint.SimplestPrint diffPrinter = new DiffPrint.SimplestPrint(expectedEntries, gotEntries);
        diffPrinter.setOutput(output);
        diffPrinter.print_script(diff);

        Assert.fail(output.toString());
    }

    // can be overwritten by subclasses
    protected SliceEntryFilter getSliceEntryFilter() {
        return null;
    }

    private static SliceEntry instrToSliceEntry(Instruction instr) {
        return new SliceEntry(
                instr.getMethod().getReadClass().getName()+"."+instr.getMethod().getName(),
                Integer.toString(instr.getLineNumber()),
                instr.toString());
    }
}
