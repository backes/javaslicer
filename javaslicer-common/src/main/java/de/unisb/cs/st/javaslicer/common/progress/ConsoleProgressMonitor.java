package de.unisb.cs.st.javaslicer.common.progress;

import java.io.PrintStream;
import java.util.Formatter;

import de.unisb.cs.st.javaslicer.common.classRepresentation.TraceIterator;


public class ConsoleProgressMonitor implements ProgressMonitor {

    private final class ConsoleProgressOutput extends Thread {

        private final TraceIterator iterator;

        public ConsoleProgressOutput(TraceIterator iterator) {
            super("console progress output");
            this.iterator = iterator;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(ConsoleProgressMonitor.this.intervalMillis);
                    output(this.iterator.getNumCrossedLabels(), this.iterator.getTotalNumCrossedLabels(), true, false);
                }
            } catch (InterruptedException e) {
                // ok
            }
        }
    }

    private final PrintStream outputStream;
    private final String title;
    private final boolean overwriteOutput;
    protected final int intervalMillis;
    private final boolean showApproxTimeRemaining;

    private ConsoleProgressOutput outputThread = null;

    private double lastNumCrossedLabels;
    private double lastApproxLabelsPerSecond;
    private long lastNanos;
    private long numApprox;

    /**
     * Creates a {@link ConsoleProgressMonitor} which outputs to System.out
     * every 0.1 seconds, tries to overwrite previous output using '\r', and shows the approximated
     * remaining time.
     * Use {@link #ConsoleProgressMonitor(PrintStream, String, boolean, int, boolean)} if you need more options.
     * @see #ConsoleProgressMonitor(PrintStream, String, boolean, int, boolean)
     */
    public ConsoleProgressMonitor() {
        this(System.out);
    }

    /**
     * Creates a {@link ConsoleProgressMonitor} which outputs to the given PrintStream
     * every 0.1 seconds, tries to overwrite previous output using '\r', and shows the approximated
     * remaining time.
     * Use {@link #ConsoleProgressMonitor(PrintStream, String, boolean, int, boolean)} if you need more options.
     * @see #ConsoleProgressMonitor(PrintStream, String, boolean, int, boolean)
     */
    public ConsoleProgressMonitor(PrintStream outputStream) {
        this(outputStream, null, true, 100, true);
    }

    /**
     * Creates a {@link ConsoleProgressMonitor} with full control over all options.
     * @param outputStream the output stream to which this ProgressMonitor outputs
     * @param title a title which is printed before the output of the percentage done
     * @param overwriteOutput whether or not to print a '\r' to overwrite previous output
     *        (usually only works on a console)
     * @param intervalMillis the interval in milli seconds between output of the percentage
     *        done
     * @param showApproxTimeRemaining whether to show an approximation of the remaining time
     */
    public ConsoleProgressMonitor(PrintStream outputStream, String title, boolean overwriteOutput,
            int intervalMillis, boolean showApproxTimeRemaining) {
        this.outputStream = outputStream;
        this.title = title;
        this.overwriteOutput = overwriteOutput;
        this.intervalMillis = intervalMillis;
        this.showApproxTimeRemaining = showApproxTimeRemaining;
    }

    public void start(TraceIterator iterator) {
        if (this.outputThread != null)
            this.outputThread.interrupt();
        this.numApprox = 0;
        this.lastApproxLabelsPerSecond = 0;
        this.lastNumCrossedLabels = 0;
        this.lastNanos = System.nanoTime();
        output(0, iterator.getTotalNumCrossedLabels(), false, false);
        this.outputThread = new ConsoleProgressOutput(iterator);
        this.outputThread.start();
    }

    public void end() {
        assert (this.outputThread != null);
        this.outputThread.interrupt();
        try {
            this.outputThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.outputThread = null;
        output(1, 1, true, true);
        if (!this.overwriteOutput)
            this.outputStream.println();
    }

    protected void output(long currentNumCrossedLabels, long totalNumCrossedLabels, boolean onlyIfChanged, boolean finished) {
        int deziPercentDone = (int) Math.round(1000. * currentNumCrossedLabels / totalNumCrossedLabels);
        double approxLabelsPerSecond = 0;
        if (this.showApproxTimeRemaining && currentNumCrossedLabels != 0 && ++this.numApprox >= 10) {
            long timeNanos = System.nanoTime();
            double newLabelsPerSecond = (currentNumCrossedLabels - this.lastNumCrossedLabels) / (timeNanos - this.lastNanos) * 1e9;
            this.lastNanos = timeNanos;
            double influence = Math.max(1 / (this.numApprox - 9), 0.001);
            approxLabelsPerSecond = (1. - influence) * this.lastApproxLabelsPerSecond + influence * newLabelsPerSecond;
        }
        int lastDeziPercentDone = (int) Math.round(1000. * this.lastNumCrossedLabels / totalNumCrossedLabels);
        int approxSecondsRemaining = (int) ((totalNumCrossedLabels - currentNumCrossedLabels) / approxLabelsPerSecond);
        int lastApproxSecondsRemaining = (int) ((totalNumCrossedLabels - this.lastNumCrossedLabels) / this.lastApproxLabelsPerSecond);
        if (!onlyIfChanged ||
                deziPercentDone != lastDeziPercentDone ||
                approxLabelsPerSecond != lastApproxSecondsRemaining) {
            StringBuilder sb = new StringBuilder();
            if (this.title != null) {
                sb.append(this.title).append(": ");
            }
            if (finished) {
                sb.append("finished             \n");
            } else {
                sb.append(deziPercentDone/10).append('.').append(deziPercentDone%10).append("% done");
                if (this.showApproxTimeRemaining) {
                    sb.append(", time left: ");
                    if (approxLabelsPerSecond == 0) {
                        sb.append("unknown");
                    } else {
                        int minutesRemaining = approxSecondsRemaining / 60;
                        int hoursRemaining = minutesRemaining / 60;
                        new Formatter(sb).format("%3d:%02d:%02d",
                            hoursRemaining,
                            minutesRemaining - 60*hoursRemaining,
                            approxSecondsRemaining - 60*minutesRemaining);
                    }
                }
                sb.append("  "); // for changing number of hours
                sb.append(this.overwriteOutput ? '\r' : '\n');
            }
            this.outputStream.print(sb);
            this.outputStream.flush();
        }
        this.lastApproxLabelsPerSecond = approxLabelsPerSecond;
        this.lastNumCrossedLabels = currentNumCrossedLabels;
    }

}
