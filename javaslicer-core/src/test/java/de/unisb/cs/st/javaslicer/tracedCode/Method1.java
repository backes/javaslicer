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
