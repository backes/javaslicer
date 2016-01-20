/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.tracedCode
 *    Class:     Exceptions7
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/tracedCode/Exceptions7.java
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
package de.unisb.cs.st.javaslicer.tracedCode;

import java.util.ArrayList;
import java.util.List;

public class Exceptions7 {

	public static void main(final String[] args) {
		long size = 1;
		List<int[]> arrays = new ArrayList<int[]>();
		try {
			while (true) {
				arrays.add(new int[(int)(size = Math.min(Integer.MAX_VALUE, 2*size))]);
			}
		} catch (Throwable t) {
			arrays.clear();
			System.out.println("Catched Exception: " + t);
			System.out.println("after creating array of size " + size);
		}
	}

}
