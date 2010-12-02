/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer.instrumentation
 *    Class:     Transformer
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/instrumentation/Transformer.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.tracer.instrumentation;

import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceMethodVisitor;

import de.unisb.cs.st.javaslicer.common.classRepresentation.Field;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.AbstractInstruction;
import de.unisb.cs.st.javaslicer.common.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.ThreadTracer;
import de.unisb.cs.st.javaslicer.tracer.Tracer;

public class Transformer implements ClassFileTransformer {

    /**
     * The asm {@link ClassWriter} has an "error" (maybe feature) in the
     * method {@link #getCommonSuperClass(String, String)}, because it
     * only uses the classloader of the current class, not the system
     * class loader.
     *
     * @author Clemens Hammacher
     */
    public static final class FixedClassWriter extends ClassWriter {
        protected FixedClassWriter(final int flags) {
            super(flags);
        }

        @Override
        protected String getCommonSuperClass(final String type1, final String type2)
        {
            Class<?> c, d;
            try {
                c = Class.forName(type1.replace('/', '.'));
            } catch (final ClassNotFoundException e) {
                try {
                    c = ClassLoader.getSystemClassLoader().loadClass(type1.replace('/', '.'));
                } catch (final ClassNotFoundException e1) {
                    throw new RuntimeException(e1);
                }
            }
            try {
                d = Class.forName(type2.replace('/', '.'));
            } catch (final ClassNotFoundException e) {
                try {
                    d = ClassLoader.getSystemClassLoader().loadClass(type2.replace('/', '.'));
                } catch (final ClassNotFoundException e1) {
                    throw new RuntimeException(e1);
                }
            }
            if (c.isAssignableFrom(d)) {
                return type1;
            }
            if (d.isAssignableFrom(c)) {
                return type2;
            }
            if (c.isInterface() || d.isInterface()) {
                return "java/lang/Object";
            }
            do {
                c = c.getSuperclass();
            } while (!c.isAssignableFrom(d));
            return c.getName().replace('.', '/');
        }
    }

    private final String[] pauseTracingClasses = new String[] {
            "java.lang.ClassLoader",
            "sun.instrument.InstrumentationImpl"
    };

    private final Set<String> notRedefinedClasses;

    private static final boolean COMPUTE_FRAMES = false;
    private final Object transformationLock = new Object();

    private final AtomicLong totalTransformationTime = new AtomicLong(0);
    private final AtomicInteger totalTransformedClasses = new AtomicInteger(0);

    private final Instrumentation instrumentation;
    private final Tracer tracer;
    private final ConcurrentLinkedQueue<ReadClass> readClasses;

    public Transformer(final Tracer tracer, final Instrumentation instrumentation, final ConcurrentLinkedQueue<ReadClass> readClasses, final Set<String> notRedefinedClasses) {
        this.tracer = tracer;
        this.instrumentation = instrumentation;
        this.readClasses = readClasses;
        this.notRedefinedClasses = notRedefinedClasses;
    }

    @Override
	public byte[] transform(final ClassLoader loader, final String className,
            final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain,
            final byte[] classfileBuffer) {

        final long startTime = System.nanoTime();

        ThreadTracer tt = null;
        boolean paused = false;
        try {
            if (this.tracer.tracingReady)
                return null;

            // disable tracing for the thread tracer of this thread
            tt = this.tracer.getThreadTracer();
            tt.pauseTracing();
            paused = true;

            final String javaClassName = Type.getObjectType(className).getClassName();
            if (isExcluded(javaClassName))
                return null;
            return transform0(className, javaClassName, classfileBuffer);
        } catch (TracerException e) {
            System.err.println("Error transforming class " + className + ": " + e.getMessage());
            return null;
        } catch (RuntimeException e) {
            System.err.println("Uncatched error while transforming class " + className + ":");
            e.printStackTrace(System.err);
            return null;
        } finally {
            if (this.tracer.debug) {
                // first build the string, then print it. otherwise the output may be interrupted
                // when new classes need to be loaded to format the output
                final long nanoSecs = System.nanoTime() - startTime;
                this.totalTransformationTime.addAndGet(nanoSecs);
                this.totalTransformedClasses.incrementAndGet();
                final String text = String.format((Locale)null, "Transforming %s took %.2f msec.%n",
                        className, 1e-6*nanoSecs);
                System.out.print(text);
            }
            if (paused && tt != null)
                tt.resumeTracing();
        }
   }

    private boolean isExcluded(final String javaClassName) {
        if (javaClassName.startsWith("de.unisb.cs.st.javaslicer.tracer."))
            return true;
        if (javaClassName.startsWith("de.unisb.cs.st.javaslicer.common."))
            return true;
        if (javaClassName.startsWith("de.hammacher.util."))
            return true;
        if (javaClassName.startsWith("de.unisb.cs.st.sequitur"))
            return true;

        //////////////////////////////////////////////////////////////////
        // NOTE: these will be cleaned up when the system runs stable
        //////////////////////////////////////////////////////////////////

        if (javaClassName.equals("java.lang.System"))
            return true;
        /*
        if (javaClassName.equals("java.lang.VerifyError")
                || javaClassName.equals("java.lang.ClassCircularityError")
                || javaClassName.equals("java.lang.LinkageError")
                || javaClassName.equals("java.lang.Error")
                || javaClassName.equals("java.lang.Throwable"))
            return null;
        */

        if (javaClassName.startsWith("java.util.Collections"))
            return true;

        if (javaClassName.startsWith("java.lang.Thread")
                && !"java.lang.Thread".equals(javaClassName))
            return true;
        // because of Thread.getName()
        if (javaClassName.equals("java.lang.String"))
            return true;
        if (javaClassName.equals("java.util.Arrays"))
            return true;
        if (javaClassName.equals("java.lang.Math"))
            return true;

        // Object
        if (javaClassName.equals("java.lang.Object"))
            return true;
        // references
        if (javaClassName.startsWith("java.lang.ref."))
            return true;

        return false;
    }

    private byte[] transform0(final String className, final String javaClassName, final byte[] classfileBuffer) {
        final ClassReader reader = new ClassReader(classfileBuffer);

        final ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);
        final ClassWriter writer;

        // we have to synchronize on System.out first.
        // otherwise it may lead to a deadlock if a thread calls removeStale() on ConcurrentReferenceHashMap
        // while he holds the lock for System.out, but another thread is inside the transformation step and
        // waits for the lock of System.out
        synchronized (System.out) { synchronized (this.transformationLock) {

            // register that class for later reconstruction of the trace
            List<Field> fields;
            if (classNode.fields.isEmpty())
                fields = Collections.emptyList();
            else
                fields = new ArrayList<Field>(classNode.fields.size());

            final String javaSuperName = Type.getObjectType(classNode.superName).getClassName();
            final ReadClass readClass = new ReadClass(
                className, AbstractInstruction.getNextIndex(), classNode.access,
                classNode.sourceFile, fields, javaSuperName);
            for (final Object fieldObj: classNode.fields) {
                final FieldNode f = (FieldNode) fieldObj;
                fields.add(new Field(f.name, f.desc, f.access, readClass));
            }

            writer = new FixedClassWriter(COMPUTE_FRAMES ? ClassWriter.COMPUTE_FRAMES : ClassWriter.COMPUTE_MAXS);

            final ClassVisitor output = this.tracer.check ? new CheckClassAdapter(writer) : writer;

            if (Arrays.asList(this.pauseTracingClasses).contains(javaClassName)
                    || className.startsWith("java/security/")) {
                new PauseTracingInstrumenter(readClass, this.tracer).transform(classNode);
            } else {
                if ("java/lang/Thread".equals(className))
                    new ThreadInstrumenter(readClass, this.tracer).transform(classNode);
                else
                    new TracingClassInstrumenter(readClass, this.tracer).transform(classNode);
            }

            new IdentifiableInstrumenter(readClass, this.tracer).transform(classNode);

            classNode.accept(COMPUTE_FRAMES ? new JSRInliner(output) : output);

            readClass.setInstructionNumberEnd(AbstractInstruction.getNextIndex());

            // now we can write the class out
            // NOTE: we do not write it out immediately, because this sometimes leads
            // to circular dependencies!
            //readClass.writeOut(this.readClassesOutputStream, this.readClassesStringCache);
            this.readClasses.add(readClass);

        }}

        final byte[] newClassfileBuffer = writer.toByteArray();

        if (this.tracer.check) {
            checkClass(newClassfileBuffer, className, classfileBuffer);
        }

        //printClass(newClassfileBuffer, Type.getObjectType(className).getClassName());
        /*
        if (className.endsWith("line/Main"))
            printClass(newClassfileBuffer, Type.getObjectType(className).getClassName());
        */

        return newClassfileBuffer;
    }

    private boolean checkClass(final byte[] newClassfileBuffer, final String classname, byte[] origClassfileBuffer) {
        final ClassNode cn = new ClassNode();
        final ClassReader cr = new ClassReader(newClassfileBuffer);
        //cr.accept(new CheckClassAdapter(cn), ClassReader.SKIP_DEBUG);
        cr.accept(new CheckClassAdapter(cn), 0);

        for (final Object methodObj : cn.methods) {
            final MethodNode method = (MethodNode) methodObj;
            final Analyzer a = new Analyzer(new BasicVerifier());
            // SimpleVerifier has problems with sub-classes, e.g. you cannot use PrintStream for Appendable and so on...
            //final Analyzer a = new Analyzer(new SimpleVerifier(
            //    Type.getObjectType(cn.name), Type.getObjectType(cn.superName),
            //    (cn.access & Opcodes.ACC_INTERFACE) != 0));
            try {
                a.analyze(cn.name, method);
            } catch (final AnalyzerException e) {
                System.err.println("Error in method " + classname + "." + method.name
                        + method.desc + ": " + e);
                //e.printStackTrace(System.err);
                printMethod(a, System.err, method);
                System.err.println("original bytecode:");
                ClassReader origClassReader = new ClassReader(origClassfileBuffer);
                ClassNode origClassNode = new ClassNode();
                origClassReader.accept(origClassNode, 0);
                for (Object origMethodObj : origClassNode.methods) {
                	MethodNode origMethod = (MethodNode) origMethodObj;
                	if (origMethod.name.equals(method.name) && origMethod.desc.equals(method.desc))
                		printMethod(System.err, origMethod);

                }
                return false;
            }
        }
        return true;
    }

    public static void printMethod(PrintStream out, MethodNode method) {
        final TraceMethodVisitor mv = new TraceMethodVisitor();

        out.println(method.name + method.desc);
        for (int j = 0; j < method.instructions.size(); ++j) {
            method.instructions.get(j).accept(mv);

            final StringBuffer s = new StringBuffer();
            while (s.length() < method.maxStack + method.maxLocals + 1) {
                s.append(' ');
            }
            out.print(Integer.toString(j + 100000).substring(1));
            out.print(" " + s + " : " + mv.text.get(j));
        }
        for (int j = 0; j < method.tryCatchBlocks.size(); ++j) {
            ((TryCatchBlockNode) method.tryCatchBlocks.get(j)).accept(mv);
            out.print(" " + mv.text.get(method.instructions.size()+j));
        }
        out.println(" MAXSTACK " + method.maxStack);
        out.println(" MAXLOCALS " + method.maxLocals);
        out.println();
    }

    private static void printMethod(final Analyzer a, final PrintStream out, final MethodNode method) {
        final Frame[] frames = a.getFrames();

        final TraceMethodVisitor mv = new TraceMethodVisitor();

        out.println(method.name + method.desc);
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
            out.print(Integer.toString(j + 100000).substring(1));
            out.print(" " + s + " : " + mv.text.get(j));
        }
        for (int j = 0; j < method.tryCatchBlocks.size(); ++j) {
            ((TryCatchBlockNode) method.tryCatchBlocks.get(j)).accept(mv);
            out.print(" " + mv.text.get(method.instructions.size()+j));
        }
        out.println(" MAXSTACK " + method.maxStack);
        out.println(" MAXLOCALS " + method.maxLocals);
        out.println();
    }

    @SuppressWarnings("unused")
    private static void printClass(final byte[] classfileBuffer, final String classname) {
        /*
        final TraceClassVisitor v = new TraceClassVisitor(new PrintWriter(System.out));
        new ClassReader(classfileBuffer).accept(v, ClassReader.SKIP_DEBUG);
        */
        final ClassNode cn = new ClassNode();
        final ClassReader cr = new ClassReader(classfileBuffer);
        //cr.accept(new CheckClassAdapter(cn), ClassReader.SKIP_DEBUG);
        cr.accept(new CheckClassAdapter(cn), 0);

        for (final Object methodObj : cn.methods) {
            final MethodNode method = (MethodNode) methodObj;
            final Analyzer a = new Analyzer(new BasicVerifier());
            //final Analyzer a = new Analyzer(new SimpleVerifier());
            try {
                a.analyze(cn.name, method);
            } catch (final AnalyzerException e) {
                System.err.println("// error in method " + classname + "." + method.name
                        + method.desc + ":" + e);
            }
            printMethod(a, System.err, method);
        }
    }

    private static String getShortName(final String name) {
        final int n = name.lastIndexOf('/');
        int k = name.length();
        if (name.charAt(k - 1) == ';') {
            k--;
        }
        return n == -1 ? name : name.substring(n + 1, k);
    }

    public void finish() {
        this.instrumentation.removeTransformer(this);
        if (this.tracer.debug) {
            TracingMethodInstrumenter.printStats(System.out);
            System.out.format((Locale)null, "Transforming %d classes took %.3f seconds in total.%n",
                    this.totalTransformedClasses.get(), 1e-9*this.totalTransformationTime.get());
        }
    }

    /**
     * Checks whether the class given by the fully qualified java class name has been
     * redefined by the instrumenter or not.
     * The classes that couldn't get redefined are those already loaded by the vm when
     * the agent's premain method was executed.
     *
     * @param className the fully qualified classname to check
     * @return true if the class was redefined, false if not
     */
    // hmm, redefined is the wrong word here...
    public boolean wasRedefined(final String className) {
        return !this.notRedefinedClasses.contains(className);
    }

}
