/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer
 *    Class:     AbstractDependencesTest
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/AbstractDependencesTest.java
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
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import de.hammacher.util.Diff;
import de.hammacher.util.Diff.change;
import de.hammacher.util.DiffPrint;
import de.unisb.cs.st.javaslicer.AbstractDependencesTest.Dependence.Type;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.DataDependenceType;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.DependencesExtractor;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.DependencesVisitorAdapter;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.VisitorCapability;
import de.unisb.cs.st.javaslicer.traceResult.ThreadId;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.variables.StackEntry;
import de.unisb.cs.st.javaslicer.variables.Variable;


public abstract class AbstractDependencesTest {

    public static class Dependence implements Comparable<Dependence> {
        public static enum Type { RAW, WAR }

        String from;
        String to;
        Type type;
        public Dependence(final String from, final String to, final Type type) {
            super();
            this.from = from;
            this.to = to;
            this.type = type;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.from == null) ? 0 : this.from.hashCode());
            result = prime * result + ((this.to == null) ? 0 : this.to.hashCode());
            result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
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
            final Dependence other = (Dependence) obj;
            if (!this.from.equals(other.from))
                return false;
            if (!this.to.equals(other.to))
                return false;
            if (!this.type.equals(other.type))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return this.type + " from " + this.from + " to " + this.to;
        }
        @Override
        public int compareTo(final Dependence o) {
            int cmp = this.from.compareTo(o.from);
            if (cmp == 0)
                cmp = this.to.compareTo(o.to);
            if (cmp == 0)
                cmp = this.type.compareTo(o.type);
            return cmp;
        }

    }


    public static class StringArrDepVisitor extends DependencesVisitorAdapter<InstructionInstance> {

        private final Set<Dependence> dependences;
        private final InstructionFilter instrFilter;

        public StringArrDepVisitor(final InstructionFilter instrFilter, final Set<Dependence> dependences) {
            this.instrFilter = instrFilter;
            this.dependences = dependences;
        }

        @Override
        public void visitDataDependence(InstructionInstance from,
                InstructionInstance to, Collection<? extends Variable> fromVars,
                Variable toVar, DataDependenceType type)
                throws InterruptedException {
            if (toVar instanceof StackEntry)
                return;
            if (!this.instrFilter.filterInstance(from) || !this.instrFilter.filterInstance(to))
                return;

            final String fromStr = from.getInstruction().getMethod().getReadClass().getSource()
                + ":" + from.getInstruction().getLineNumber();
            final String toStr = to.getInstruction().getMethod().getReadClass().getSource()
                + ":" + to.getInstruction().getLineNumber();
            final Type depType = type == DataDependenceType.READ_AFTER_WRITE ? Dependence.Type.RAW : Dependence.Type.WAR;
            this.dependences.add(new Dependence(fromStr, toStr, depType));
        }

    }

    protected static interface InstructionFilter {

        boolean filterInstance(InstructionInstance inst);

    }

    protected void compareDependences(final Dependence[] expectedDependences,
            final String traceFilename, final String threadName, final InstructionFilter instrFilter) throws IOException {
        File traceFile;
        try {
            traceFile = new File(AbstractSlicingTest.class.getResource(traceFilename).toURI());
        } catch (URISyntaxException e1) {
            throw new IOException(e1);
        }

        final TraceResult res = TraceResult.readFrom(traceFile);

        ThreadId threadId = null;
        for (final ThreadId t: res.getThreads()) {
            if (threadName.equals(t.getThreadName())) {
                    threadId = t;
                    break;
            }
        }

        assertTrue("Thread not found", threadId != null);

        final Set<Dependence> dependences = new HashSet<Dependence>();
        final DependencesExtractor<InstructionInstance> extr = DependencesExtractor.forTrace(res);
        extr.registerVisitor(new StringArrDepVisitor(instrFilter, dependences), VisitorCapability.DATA_DEPENDENCES_ALL);
        try {
            extr.processBackwardTrace(threadId);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("interrupted");
        }
        final Dependence[] computetedDependences = dependences.toArray(new Dependence[dependences.size()]);

        Arrays.sort(expectedDependences);
        Arrays.sort(computetedDependences);

        final Diff differ = new Diff(expectedDependences, computetedDependences);
        final change diff = differ.diff_2(false);
        if (diff == null)
            return;

        final StringWriter output = new StringWriter();
        output.append("Slice differs from expected slice:").append(System.getProperty("line.separator"));

        if (expectedDependences.length != computetedDependences.length) {
            output.append("Expected " + expectedDependences.length +
                " entries, got " + computetedDependences.length + "." +
                System.getProperty("line.separator"));
        }

        final DiffPrint.Base diffPrinter = new DiffPrint.Base(expectedDependences, computetedDependences) {
            @Override
            protected void print_hunk(final change hunk) {
                /* Determine range of line numbers involved in each file. */
                analyze_hunk(hunk);
                if (this.deletes == 0 && this.inserts == 0)
                    return;

                /* Print the lines that were expected but did not occur. */
                if (this.deletes != 0)
                    for (int i = this.first0; i <= this.last0; i++) {
                        final Dependence exp = (Dependence) this.file0[i];
                        print_1_line("- ", exp);
                    }

                /* Print the lines that the second file has. */
                if (this.inserts != 0)
                    for (int i = this.first1; i <= this.last1; i++) {
                        final Dependence exp = (Dependence) this.file1[i];
                        print_1_line("+ ", exp);
                    }
            }
        };
        diffPrinter.setOutput(output);
        diffPrinter.print_script(diff);

        Assert.fail(output.toString());

    }

}
