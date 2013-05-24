/** License information:
 *    Component: javaslicer-jung
 *    Package:   de.unisb.cs.st.javaslicer.jung
 *    Class:     SliceEdge
 *    Filename:  javaslicer-jung/src/main/java/de/unisb/cs/st/javaslicer/jung/SliceEdge.java
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

import de.unisb.cs.st.javaslicer.variables.Variable;


public class SliceEdge<VertexType> {

	private final VertexType start;
	private final VertexType end;
	private final Variable variable;

	public SliceEdge(VertexType start, VertexType end,
			Variable variable) {
		if (start == null || end == null)
			throw new NullPointerException("start node and end node must not be null");
		this.start = start;
		this.end = end;
		this.variable = variable;
	}

	public VertexType getStart() {
		return this.start;
	}

	public VertexType getEnd() {
		return this.end;
	}

	public Variable getVariable() {
		return this.variable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.end.hashCode();
		result = prime * result + this.start.hashCode();
		result = prime * result + (this.variable == null ? 0 : this.variable.hashCode());
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
		SliceEdge<?> other = (SliceEdge<?>) obj;
		if (!this.end.equals(other.end))
			return false;
		if (!this.start.equals(other.start))
			return false;
		if (this.variable == null) {
			if (other.variable != null)
				return false;
		} else if (!this.variable.equals(other.variable))
			return false;
		return true;
	}


}
