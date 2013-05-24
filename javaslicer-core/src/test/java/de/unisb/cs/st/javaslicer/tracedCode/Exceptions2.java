/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.tracedCode
 *    Class:     Exceptions2
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/tracedCode/Exceptions2.java
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

public class Exceptions2 {

    public static void main(final String[] args) {
        int[] a = new int[1];
        int[] b = null;
        String error = null;

        try {
            useArrays(a, b);
        } catch (final NullPointerException e) {
            error = e.getMessage();
        }

        b = a;
        a = null;
        try {
            useArrays(a, b);
        } catch (final NullPointerException e) {
            error = e.getMessage();
            b = null;
        }
        if (error != null) {
            a = new int[error.length()]; // just to use local variable "error"
        }
        return;
    }

    private static int useArrays(final int[] a, final int[] b) {
        if (a == null)
            throw new NullPointerException("a is null");
        if (b == null)
            throw new NullPointerException("b is null");
        return a.length + b.length;
    }

}
