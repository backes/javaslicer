/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.tracedCode
 *    Class:     Exceptions7
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/tracedCode/Exceptions7.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.tracedCode;

import java.util.ArrayList;
import java.util.List;

public class Exceptions7 {

	public static void main(final String[] args) {
		int size = 1;
		List<int[]> arrays = new ArrayList<int[]>();
		try {
			while (true) {
				arrays.add(new int[size = Math.max(Integer.MAX_VALUE, 2*size)]);
			}
		} catch (Throwable t) {
			System.out.println("Catched Exception: " + t);
			System.out.println("after creating array of size " + size);
		}
	}

}
