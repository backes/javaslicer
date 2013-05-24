/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.progress
 *    Class:     ProgressLineEmitter
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/progress/ProgressLineEmitter.java
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
package de.unisb.cs.st.javaslicer.common.progress;

import java.util.Formatter;


public abstract class ProgressLineEmitter implements ProgressMonitor {

    private final class PeriodicOutput extends Thread {

        private final ProgressInformationProvider progressInfoProvider;

        public PeriodicOutput(ProgressInformationProvider progressInfoProv) {
            super("periodic progress output");
            this.progressInfoProvider = progressInfoProv;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(ProgressLineEmitter.this.intervalMillis);
                    output(this.progressInfoProvider.getPercentageDone());
                }
            } catch (InterruptedException e) {
                // ok
            }
        }
    }

    private final String title;
    protected final int intervalMillis;
    private final boolean showApproxTimeRemaining;
    private final boolean onlyIfChanged;

    private PeriodicOutput outputThread = null;
    private final RemainingTimeEstimator estimator = new RemainingTimeEstimator();

    private long numApprox;
	private double lastPercentageDone;
	private int lastApproxSecondsRemaining;

    /**
     * Creates a {@link ProgressLineEmitter} which outputs every 0.1 seconds and shows the approximated
     * remaining time. A line is only emitted if something changed.
     * Use {@link #ProgressLineEmitter(String, int, boolean, boolean)} if you need some options.
     * @see #ProgressLineEmitter(String, int, boolean, boolean)
     */
    public ProgressLineEmitter() {
        this("", 100, true, true);
    }

    /**
     * Creates a {@link ProgressLineEmitter} with full control over all options.
     * @param title a title which is printed before the output of the percentage done
     * @param intervalMillis the interval in milli seconds between output of the percentage
     *        done
     * @param showApproxTimeRemaining whether to show an approximation of the remaining time
     * @param onlyIfChanged do only emit a progress line if anything really changed
     */
    public ProgressLineEmitter(String title, int intervalMillis, boolean showApproxTimeRemaining,
    		boolean onlyIfChanged) {
        this.title = title;
        this.intervalMillis = intervalMillis;
        this.showApproxTimeRemaining = showApproxTimeRemaining;
        this.onlyIfChanged = onlyIfChanged;
    }

    @Override
	public void start(ProgressInformationProvider progressInfoProv) {
        if (this.outputThread != null)
            this.outputThread.interrupt();
        this.numApprox = 0;
        output(0);
        this.outputThread = new PeriodicOutput(progressInfoProv);
        this.outputThread.start();
    }

    @Override
	public void end() {
        assert (this.outputThread != null) : "Called end() before start()";
        this.outputThread.interrupt();
        boolean interrupted = false;
        while (true) {
	        try {
	            this.outputThread.join();
	            break;
	        } catch (InterruptedException e) {
	            interrupted = true;
	        }
        }
        if (interrupted)
        	Thread.currentThread().interrupt();
        this.outputThread = null;
        output(100);
    }

    protected void output(double percentageDone) {
    	if (this.lastPercentageDone > percentageDone)
    		percentageDone = this.lastPercentageDone;
        int deziPercentDone = (int) Math.round(10. * percentageDone);
        int approxSecondsRemaining = Integer.MAX_VALUE;
        if (this.showApproxTimeRemaining && percentageDone != 0) {
        	if (++this.numApprox < 10) {
        		if (this.numApprox == 9)
        			this.estimator.reset(percentageDone);
        	} else {
        		approxSecondsRemaining = (int) this.estimator.estimateRemainingSeconds(percentageDone);
        	}
        }

        int lastDeziPercentDone = (int) Math.round(10. * this.lastPercentageDone);
        if (!this.onlyIfChanged ||
                deziPercentDone != lastDeziPercentDone ||
                approxSecondsRemaining != this.lastApproxSecondsRemaining) {
            StringBuilder sb = new StringBuilder();
            if (this.title != null) {
                sb.append(this.title).append(": ");
            }
            if (deziPercentDone < 100)
                sb.append(' ');
            sb.append(deziPercentDone/10).append('.').append(deziPercentDone%10).append("% done");
            if (this.showApproxTimeRemaining) {
                sb.append(", time left: ");
                if (approxSecondsRemaining == Integer.MAX_VALUE) {
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
            emitProgressLine(sb.toString());
        }
        this.lastApproxSecondsRemaining = approxSecondsRemaining;
        this.lastPercentageDone = percentageDone;
    }

	protected abstract void emitProgressLine(String progressLine);

}
