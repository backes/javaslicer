/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.tracedCode
 *    Class:     Method1
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/tracedCode/Method1.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.tracedCode;

public class Method1 {

    @SuppressWarnings("unused")
    public static void main(final String[] args) {
        final int a = args[0].charAt(0)-'0'; // this expression must not be constant!
        final int b = 2*a;
        final int c = getFirst(a, b);
        final int d = getSecond(a, b);
        final int e = get(a, a, b);
    }

    private static int getFirst(final int a, @SuppressWarnings("unused") final int b) {
        return a;
    }

    private static int getSecond(@SuppressWarnings("unused") final int a, final int b) {
        return b;
    }

    private static int get(final int nr, final int ... val) {
        return val[nr];
    }

}
