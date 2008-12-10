package de.unisb.cs.st.javaslicer;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.tracer.traceResult.ThreadTraceResult.ThreadId;
import de.unisb.cs.st.javaslicer.util.Diff;
import de.unisb.cs.st.javaslicer.util.DiffPrint;
import de.unisb.cs.st.javaslicer.util.Diff.change;

public abstract class AbstractSlicingTest {

    private static class SliceEntry {

        private final String method;
        private final String line;
        private final String instr;

        public SliceEntry(final String method, final String line, final String instr) {
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
            final int prime = 31;
            int result = 1;
            result = prime * result + this.instr.hashCode();
            result = prime * result + this.line.hashCode();
            result = prime * result + this.method.hashCode();
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final SliceEntry other = (SliceEntry) obj;
            if (!this.instr.equals(other.instr))
                return false;
            if (!this.line.equals(other.line))
                return false;
            if (!this.method.equals(other.method))
                return false;
            return true;
        }

    }

    protected static List<Instruction> getSlice(final String traceFilename, final String thread, final String criterion)
            throws IllegalParameterException, IOException, URISyntaxException {
        final File traceFile = new File(AbstractSlicingTest.class.getResource(traceFilename).toURI());
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

    private static Pattern sliceEntryPattern = Pattern.compile("^([^ ]+):([0-9]+) (.*)$");

    protected static void checkSlice(final List<Instruction> slice, final String[] expected) {
        final SliceEntry[] gotEntries = new SliceEntry[slice.size()];
        for (int i = 0; i < slice.size(); ++i) {
            final Instruction instr = slice.get(i);
            gotEntries[i] = new SliceEntry(
                    instr.getMethod().getReadClass().getName()+"."+instr.getMethod().getName(),
                    Integer.toString(instr.getLineNumber()),
                    instr.toString());
        }

        final SliceEntry[] expectedEntries = new SliceEntry[expected.length];
        for (int i = 0; i < expected.length; ++i) {
            final Matcher m = sliceEntryPattern.matcher(expected[i]);
            Assert.assertTrue("Error in expected output", m.matches());
            expectedEntries[i] = new SliceEntry(m.group(1), m.group(2), m.group(3));
        }

        final Diff differ = new Diff(expectedEntries, gotEntries);
        final change diff = differ.diff_2(false);
        if (diff == null)
            return;

        final StringWriter output = new StringWriter();
        output.append("Slice differs from expected slice:").append(System.getProperty("line.separator"));

        if (expectedEntries.length != gotEntries.length) {
            output.append("Expected " + expectedEntries.length + " entries, got " + gotEntries.length + "." +
                    System.getProperty("line.separator"));
        }

        final DiffPrint.Base diffPrinter = new DiffPrint.Base(expectedEntries, gotEntries) {
            @Override
            protected void print_hunk(final change hunk) {
                /* Determine range of line numbers involved in each file. */
                analyze_hunk(hunk);
                if (this.deletes == 0 && this.inserts == 0)
                    return;

                /* Print the lines that were expected but did not occur. */
                if (this.deletes != 0)
                    for (int i = this.first0; i <= this.last0; i++) {
                        final SliceEntry exp = (SliceEntry) this.file0[i];
                        print_1_line("- ", exp);
                    }

                /* Print the lines that the second file has. */
                if (this.inserts != 0)
                    for (int i = this.first1; i <= this.last1; i++) {
                        final SliceEntry exp = (SliceEntry) this.file1[i];
                        print_1_line("+ ", exp);
                    }
            }
        };
        diffPrinter.setOutput(output);
        diffPrinter.print_script(diff);

        Assert.fail(output.toString());
    }
}
