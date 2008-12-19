package de.unisb.cs.st.javaslicer;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import de.hammacher.util.Diff;
import de.hammacher.util.DiffPrint;
import de.hammacher.util.Diff.change;
import de.unisb.cs.st.javaslicer.AbstractDependenciesTest.Dependency.Type;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.dependencyAnalysis.DependencyExtractor;
import de.unisb.cs.st.javaslicer.dependencyAnalysis.DependencyVisitorAdapter;
import de.unisb.cs.st.javaslicer.dependencyAnalysis.DependencyExtractor.VisitorCapabilities;
import de.unisb.cs.st.javaslicer.traceResult.ThreadId;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.variables.StackEntry;
import de.unisb.cs.st.javaslicer.variables.Variable;


public abstract class AbstractDependenciesTest {

    public static class Dependency implements Comparable<Dependency> {
        public static enum Type { RAW, WAR }

        String from;
        String to;
        Type type;
        public Dependency(String from, String to, Type type) {
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
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Dependency other = (Dependency) obj;
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
        public int compareTo(Dependency o) {
            int cmp = this.from.compareTo(o.from);
            if (cmp == 0)
                cmp = this.to.compareTo(o.to);
            if (cmp == 0)
                cmp = this.type.compareTo(o.type);
            return cmp;
        }

    }


    public static class StringArrDepVisitor extends DependencyVisitorAdapter {

        private final Set<Dependency> dependencies;
        private final InstructionFilter instrFilter;

        public StringArrDepVisitor(InstructionFilter instrFilter, Set<Dependency> dependencies) {
            this.instrFilter = instrFilter;
            this.dependencies = dependencies;
        }

        @Override
        public void visitDataDependency(Instance from, Instance to,
                Variable var, DataDependencyType type) {
            if (var instanceof StackEntry)
                return;
            if (!this.instrFilter.filterInstance(from) || !this.instrFilter.filterInstance(to))
                return;

            String fromStr = from.getMethod().getReadClass().getSource()
                + ":" + from.getLineNumber();
            String toStr = to.getMethod().getReadClass().getSource()
                + ":" + to.getLineNumber();
            Type depType = type == DataDependencyType.READ_AFTER_WRITE ? Dependency.Type.RAW : Dependency.Type.WAR;
            this.dependencies.add(new Dependency(fromStr, toStr, depType));
        }

    }

    protected static interface InstructionFilter {

        boolean filterInstance(Instance inst);

    }

    protected void compareDependencies(Dependency[] expectedDependencies,
            String traceFilename, String threadName, InstructionFilter instrFilter) throws IOException, URISyntaxException {
        final File traceFile = new File(AbstractSlicingTest.class.getResource(traceFilename).toURI());

        TraceResult res = TraceResult.readFrom(traceFile);

        ThreadId threadId = null;
        for (final ThreadId t: res.getThreads()) {
            if (threadName.equals(t.getThreadName())) {
                    threadId = t;
                    break;
            }
        }

        assertTrue("Thread not found", threadId != null);

        Set<Dependency> dependencies = new HashSet<Dependency>();
        DependencyExtractor extr = new DependencyExtractor(res);
        extr.registerVisitor(new StringArrDepVisitor(instrFilter, dependencies), VisitorCapabilities.DATA_DEPENDENCIES_ALL);
        extr.processBackwardTrace(threadId);
        Dependency[] computetedDependencies = dependencies.toArray(new Dependency[dependencies.size()]);

        Arrays.sort(expectedDependencies);
        Arrays.sort(computetedDependencies);

        final Diff differ = new Diff(expectedDependencies, computetedDependencies);
        final change diff = differ.diff_2(false);
        if (diff == null)
            return;

        final StringWriter output = new StringWriter();
        output.append("Slice differs from expected slice:").append(System.getProperty("line.separator"));

        if (expectedDependencies.length != computetedDependencies.length) {
            output.append("Expected " + expectedDependencies.length +
                " entries, got " + computetedDependencies.length + "." +
                System.getProperty("line.separator"));
        }

        final DiffPrint.Base diffPrinter = new DiffPrint.Base(expectedDependencies, computetedDependencies) {
            @Override
            protected void print_hunk(final change hunk) {
                /* Determine range of line numbers involved in each file. */
                analyze_hunk(hunk);
                if (this.deletes == 0 && this.inserts == 0)
                    return;

                /* Print the lines that were expected but did not occur. */
                if (this.deletes != 0)
                    for (int i = this.first0; i <= this.last0; i++) {
                        final Dependency exp = (Dependency) this.file0[i];
                        print_1_line("- ", exp);
                    }

                /* Print the lines that the second file has. */
                if (this.inserts != 0)
                    for (int i = this.first1; i <= this.last1; i++) {
                        final Dependency exp = (Dependency) this.file1[i];
                        print_1_line("+ ", exp);
                    }
            }
        };
        diffPrinter.setOutput(output);
        diffPrinter.print_script(diff);

        Assert.fail(output.toString());

    }

}
