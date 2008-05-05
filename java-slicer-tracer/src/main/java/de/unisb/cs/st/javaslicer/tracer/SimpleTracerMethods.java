package de.unisb.cs.st.javaslicer.tracer;

import org.objectweb.asm.Opcodes;

public class SimpleTracerMethods implements Opcodes {

    public static void traceIntEqZero(final int value, final int traceSequenceIndex) {
        Tracer.traceBoolean(traceSequenceIndex, value == 0);
    }

    public static void traceIntNeqZero(final int value, final int traceSequenceIndex) {
        Tracer.traceBoolean(traceSequenceIndex, value != 0);
    }

    public static void traceIntLtZero(final int value, final int traceSequenceIndex) {
        Tracer.traceBoolean(traceSequenceIndex, value < 0);
    }

    public static void traceIntGeZero(final int value, final int traceSequenceIndex) {
        Tracer.traceBoolean(traceSequenceIndex, value >= 0);
    }

    public static void traceIntGtZero(final int value, final int traceSequenceIndex) {
        Tracer.traceBoolean(traceSequenceIndex, value > 0);
    }

    public static void traceIntLeZero(final int value, final int traceSequenceIndex) {
        Tracer.traceBoolean(traceSequenceIndex, value <= 0);
    }

    public static void traceObjIsNull(final Object obj, final int traceSequenceIndex) {
        Tracer.traceBoolean(traceSequenceIndex, obj == null);
    }

    public static void traceObjIsNotNull(final Object obj, final int traceSequenceIndex) {
        Tracer.traceBoolean(traceSequenceIndex, obj != null);
    }

    public static void traceIntCmpEq(final int value1, final int value2, final int traceSequenceIndex) {
        Tracer.traceBoolean(traceSequenceIndex, value2 == value1);
    }

    public static void traceIntCmpNeq(final int value1, final int value2, final int traceSequenceIndex) {
        Tracer.traceBoolean(traceSequenceIndex, value2 != value1);
    }

    public static void traceIntCmpLt(final int value1, final int value2, final int traceSequenceIndex) {
        Tracer.traceBoolean(traceSequenceIndex, value2 < value1);
    }

    public static void traceIntCmpGe(final int value1, final int value2, final int traceSequenceIndex) {
        Tracer.traceBoolean(traceSequenceIndex, value2 >= value1);
    }

    public static void traceIntCmpGt(final int value1, final int value2, final int traceSequenceIndex) {
        Tracer.traceBoolean(traceSequenceIndex, value2 > value1);
    }

    public static void traceIntCmpLe(final int value1, final int value2, final int traceSequenceIndex) {
        Tracer.traceBoolean(traceSequenceIndex, value2 <= value1);
    }

    public static void traceObjCmpEq(final Object obj1, final Object obj2, final int traceSequenceIndex) {
        Tracer.traceBoolean(traceSequenceIndex, obj2 == obj1);
    }

    public static void traceObjCmpNeq(final Object obj1, final Object obj2, final int traceSequenceIndex) {
        Tracer.traceBoolean(traceSequenceIndex, obj2 != obj1);
    }

    public static void traceClass(final Object obj, final int traceSequenceIndex) {
        Tracer.traceClass(traceSequenceIndex, obj.getClass());
    }

    public static void traceInteger(final int value, final int traceSequenceIndex) {
        Tracer.traceInteger(traceSequenceIndex, value);
    }

    public static void traceObject(final Object obj, final int traceSequenceIndex) {
        Tracer.traceObject(traceSequenceIndex, obj);
    }

}
