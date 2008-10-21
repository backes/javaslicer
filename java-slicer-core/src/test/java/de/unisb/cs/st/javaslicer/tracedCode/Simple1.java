package de.unisb.cs.st.javaslicer.tracedCode;

public class Simple1 {

    @SuppressWarnings("unused")
    public static void main(final String[] args) {
        final int a = 1;
        final int b = 2*a;
        final int c = 2*b;
        final int d = 2*c;
    }

}
