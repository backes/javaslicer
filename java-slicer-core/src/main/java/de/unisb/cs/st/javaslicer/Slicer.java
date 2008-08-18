package de.unisb.cs.st.javaslicer;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.tracer.traceResult.TraceResult;

public class Slicer {

    private final TraceResult trace;

    public Slicer(final File traceFile) throws IOException {
        this.trace = TraceResult.readFrom(traceFile);
    }

    public static void main(final String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java [<javaoptions>] " + Slicer.class.getName()
                    + " <trace file> + <loc>[,<loc>]*:<var>[,<var>]*");
            System.exit(-1);
        }

        final File traceFile = new File(args[0]);

        final SlicingCriterion sc = readSlicingCriterion(args[1]);

        final long startTime = System.nanoTime();
        final Set<Instruction.Instance> slice = new Slicer(traceFile).getSlice(sc);
        final long endTime = System.nanoTime();

        System.out.println("The dynamic slice for " + sc + ":");
        for (final Instance insn: slice) {
            System.out.format((Locale)null, "%s.%s:%d %s (%d)%n",
                    insn.getMethod().getReadClass().getName(),
                    insn.getMethod().getName(),
                    insn.getLineNumber(),
                    insn.toString(),
                    insn.getOccurenceNumber());
        }
        System.out.format((Locale)null, "%nComputation took %.2f seconds.%n", 1e-9*(endTime-startTime));
    }

    private static SlicingCriterion readSlicingCriterion(final String string) {
        // TODO Auto-generated method stub
        return null;
    }

    private Set<Instance> getSlice(final SlicingCriterion sc) {
        // TODO Auto-generated method stub
        return null;
    }

}
