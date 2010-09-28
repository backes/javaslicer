/** License information:
 *    Component: javaslicer-jung
 *    Package:   de.unisb.cs.st.javaslicer.jung
 *    Class:     Line
 *    Filename:  javaslicer-jung/src/main/java/de/unisb/cs/st/javaslicer/jung/Line.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.jung;

import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;

public class Line {

	private final ReadMethod method;
	private final int lineNr;

	public Line(ReadMethod method, int lineNr) {
		this.method = method;
		this.lineNr = lineNr;
	}

	public ReadMethod getMethod() {
		return this.method;
	}

	public int getLineNr() {
		return this.lineNr;
	}

	@Override
	public String toString() {
		return this.method + ":" + this.lineNr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.lineNr;
		result = prime * result + this.method.hashCode();
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
		Line other = (Line) obj;
		if (this.lineNr != other.lineNr)
			return false;
		if (!this.method.equals(other.method))
			return false;
		return true;
	}

}
