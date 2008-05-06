package de.unisb.cs.st.javaslicer.tracer;

import java.io.Serializable;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;

public class Tracer implements ClassFileTransformer, Serializable {

    // this is the variable modified during runtime of the instrumented program
    public static int lastInstructionIndex = -1;

    private final List<ReadClass> readClasses = new ArrayList<ReadClass>();

    private Tracer() {
        // nothing so far
    }

    public static Tracer newTracer(final Instrumentation inst, final boolean retransformClasses)
            throws TracerException {
        if (retransformClasses && !inst.isRetransformClassesSupported())
            throw new TracerException("Your JVM does not support retransformation of classes");

        final Tracer tracer = new Tracer();
        inst.addTransformer(tracer, true);
        if (retransformClasses) {
            Class[] allLoadedClasses = inst.getAllLoadedClasses();
            int k = 0;
            for (final Class class1 : allLoadedClasses)
                if (inst.isModifiableClass(class1))
                    allLoadedClasses[k++] = class1;
            if (k < allLoadedClasses.length)
                allLoadedClasses = Arrays.copyOf(allLoadedClasses, k);
            try {
                inst.retransformClasses(allLoadedClasses);
            } catch (final UnmodifiableClassException e) {
                throw new TracerException(e);
            }
        }
        return tracer;
    }

    public synchronized byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain, final byte[] classfileBuffer) {

        // register that class for later reconstruction of the trace
        final ReadClass readClass = new ReadClass(className, classfileBuffer);
        this.readClasses.add(readClass);

        final ClassReader reader = new ClassReader(classfileBuffer);
        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        final ClassInstrumenter instrumenter = new ClassInstrumenter(writer, this, readClass);

        reader.accept(instrumenter, 0);

        return writer.toByteArray();
    }

    public TraceSequence newBooleanTraceSequence() {
        // TODO Auto-generated method stub
        return null;
    }

    public TraceSequence newClassTraceSequence() {
        // TODO Auto-generated method stub
        return null;
    }

    public TraceSequence newIntegerTraceSequence() {
        // TODO Auto-generated method stub
        return null;
    }

    public TraceSequence newObjectTraceSequence() {
        // TODO Auto-generated method stub
        return null;
    }

    public static void traceBoolean(final int traceSequenceIndex, final boolean value) {
        // TODO Auto-generated method stub

    }

    public static void traceClass(final int traceSequenceIndex, final Class<? extends Object> class1) {
        // TODO Auto-generated method stub

    }

    public static void traceInteger(final int traceSequenceIndex, final int value) {
        // TODO Auto-generated method stub

    }

    public static void traceObject(final int traceSequenceIndex, final Object obj) {
        // TODO Auto-generated method stub

    }

}
