/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.tracedCode
 *    Class:     Exceptions2
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/tracedCode/Exceptions2.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
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
