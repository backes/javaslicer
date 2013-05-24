/** License information:
 *    Component: javaslicer-jung
 *    Package:   de.unisb.cs.st.javaslicer.jung
 *    Class:     Line
 *    Filename:  javaslicer-jung/src/main/java/de/unisb/cs/st/javaslicer/jung/Line.java
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
