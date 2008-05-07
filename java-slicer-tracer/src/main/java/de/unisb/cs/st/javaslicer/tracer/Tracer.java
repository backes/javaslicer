package de.unisb.cs.st.javaslicer.tracer;

import java.io.Serializable;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.CheckClassAdapter;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;

public class Tracer implements ClassFileTransformer, Serializable {

    public static boolean debug = true;

    // this is the variable modified during runtime of the instrumented program
    public static int lastInstructionIndex = -1;

    private static final List<ReadClass> readClasses = new ArrayList<ReadClass>();
    private static final List<TraceSequence> traceSequences = new ArrayList<TraceSequence>();

    private Tracer() {
        // prevent instantiation
    }

    public static Tracer newTracer() {
        return new Tracer();
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

        try {
            if (Type.getObjectType(className).getClassName().startsWith(Tracer.class.getPackage().getName()))
                return null;
            // TODO check and remove
            /*
            if (Type.getInternalName(IOException.class).equals(className))
                return null;
            */

            // register that class for later reconstruction of the trace
            final ReadClass readClass = new ReadClass(className, classfileBuffer);
            this.readClasses.add(readClass);

            final ClassReader reader = new ClassReader(classfileBuffer);
            final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

            final ClassVisitor output = debug ? new CheckClassAdapter(writer) : writer;

            final ClassVisitor instrumenter = new ClassAdapter(output);
                //new ClassInstrumenter(output, this, readClass);

            reader.accept(instrumenter, 0);

            final byte[] newClassfileBuffer = writer.toByteArray();

            if (debug) {
                try {
                    checkClass(newClassfileBuffer);
                } catch (final AnalyzerException e) {
                    System.err.println("Error in class " + readClass.getClassName() + ":");
                    e.printStackTrace(System.err);
                }
            }

            return newClassfileBuffer;
        } catch (final Throwable t) {
            System.err.println("Error transforming class " + className + ":");
            t.printStackTrace(System.err);
            return null;
        }
    }

    private void checkClass(final byte[] newClassfileBuffer) throws AnalyzerException {
        final ClassNode cn = new ClassNode();
        final ClassReader cr = new ClassReader(newClassfileBuffer);
        cr.accept(new CheckClassAdapter(cn), ClassReader.SKIP_DEBUG);

        /*
        final Type syperType = cn.superName == null
                ? null
                : Type.getObjectType(cn.superName);
        */
        for (final Object methodObj: cn.methods) {
            final MethodNode method = (MethodNode) methodObj;
            /*
            final Analyzer a = new Analyzer(new SimpleVerifier(Type.getObjectType(cn.name),
                    syperType,
                    false));
            */
            final Analyzer a = new Analyzer(new BasicVerifier());
            a.analyze(cn.name, method);
        }
    }

    public synchronized TraceSequence newIntegerTraceSequence() {
        final int nextIndex = this.traceSequences.size();
        final TraceSequence seq = new IntegerTraceSequence(nextIndex);
        return seq;
    }

    public TraceSequence newObjectTraceSequence() {
        return new ObjectTraceSequence(newIntegerTraceSequence());
    }

    public static void traceInteger(final int value, final int traceSequenceIndex) {
        assert traceSequenceIndex < traceSequences.size();
        final TraceSequence seq = traceSequences.get(traceSequenceIndex);
        assert seq instanceof IntegerTraceSequence;
        ((IntegerTraceSequence)seq).trace(value);
    }

    public static void traceObject(final Object obj, final int traceSequenceIndex) {
        assert traceSequenceIndex < traceSequences.size();
        final TraceSequence seq = traceSequences.get(traceSequenceIndex);
        assert seq instanceof ObjectTraceSequence;
        ((ObjectTraceSequence)seq).trace(obj);
    }

}
