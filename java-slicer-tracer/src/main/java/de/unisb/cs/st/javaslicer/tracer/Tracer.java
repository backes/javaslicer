package de.unisb.cs.st.javaslicer.tracer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
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

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.LongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.ObjectTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequenceFactory;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.gzip.GZipTraceSequenceFactory;

public class Tracer implements ClassFileTransformer {

    private static final long serialVersionUID = 3853368930402145734L;

    private static Tracer instance = null;

    public static boolean debug = false;

    private static TraceSequenceFactory seqFactory = new GZipTraceSequenceFactory();

    // this is the variable modified during runtime of the instrumented program
    public static int lastInstructionIndex = -1;

    public static boolean trace = true;

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

    public void add(final Instrumentation inst, final boolean retransformClasses) throws TracerException {
        if (retransformClasses && !inst.isRetransformClassesSupported())
            throw new TracerException("Your JVM does not support retransformation of classes");

        inst.addTransformer(this, true);

        if (retransformClasses) {
            Class<?>[] allLoadedClasses = inst.getAllLoadedClasses();
            int k = 0;
            for (final Class<?> class1 : allLoadedClasses) {
                boolean modify = inst.isModifiableClass(class1) && !class1.isInterface();
                modify &= !class1.getName().startsWith("de.unisb.cs.st.javaslicer.tracer");
                if (modify)
                    allLoadedClasses[k++] = class1;
            }
            if (k < allLoadedClasses.length)
                allLoadedClasses = Arrays.copyOf(allLoadedClasses, k);

            System.out.println("all classes: ");
            System.out.println("############################################");
            System.out.println("############################################");
            System.out.println("############################################");
            for (final Class<?> c1 : allLoadedClasses) {
                System.out.println(c1);
            }
            System.out.println("############################################");
            System.out.println("############################################");
            System.out.println("############################################");

            final Class<?>[] oldAllLoaded = allLoadedClasses;
            allLoadedClasses = new Class<?>[oldAllLoaded.length + 1];
            allLoadedClasses[0] = Tracer.class;
            System.arraycopy(oldAllLoaded, 0, allLoadedClasses, 1, oldAllLoaded.length);

            try {
                //trace = false;
                inst.retransformClasses(allLoadedClasses);
                System.out.println("Instrumentation ready");
            } catch (final UnmodifiableClassException e) {
                throw new TracerException(e);
            } finally {
                //trace = true;
            }
        }
    }

    public synchronized byte[] transform(final ClassLoader loader, final String className,
            final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer) {

        final boolean oldTrace = trace;
        trace = false;
        try {
            if (Type.getObjectType(className).getClassName().startsWith(Tracer.class.getPackage().getName()))
                return null;

            // register that class for later reconstruction of the trace
            final ReadClass readClass = new ReadClass(className, classfileBuffer, Instruction.getNextIndex());
            this.readClasses.add(readClass);

            final ClassReader reader = new ClassReader(classfileBuffer);
            final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

            final ClassVisitor output = debug ? new CheckClassAdapter(writer) : writer;

            final ClassVisitor instrumenter = new ClassInstrumenter(output, this, readClass);

            reader.accept(instrumenter, 0);

            readClass.setInstructionNumberEnd(Instruction.getNextIndex());

            final byte[] newClassfileBuffer = writer.toByteArray();

            if (debug) {
                checkClass(newClassfileBuffer, readClass.getClassName());
            }

            return newClassfileBuffer;
        } catch (final Throwable t) {
            System.err.println("Error transforming class " + className + ":");
            t.printStackTrace(System.err);
            return null;
        } finally {
            trace = oldTrace;
        }
    }

    public int getNextSequenceIndex() {
        return this.traceSequences.size();
    }

    private boolean checkClass(final byte[] newClassfileBuffer, final String classname) {
        final ClassNode cn = new ClassNode();
        final ClassReader cr = new ClassReader(newClassfileBuffer);
        cr.accept(new CheckClassAdapter(cn), ClassReader.SKIP_DEBUG);

        for (final Object methodObj : cn.methods) {
            final MethodNode method = (MethodNode) methodObj;
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
                    s.append(getShortName(f.getLocal(k).toString())).append(' ');
                }
                s.append(" : ");
                for (int k = 0; k < f.getStackSize(); ++k) {
                    s.append(getShortName(f.getStack(k).toString())).append(' ');
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
        final int nextIndex = getNextSequenceIndex();
        final IntegerTraceSequence seq = seqFactory.createIntegerTraceSequence(nextIndex);
        this.traceSequences.add(seq);
        return seq;
    }

    public synchronized LongTraceSequence newLongTraceSequence() {
        final int nextIndex = getNextSequenceIndex();
        final LongTraceSequence seq = seqFactory.createLongTraceSequence(nextIndex);
        this.traceSequences.add(seq);
        return seq;
    }

    public TraceSequence newObjectTraceSequence() {
        final int nextIndex = this.traceSequences.size();
        final ObjectTraceSequence seq = new ObjectTraceSequence(seqFactory.createLongTraceSequence(nextIndex));
        this.traceSequences.add(seq);
        return seq;
    }

    public static void traceInteger(final int value, final int traceSequenceIndex) {
        if (!trace)
            return;
        trace = false;
        //System.out.println("Tracing " + traceSequenceIndex + "; " + value);
        try {
            final Tracer tracer = getInstance();
            assert traceSequenceIndex < tracer.traceSequences.size();
            final TraceSequence seq = tracer.traceSequences.get(traceSequenceIndex);
            assert seq instanceof IntegerTraceSequence;
            ((IntegerTraceSequence) seq).trace(value);
        } finally {
            trace = true;
        }
    }

    public static void traceObject(final Object obj, final int traceSequenceIndex) {
        if (!trace)
            return;
        trace = false;
        //System.out.println("Tracing " + traceSequenceIndex + "; " + obj.getClass().getName() + "@" + System.identityHashCode(obj));
        try {
            final Tracer tracer = getInstance();
            assert traceSequenceIndex < tracer.traceSequences.size();
            final TraceSequence seq = tracer.traceSequences.get(traceSequenceIndex);
            assert seq instanceof ObjectTraceSequence;
            ((ObjectTraceSequence) seq).trace(obj);
        } finally {
            trace = true;
        }
    }

    public void writeOut(final ObjectOutputStream out) throws IOException {
        out.writeInt(this.readClasses.size());
        for (final ReadClass rc: this.readClasses)
            rc.writeOut(out);
        out.writeInt(this.traceSequences.size());
        for (final TraceSequence seq: this.traceSequences) {
            seq.writeOut(out);
        }
        out.writeInt(lastInstructionIndex);
    }

    public TraceResult getResult() {
        try {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            final ObjectOutputStream out = new ObjectOutputStream(buffer);
            writeOut(out);
            out.close();
            final ByteArrayInputStream bufIn = new ByteArrayInputStream(buffer.toByteArray());
            final ObjectInputStream in = new ObjectInputStream(bufIn);
            return TraceResult.readFrom(in);
        } catch (final IOException e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }

}
