package de.unisb.cs.st.javaslicer.tracedCode;

public class Simple3 {

    @SuppressWarnings("unused")
    public static void main(final String[] args) {
    	int a, x, y, z;

        a = args[0].charAt(0)-'0'; // this expression must not be constant!
        x = 2*a;
        y = a + 2;
        a = 14;
        a = 13;
        z = a/2;
    }

}
