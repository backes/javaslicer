/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.tracedCode
 *    Class:     Exceptions9
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/tracedCode/Exceptions9.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.tracedCode;


public class Exceptions9 {

	public static void main(final String[] args) {
		int[] x = new int[2];
		int[] y = null;
		try {
			System.arraycopy(x, 0, y, 0, 2); // exception from native call
		} catch (NullPointerException e) {
			y = new int[3];
		}
		System.out.println(y.length);
	}

}
