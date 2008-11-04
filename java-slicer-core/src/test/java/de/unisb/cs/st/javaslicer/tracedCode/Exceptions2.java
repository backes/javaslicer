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
