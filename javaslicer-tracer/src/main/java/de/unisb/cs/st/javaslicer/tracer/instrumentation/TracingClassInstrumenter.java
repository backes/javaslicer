package de.unisb.cs.st.javaslicer.tracer.instrumentation;

import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.AbstractInstruction;
import de.unisb.cs.st.javaslicer.tracer.Tracer;

public class TracingClassInstrumenter implements Opcodes {

    private final Tracer tracer;
    private final ReadClass readClass;

    public TracingClassInstrumenter(final ReadClass readClass, final Tracer tracer) {
        this(readClass, tracer, true);
    }

    protected TracingClassInstrumenter(final ReadClass readClass, final Tracer tracer,
            final boolean printDebug) {
        if (tracer.debug && printDebug)
            System.out.println("instrumenting " + readClass.getName());
        this.tracer = tracer;
        this.readClass = readClass;
    }

    @SuppressWarnings("unchecked")
    public void transform(final ClassNode classNode) {
        this.readClass.setSource(classNode.sourceFile);
        final ListIterator<MethodNode> methodIt = classNode.methods.listIterator();
        while (methodIt.hasNext()) {
            transformMethod(classNode, methodIt.next(), methodIt);
        }
        this.readClass.ready();
    }

    protected void transformMethod(final ClassNode classNode, final MethodNode method, final ListIterator<MethodNode> methodIt) {
        final ReadMethod readMethod = new ReadMethod(this.readClass, method.access,
                method.name, method.desc, AbstractInstruction.getNextIndex());
        this.readClass.addMethod(readMethod);

        // do not instrument <clinit> methods (break (linear) control flow)
        // because these methods may call other methods, we have to pause tracing when they are entered
        if ("<clinit>".equals(method.name)) {
            new PauseTracingInstrumenter(null, this.tracer).transformMethod(method, methodIt, this.readClass.getName());
            return;
        }

        new TracingMethodInstrumenter(this.tracer, readMethod, classNode, method).transform(methodIt);
    }

}
