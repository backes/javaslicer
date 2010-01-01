package de.unisb.cs.st.javaslicer.progress;

import java.io.PrintStream;

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
                    double percentageDone = this.iterator.getPercentageDone();
                    output(percentageDone);
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

    private ConsoleProgressOutput outputThread = null;

    /**
     * Creates a {@link ConsoleProgressMonitor} which outputs to System.out
     * every 0.1 seconds and tries to overwrite previous output using '\r'.
     * Use {@link #ConsoleProgressMonitor(PrintStream, String, boolean, int)} if you need more options.
     * @see #ConsoleProgressMonitor(PrintStream, String, boolean, int)
     */
    public ConsoleProgressMonitor() {
        this(System.out);
    }

    /**
     * Creates a {@link ConsoleProgressMonitor} which outputs to the given PrintStream
     * every 0.1 seconds and tries to overwrite previous output using '\r'.
     * Use {@link #ConsoleProgressMonitor(PrintStream, String, boolean, int)} if you need more options.
     * @see #ConsoleProgressMonitor(PrintStream, String, boolean, int)
     */
    public ConsoleProgressMonitor(PrintStream outputStream) {
        this(outputStream, null, true, 100);
    }

    /**
     * Creates a {@link ConsoleProgressMonitor} with full control over all options.
     * @param outputStream the output stream to which this ProgressMonitor outputs
     * @param title a title which is printed before the output of the percentage done
     * @param overwriteOutput whether or not to print a '\r' to overwrite previous output
     *        (usually only works on a console)
     * @param intervalMillis the interval in milli seconds between output of the percentage
     *        done
     */
    public ConsoleProgressMonitor(PrintStream outputStream, String title, boolean overwriteOutput,
            int intervalMillis) {
        this.outputStream = outputStream;
        this.title = title;
        this.overwriteOutput = overwriteOutput;
        this.intervalMillis = intervalMillis;
    }

    public void start(TraceIterator iterator) {
        if (this.outputThread != null)
            this.outputThread.interrupt();
        output(0);
        this.outputThread = new ConsoleProgressOutput(iterator);
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
        output(100);
    }

    protected void output(double percentageDone) {
        if (this.title != null)
            this.outputStream.format("%s: %5.1%% done...%s", this.title, percentageDone, this.overwriteOutput?"\r":"");
        else
            this.outputStream.format("%5.1%% done...%s", percentageDone, this.overwriteOutput?"\r":"");
    }

}
