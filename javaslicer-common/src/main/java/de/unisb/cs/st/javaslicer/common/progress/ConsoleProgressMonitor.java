/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.progress
 *    Class:     ConsoleProgressMonitor
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/progress/ConsoleProgressMonitor.java
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

import java.io.PrintStream;


public class ConsoleProgressMonitor extends ProgressLineEmitter {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private final PrintStream outputStream;
    private final boolean overwriteOutput;

	private int lastLength;

    /**
     * Creates a {@link ConsoleProgressMonitor} which outputs to System.out
     * every 0.1 seconds, tries to overwrite previous output using '\r', and shows the approximated
     * remaining time.
     * Use {@link #ConsoleProgressMonitor(PrintStream, String, boolean, int, boolean, boolean)} if you need more options.
     * @see #ConsoleProgressMonitor(PrintStream, String, boolean, int, boolean, boolean)
     */
    public ConsoleProgressMonitor() {
        this(System.out);
    }

    /**
     * Creates a {@link ConsoleProgressMonitor} which outputs to the given PrintStream
     * every 0.1 seconds, tries to overwrite previous output using '\r', and shows the approximated
     * remaining time.
     * Use {@link #ConsoleProgressMonitor(PrintStream, String, boolean, int, boolean, boolean)} if you need more options.
     * @see #ConsoleProgressMonitor(PrintStream, String, boolean, int, boolean, boolean)
     */
    public ConsoleProgressMonitor(PrintStream outputStream) {
        this(outputStream, null, true, 100, true, true);
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
            int intervalMillis, boolean showApproxTimeRemaining, boolean onlyIfChanged) {
    	super(title, intervalMillis, showApproxTimeRemaining, onlyIfChanged);
        this.outputStream = outputStream;
        this.overwriteOutput = overwriteOutput;
    }

    @Override
	public void start(ProgressInformationProvider progressInfoProv) {
    	super.start(progressInfoProv);
    }

    @Override
	public void end() {
    	super.end();
    	finished();
    }

	@Override
	protected void emitProgressLine(String progressLine) {
		if (this.overwriteOutput) {
			StringBuilder sb = new StringBuilder(progressLine);
			int numWhiteSpaces = progressLine.length() - this.lastLength + 1;
			while (numWhiteSpaces-- > 0)
				sb.append(' ');
            sb.append(this.overwriteOutput ? "\r" : LINE_SEPARATOR);
            this.outputStream.print(sb.toString());
            this.outputStream.flush();
        } else {
        	this.outputStream.println(progressLine);
        	this.outputStream.flush();
        }
		this.lastLength = progressLine.length();
    }

	private void finished() {
		String s = "finished";
		if (this.overwriteOutput) {
			StringBuilder sb = new StringBuilder(s);
			int numWhiteSpaces = s.length() - this.lastLength + 1;
			while (numWhiteSpaces-- > 0)
				sb.append(' ');
			s = sb.toString();
        }

    	this.outputStream.println(s);
    	this.outputStream.flush();
	}

}
