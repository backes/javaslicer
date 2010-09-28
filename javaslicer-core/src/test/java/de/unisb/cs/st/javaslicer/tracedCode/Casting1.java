/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.tracedCode
 *    Class:     Casting1
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/tracedCode/Casting1.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.tracedCode;

public class Casting1 {

    @SuppressWarnings("unused")
    public static void main(final String[] args) {
        int i1 = args[0].charAt(0)-'0'; // this expression must not be constant!
        double d1 = i1;
        int i2 = (int) (d1 * i1);
        double d2 = i2 * d1;
        float f1 = (float) d1;
        byte b1 = (byte) d2;
        byte b2 = (byte) (b1 * f1);
        byte b3 = (byte) (b1 * (byte)i2);
        long l1 = b2 * b1;
        long l2 = (long) f1;
        long l3 = (long) (d2 + l2);
        double d3 = f1 * l1;
        float f2 = l3 * i1;
        return;
    }

}
