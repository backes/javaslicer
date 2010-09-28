/** License information:
 *    Component: 
 *    Package:   
 *    Class:     
 *    Filename:  evaluation/exception1/Exception1.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
public class Exception1 {

    public static void main(final String[] args) {
        int[] a = new int[1];
        int[] b = null;
        int c = 0;

        try {
            useArrays(a, b);
        } catch (final NullPointerException e) {
            c = 1;
        }

        b = a;
        a = null;
        try {
            useArrays(a, b);
        } catch (final NullPointerException e) {
            c = 2;
        }

        a = b;
        try {
            useArrays(a, b);
        } catch (final NullPointerException e) {
            c = 3;
        }
        ++c;
    }

    private static int useArrays(final int[] a, final int[] b) {
        return a.length + b.length;
    }

}
