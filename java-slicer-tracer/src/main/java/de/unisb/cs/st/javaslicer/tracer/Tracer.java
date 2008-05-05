package de.unisb.cs.st.javaslicer.tracer;

import java.io.Serializable;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;

public class Tracer implements ClassFileTransformer, Serializable {

    private final List<ReadClass> readClasses = new ArrayList<ReadClass>();

    public Tracer() {
        // nothing so far
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
