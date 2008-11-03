package de.unisb.cs.st.javaslicer.tracedCode;

public class Exceptions1 {

    @SuppressWarnings("unused")
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
    }

    private static int useArrays(final int[] a, final int[] b) {
        return a.length + b.length;
    }

}
