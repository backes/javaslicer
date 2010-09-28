/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.tracedCode
 *    Class:     Branches1
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/tracedCode/Branches1.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.tracedCode;

public class Branches1 {

    @SuppressWarnings("unused")
    public static void main(final String[] args) {
        final int a = args[0].charAt(0)-'0'; // this expression must not be constant!

        final int b = 2*a;
        final int c = 3*a;
        final int d = a < 5 ? b : c;

        final boolean true0 = a == 1;
        final int e = get(true0, b, c);

        final boolean false0 = a == 0;
        final int f = get(false0, b, c);
    }

    private static int get(final boolean cond, final int ifTrue, final int ifFalse) {
        if (cond)
            return ifTrue;
        return ifFalse;
    }


}
