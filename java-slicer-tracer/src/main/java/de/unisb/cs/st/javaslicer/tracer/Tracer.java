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
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequenceFactory;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.gzip.GZipTraceSequenceFactory;
import de.unisb.cs.st.javaslicer.tracer.util.SimpleArrayList;
import de.unisb.cs.st.javaslicer.tracer.util.WeakThreadMap;

public class Tracer implements ClassFileTransformer {

    private static final long serialVersionUID = 3853368930402145734L;

    private static Tracer instance = null;

    public static boolean debug = false;

    public static final TraceSequenceFactory seqFactory = new GZipTraceSequenceFactory();

    private final WeakThreadMap<ThreadTracer> threadTracers = new WeakThreadMap<ThreadTracer>();
    private final List<ThreadTracer> allThreadTracers = new SimpleArrayList<ThreadTracer>();

    private final List<ReadClass> readClasses = new ArrayList<ReadClass>();

    protected final List<TraceSequence.Type> traceSequenceTypes
        = new ArrayList<TraceSequence.Type>();

    private volatile boolean tracingStarted = false;

    public volatile boolean tracingReady = false;

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

        final Class<?>[] additionalClassesToRetransform = { java.security.Policy.class };

        inst.addTransformer(this, true);

        if (retransformClasses) {
            final ArrayList<Class<?>> classesToTransform = new ArrayList<Class<?>>();
            for (final Class<?> class1 : inst.getAllLoadedClasses()) {
                boolean modify = inst.isModifiableClass(class1) && !class1.isInterface();
                modify &= !class1.getName().startsWith("de.unisb.cs.st.javaslicer.tracer");
                if (modify)
                    classesToTransform.add(class1);
            }
            for (final Class<?> class1: additionalClassesToRetransform) {
                if (!classesToTransform.contains(class1))
                    classesToTransform.add(class1);
            }

            System.out.println("classes to transform:");
            System.out.println("############################################");
            System.out.println("############################################");
            System.out.println("############################################");
            for (final Class<?> c1 : classesToTransform) {
                System.out.println(c1);
            }
            System.out.println("############################################");
            System.out.println("############################################");
            System.out.println("############################################");

            try {
                inst.retransformClasses(classesToTransform.toArray(new Class<?>[classesToTransform.size()]));
                System.out.println("Instrumentation ready");
            } catch (final UnmodifiableClassException e) {
                throw new TracerException(e);
            }
            this.tracingStarted = true;
        }
    }

    public synchronized byte[] transform(final ClassLoader loader, final String className,
            final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer) {

        if (this.tracingReady)
            return null;

        // disable tracing for the thread tracer of this thread
        final ThreadTracer tt = getThreadTracer();
        final boolean oldEnabledState = tt.setTracingEnabled(false);

        try {
            if (Type.getObjectType(className).getClassName().startsWith(Tracer.class.getPackage().getName()))
                return null;

            // Thread, ThreadLocal and ThreadLocalMap
            if (Type.getObjectType(className).getClassName().startsWith("java.lang.Thread"))
                return null;
            // because of Thread.getName()
            if (Type.getObjectType(className).getClassName().equals("java.lang.String"))
                return null;
            if (Type.getObjectType(className).getClassName().equals("java.util.Arrays"))
                return null;
            if (Type.getObjectType(className).getClassName().equals("java.lang.Math"))
                return null;

            // Object
            if (Type.getObjectType(className).getClassName().equals("java.lang.Object"))
                return null;
            if (Type.getObjectType(className).getClassName().startsWith("java.lang.ref."))
                return null;

            // register that class for later reconstruction of the trace
            final ReadClass readClass = new ReadClass(className, classfileBuffer, Instruction.getNextIndex());
            this.readClasses.add(readClass);

            final ClassReader reader = new ClassReader(classfileBuffer);
            final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

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
            tt.setTracingEnabled(oldEnabledState);
        }
    }

    public int getNextSequenceIndex() {
        return this.traceSequenceTypes.size();
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

    public synchronized int newIntegerTraceSequence() {
        return newTraceSequence(TraceSequence.Type.INTEGER);
    }

    public synchronized int newLongTraceSequence() {
        return newTraceSequence(TraceSequence.Type.LONG);
    }

    public synchronized int newObjectTraceSequence() {
        return newTraceSequence(TraceSequence.Type.OBJECT);
    }

    private synchronized int newTraceSequence(final TraceSequence.Type type) {
        final int nextIndex = getNextSequenceIndex();
        this.traceSequenceTypes.add(type);
        return nextIndex;
    }

    public static void traceInteger(final int value, final int traceSequenceIndex) {
        final Tracer tracer = getInstance();
        if (!tracer.tracingStarted || tracer.tracingReady)
            return;
        tracer.getThreadTracer(Thread.currentThread()).traceInt(value, traceSequenceIndex);
    }

    private static ThreadTracer getThreadTracer() {
        return getInstance().getThreadTracer(Thread.currentThread());
    }

    private ThreadTracer getThreadTracer(final Thread thread) {
        final ThreadTracer tracer = this.threadTracers.get(thread);
        if (tracer != null)
            return tracer;
        final ThreadTracer newTracer = new ThreadTracer(thread.getId(),
                thread.getName(),
                Tracer.this.traceSequenceTypes);
        this.threadTracers.put(thread, newTracer);
        this.allThreadTracers.add(newTracer);
        return newTracer;
    }

    public static void traceObject(final Object obj, final int traceSequenceIndex) {
        final Tracer tracer = getInstance();
        if (!tracer.tracingStarted || tracer.tracingReady)
            return;
        tracer.getThreadTracer(Thread.currentThread()).traceObject(obj, traceSequenceIndex);
    }

    public static void setLastInstructionIndex(final int instructionIndex) {
        /*
        final Tracer tracer = getInstance();
        if (!tracer.tracingStarted)
            return;
        if (tracer.tracingReady)
            return;
        tracer.tracingStarted = false;
        */
        getThreadTracer().setLastInstructionIndex(instructionIndex);
        //tracer.tracingStarted = true;
    }

    public static int getLastInstructionIndex() {
        return getThreadTracer().getLastInstructionIndex();
    }

    public void writeOut(final ObjectOutputStream out) throws IOException {
        this.tracingReady = true;
        out.writeInt(this.readClasses.size());
        for (final ReadClass rc: this.readClasses)
            rc.writeOut(out);
        out.writeInt(this.allThreadTracers.size());
        for (final ThreadTracer t: this.allThreadTracers)
            t.writeOut(out);
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
