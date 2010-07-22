package de.unisb.cs.st.javaslicer.common.progress;

import java.io.PrintStream;
import java.util.Formatter;


public class ConsoleProgressMonitor implements ProgressMonitor {

    private final class ConsoleProgressOutput extends Thread {

        private final ProgressInformationProvider progressInfoProvider;

        public ConsoleProgressOutput(ProgressInformationProvider progressInfoProv) {
            super("console progress output");
            this.progressInfoProvider = progressInfoProv;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(ConsoleProgressMonitor.this.intervalMillis);
                    output(this.progressInfoProvider.getPercentageDone(), true, false);
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

    private double lastPercentageDone;
    private double lastApproxPercentPerSecond;
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

    public void start(ProgressInformationProvider progressInfoProv) {
        if (this.outputThread != null)
            this.outputThread.interrupt();
        this.numApprox = 0;
        this.lastApproxPercentPerSecond = 0;
        this.lastPercentageDone = 0;
        this.lastNanos = System.nanoTime();
        output(0, false, false);
        this.outputThread = new ConsoleProgressOutput(progressInfoProv);
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
        output(100, true, true);
        if (!this.overwriteOutput)
            this.outputStream.println();
    }

    protected void output(double percentageDone, boolean onlyIfChanged, boolean finished) {
        int deziPercentDone = (int) Math.round(10. * percentageDone);
        double approxPercentPerSecond = 0;
        if (this.showApproxTimeRemaining && percentageDone != 0 && ++this.numApprox >= 10) {
            long timeNanos = System.nanoTime();
            double newPercentPerSecond = (percentageDone - this.lastPercentageDone) / (timeNanos - this.lastNanos) * 1e9;
            this.lastNanos = timeNanos;
            double influence = Math.min(1, (percentageDone - this.lastPercentageDone) * 0.1);
            approxPercentPerSecond = (1. - influence) * this.lastApproxPercentPerSecond + influence * newPercentPerSecond;
        }
        int lastDeziPercentDone = (int) Math.round(10. * this.lastPercentageDone);
        int approxSecondsRemaining = (int) ((100. - percentageDone) / approxPercentPerSecond);
        int lastApproxSecondsRemaining = (int) ((100. - this.lastPercentageDone) / this.lastApproxPercentPerSecond);
        if (!onlyIfChanged ||
                deziPercentDone != lastDeziPercentDone ||
                approxPercentPerSecond != lastApproxSecondsRemaining) {
            StringBuilder sb = new StringBuilder();
            if (this.title != null) {
                sb.append(this.title).append(": ");
            }
            if (finished) {
                int numWS = 11 /* for percentage */ + 2 /* to be safe */;
                if (this.showApproxTimeRemaining)
                    numWS += 22;
                int oldLen = sb.length();
                sb.append("finished");
                while (sb.length() - oldLen < numWS)
                    sb.append(' ');
                sb.append('\n');
            } else {
                if (deziPercentDone < 100)
                    sb.append(' ');
                sb.append(deziPercentDone/10).append('.').append(deziPercentDone%10).append("% done");
                if (this.showApproxTimeRemaining) {
                    sb.append(", time left: ");
                    if (approxPercentPerSecond == 0) {
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
        this.lastApproxPercentPerSecond = approxPercentPerSecond;
        this.lastPercentageDone = percentageDone;
    }

}
