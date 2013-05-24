/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.progress
 *    Class:     RemainingTimeEstimator
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/progress/RemainingTimeEstimator.java
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


public class RemainingTimeEstimator {

    private double lastPercentageDone;
    private double lastApproxPercentPerSecond;
    private double lastApproxSecondsRemaining;
    private long lastNanos;
    private long numApprox;

    public RemainingTimeEstimator() {
    	// nop
    }

    /**
     * Resets internal statistics. You *must* call this method once before using {@link #estimateRemainingSeconds(double)}.
     * @param percentageDone the current percentage done
     */
    public void reset(double percentageDone) {
    	this.lastPercentageDone = percentageDone;
    	this.lastNanos = System.nanoTime();
    	this.numApprox = 0;
    }

    /**
     * Return an estimation of the remaining seconds based on the percentage done since the last call to
     * {@link #estimateRemainingSeconds(double)} or {@link #reset(double)}.
     *
     * @param percentageDone the current percentage done
     * @return an estimation of the remaining seconds
     */
    public double estimateRemainingSeconds(double percentageDone) {
    	if (this.lastPercentageDone > percentageDone)
    		percentageDone = this.lastPercentageDone;
        double approxPercentPerSecond = 0;
        double approxSecondsRemaining = 0;
        if (percentageDone != 0) {
            long timeNanos = System.nanoTime();
            double influenceStart = 1. / ++this.numApprox;

            double newPercentPerSecond = (percentageDone - this.lastPercentageDone) / (timeNanos - this.lastNanos) * 1e9;
            double influencePercentage = Math.min(1, (percentageDone - this.lastPercentageDone) / 5); // roughly average over the last 5 percent
            double influenceTime = Math.min(1, (timeNanos - this.lastNanos) / 60e9); // roughly average over the last minute
            double influence = Math.max(influenceStart, Math.max(influencePercentage, influenceTime));
            approxPercentPerSecond = (1. - influence) * this.lastApproxPercentPerSecond + influence * newPercentPerSecond;

            double approxSecondsRemainingNow = ((100. - percentageDone) / approxPercentPerSecond);

            if (approxSecondsRemainingNow <= 0.01)
            	approxSecondsRemainingNow = 0.01;
            double timeInfluenceTime = Math.min(1, (timeNanos - this.lastNanos) / 15e9); // roughly average over the last 15 seconds
            double timeInfluenceRemaining = Math.min(1, 1. / approxSecondsRemainingNow); // more influence when we are about to finish
            double timeInfluence = Math.max(influenceStart, Math.max(timeInfluenceTime, timeInfluenceRemaining));
            approxSecondsRemaining = (1. - timeInfluence) * this.lastApproxSecondsRemaining + influence * approxSecondsRemainingNow;

            this.lastNanos = timeNanos;
        }

        this.lastApproxPercentPerSecond = approxPercentPerSecond;
        this.lastApproxSecondsRemaining = approxSecondsRemaining;
        this.lastPercentageDone = percentageDone;

        return approxSecondsRemaining;
    }

}
