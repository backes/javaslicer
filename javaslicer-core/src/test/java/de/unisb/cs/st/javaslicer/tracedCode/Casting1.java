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
