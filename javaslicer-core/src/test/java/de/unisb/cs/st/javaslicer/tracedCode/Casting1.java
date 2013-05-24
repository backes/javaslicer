/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.tracedCode
 *    Class:     Casting1
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/tracedCode/Casting1.java
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
