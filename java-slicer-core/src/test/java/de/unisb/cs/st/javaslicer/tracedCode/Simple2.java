package de.unisb.cs.st.javaslicer.tracedCode;

public class Simple2 {

    @SuppressWarnings("unused")
    public static void main(final String[] args) {
        final int a = args[0].charAt(0)-'0'; // this expression must not be constant!
        final int b = 2*a;
        final int c = 2*a;
        final int d = 2*c;
        final int e = 2*b;
    }

}
