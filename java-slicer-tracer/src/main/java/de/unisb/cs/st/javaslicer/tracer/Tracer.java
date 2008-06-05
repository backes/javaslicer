package de.unisb.cs.st.javaslicer.tracer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.AbstractInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequenceFactory;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.uncompressed.UncompressedTraceSequenceFactory;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileWriter;
import de.unisb.cs.st.javaslicer.tracer.util.SimpleArrayList;
import de.unisb.cs.st.javaslicer.tracer.util.WeakThreadMap;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileWriter.MultiplexOutputStream;

public class Tracer implements ClassFileTransformer {

    private static final long serialVersionUID = 3853368930402145734L;

    private static Tracer instance = null;

    public static boolean debug = false;

    public static final TraceSequenceFactory seqFactory = new UncompressedTraceSequenceFactory();

    private final List<ThreadTracer> allThreadTracers = new SimpleArrayList<ThreadTracer>();
    private final WeakThreadMap<ThreadTracer> threadTracers = new WeakThreadMap<ThreadTracer>() {
        @Override
        protected void removing(ThreadTracer value) {
            try {
                value.finish();
            } catch (IOException e) {
                error(e);
            }
        }
    };

    private final List<ReadClass> readClasses = new ArrayList<ReadClass>();

    protected final List<TraceSequence.Type> traceSequenceTypes
        = new ArrayList<TraceSequence.Type>();

    private volatile boolean tracingStarted = false;

    public volatile boolean tracingReady = false;

    private final File filename;
    private final MultiplexedFileWriter file;
    private final DataOutputStream mainOutStream;

    private static final AtomicInteger errorCount = new AtomicInteger(0);
    private static String lastErrorString;


    private Tracer(final File filename) throws IOException {
        this.filename = filename;
        this.file = new MultiplexedFileWriter(filename);
        final MultiplexOutputStream stream = this.file.newOutputStream();
        if (stream.getId() != 0)
            throw new AssertionError("MultiplexedFileWriter does not initially return stream id 0");
        this.mainOutStream = new DataOutputStream(stream);
    }

    public static void error(final Exception e) {
        lastErrorString = e.toString();
        errorCount.getAndIncrement();
    }

    public static void newInstance(final File filename) throws IOException {
        if (instance != null)
            throw new IllegalStateException("Tracer instance already exists");
        instance = new Tracer(filename);
    }

    public static Tracer getInstance() {
        if (instance == null)
            throw new IllegalStateException("Tracer instance not created");
        return instance;
    }

    public void add(final Instrumentation inst, final boolean retransformClasses) throws TracerException {
        if (retransformClasses && !inst.isRetransformClassesSupported())
            throw new TracerException("Your JVM does not support retransformation of classes");

        // there are classes needed while retransforming.
        // these must be loaded a-priori, otherwise circular dependencies may occur
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

            if (debug) {
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
            }

            try {
                inst.retransformClasses(classesToTransform.toArray(new Class<?>[classesToTransform.size()]));
                if (debug)
                    System.out.println("Instrumentation ready");
            } catch (final UnmodifiableClassException e) {
                throw new TracerException(e);
            }

            if (debug) {
                // print statistics once now and once when all finished
                MethodInstrumenter.printStats(System.out);
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        MethodInstrumenter.printStats(System.out);
                    }
                });
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

            //////////////////////////////////////////////////////////////////
            // NOTE: these will be cleaned up when the system runs stable
            //////////////////////////////////////////////////////////////////

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
            final ReadClass readClass = new ReadClass(className, AbstractInstruction.getNextIndex());
            this.readClasses.add(readClass);

            final ClassReader reader = new ClassReader(classfileBuffer);
            final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

            final ClassVisitor output = debug ? new CheckClassAdapter(writer) : writer;

            final ClassVisitor instrumenter = new ClassInstrumenter(output, this, readClass);

            reader.accept(instrumenter, 0);

            readClass.setInstructionNumberEnd(AbstractInstruction.getNextIndex());

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

    public int newIntegerTraceSequence() {
        return newTraceSequence(TraceSequence.Type.INTEGER);
    }

    public int newLongTraceSequence() {
        return newTraceSequence(TraceSequence.Type.LONG);
    }

    public int newObjectTraceSequence() {
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
        final ThreadTracer newTracer = new ThreadTracer(thread,
                Tracer.this.traceSequenceTypes, this);
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
        getThreadTracer().setLastInstructionIndex(instructionIndex);
    }

    public static int getLastInstructionIndex() {
        return getThreadTracer().getLastInstructionIndex();
    }

    public void finish() throws IOException {
        if (this.tracingReady)
            return;
        this.tracingReady = true;
        this.mainOutStream.writeInt(this.readClasses.size());
        for (final ReadClass rc: this.readClasses)
            rc.writeOut(this.mainOutStream);
        this.mainOutStream.writeInt(this.allThreadTracers.size());
        for (final ThreadTracer t: this.allThreadTracers)
            t.writeOut(this.mainOutStream);
        this.mainOutStream.close();
        this.file.close();
    }

    public TraceResult getResult() {
        try {
            finish();
            return TraceResult.readFrom(this.filename);
        } catch (final IOException e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }

    public MultiplexOutputStream newOutputStream() throws IOException {
        return this.file.newOutputStream();
    }

    public static void printFinalUserInfo() {
        if (errorCount.get() == 1) {
            System.out.println("There was an error while tracing: " + lastErrorString);
        } else if (errorCount.get() > 1) {
            System.out.println("There were several errors (" + errorCount.get() + " while tracing.");
            System.out.println("Last error message: " + lastErrorString);
        } else {
            if (Tracer.debug)
                System.out.println("DEBUG: trace written successfully");
        }
    }

}
