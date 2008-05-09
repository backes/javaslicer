package de.unisb.cs.st.javaslicer.tracer;

import java.io.PrintStream;
import java.io.Serializable;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceMethodVisitor;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.LongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.ObjectTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;

public class Tracer implements ClassFileTransformer, Serializable {

    private static Tracer instance = null;
    public static boolean debug = false;

    // this is the variable modified during runtime of the instrumented program
    public static int lastInstructionIndex = -1;

    private final List<ReadClass> readClasses = new ArrayList<ReadClass>();
    private final List<TraceSequence> traceSequences = new ArrayList<TraceSequence>();

    private Tracer() {
        // prevent instantiation
    }

    public static Tracer getInstance() {
        if (instance == null)
            instance = new Tracer();
        return instance;
    }

    public void add(final Instrumentation inst, final boolean retransformClasses)
            throws TracerException {
        if (retransformClasses && !inst.isRetransformClassesSupported())
            throw new TracerException("Your JVM does not support retransformation of classes");

        inst.addTransformer(this, true);
        if (retransformClasses) {
            Class<?>[] allLoadedClasses = inst.getAllLoadedClasses();
            int k = 0;
            for (final Class<?> class1 : allLoadedClasses)
                if (inst.isModifiableClass(class1) && !class1.isInterface())
                    allLoadedClasses[k++] = class1;
            if (k < allLoadedClasses.length)
                allLoadedClasses = Arrays.copyOf(allLoadedClasses, k);
            try {
                inst.retransformClasses(allLoadedClasses);
            } catch (final UnmodifiableClassException e) {
                throw new TracerException(e);
            }
        }
    }

    public synchronized byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain, final byte[] classfileBuffer) {

        try {
            if (Type.getObjectType(className).getClassName().startsWith(Tracer.class.getPackage().getName()))
                return null;

            // register that class for later reconstruction of the trace
            final ReadClass readClass = new ReadClass(className, classfileBuffer);
            this.readClasses.add(readClass);

            final ClassReader reader = new ClassReader(classfileBuffer);
            final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

            final ClassVisitor output = debug ? new CheckClassAdapter(writer) : writer;

            final ClassVisitor instrumenter = new ClassInstrumenter(output, this, readClass);

            reader.accept(instrumenter, 0);

            final byte[] newClassfileBuffer = writer.toByteArray();

            if (debug) {
                checkClass(newClassfileBuffer, readClass.getClassName());
            }

            return newClassfileBuffer;
        } catch (final Throwable t) {
            System.err.println("Error transforming class " + className + ":");
            t.printStackTrace(System.err);
            return null;
        }
    }

    private boolean checkClass(final byte[] newClassfileBuffer, final String classname) {
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
            try {
                a.analyze(cn.name, method);
            } catch (final AnalyzerException e) {
                System.err.println("Error in class " + classname + ":");
                e.printStackTrace(System.err);
                printMethod(a, System.err, method);
                return false;
            }
        }
        return true;
    }

    private void printMethod(final Analyzer a, final PrintStream err, final MethodNode method) {
        final Frame[] frames = a.getFrames();

        final TraceMethodVisitor mv = new TraceMethodVisitor();

        err.println(method.name + method.desc);
        for (int j = 0; j < method.instructions.size(); ++j) {
            method.instructions.get(j).accept(mv);

            final StringBuffer s = new StringBuffer();
            final Frame f = frames[j];
            if (f == null) {
                s.append('?');
            } else {
                for (int k = 0; k < f.getLocals(); ++k) {
                    s.append(getShortName(f.getLocal(k).toString()))
                            .append(' ');
                }
                s.append(" : ");
                for (int k = 0; k < f.getStackSize(); ++k) {
                    s.append(getShortName(f.getStack(k).toString()))
                            .append(' ');
                }
            }
            while (s.length() < method.maxStack + method.maxLocals + 1) {
                s.append(' ');
            }
            err.print(Integer.toString(j + 100000).substring(1));
            err.print(" " + s + " : " + mv.text.get(j));
        }
        for (int j = 0; j < method.tryCatchBlocks.size(); ++j) {
            ((TryCatchBlockNode) method.tryCatchBlocks.get(j)).accept(mv);
            err.print(" " + mv.text.get(j));
        }
        err.println();
    }

    private static String getShortName(final String name) {
        final int n = name.lastIndexOf('/');
        int k = name.length();
        if (name.charAt(k - 1) == ';') {
            k--;
        }
        return n == -1 ? name : name.substring(n + 1, k);
    }

    public synchronized IntegerTraceSequence newIntegerTraceSequence() {
        final int nextIndex = this.traceSequences.size();
        final IntegerTraceSequence seq = new IntegerTraceSequence(nextIndex);
        this.traceSequences.add(seq);
        return seq;
    }

    public synchronized LongTraceSequence newLongTraceSequence() {
        final int nextIndex = this.traceSequences.size();
        final LongTraceSequence seq = new LongTraceSequence(nextIndex);
        this.traceSequences.add(seq);
        return seq;
    }

    public TraceSequence newObjectTraceSequence() {
        return new ObjectTraceSequence(newLongTraceSequence());
    }

    public static void traceInteger(final int value, final int traceSequenceIndex) {
        final Tracer tracer = getInstance();
        assert traceSequenceIndex < tracer.readClasses.size();
        final TraceSequence seq = tracer.traceSequences.get(traceSequenceIndex);
        assert seq instanceof IntegerTraceSequence;
        ((IntegerTraceSequence)seq).trace(value);
    }

    public static void traceObject(final Object obj, final int traceSequenceIndex) {
        final Tracer tracer = getInstance();
        assert traceSequenceIndex < tracer.traceSequences.size();
        final TraceSequence seq = tracer.traceSequences.get(traceSequenceIndex);
        assert seq instanceof ObjectTraceSequence;
        ((ObjectTraceSequence)seq).trace(obj);
    }

}
