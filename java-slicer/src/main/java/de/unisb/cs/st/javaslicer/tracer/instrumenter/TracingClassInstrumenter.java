package de.unisb.cs.st.javaslicer.tracer.instrumenter;

import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.AbstractInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;

public class TracingClassInstrumenter implements Opcodes {

    private final Tracer tracer;
    private final ReadClass readClass;

	public TracingClassInstrumenter(final ReadClass readClass, final Tracer tracer) {
	    this.tracer = tracer;
	    this.readClass = readClass;
        if (Tracer.debug)
            System.out.println("instrumenting " + readClass.getClassName());
    }

    @SuppressWarnings("unchecked")
    public void transform(final ClassNode classNode) {
        this.readClass.setSource(classNode.sourceFile);
        final ListIterator<MethodNode> methodIt = classNode.methods.listIterator();
        while (methodIt.hasNext()) {
            transformMethod(classNode, methodIt);
        }
        this.readClass.ready();
    }

	private void transformMethod(final ClassNode classNode, final ListIterator<MethodNode> methodIt) {
	    final MethodNode method = methodIt.next();
        final ReadMethod readMethod = new ReadMethod(this.readClass, method.access,
                method.name, method.desc, AbstractInstruction.getNextIndex());
        this.readClass.addMethod(readMethod);

        // do not instrument <clinit> methods (break (linear) control flow)
        // because these methods may call other methods, we have to pause tracing when they are entered
        if ("<clinit>".equals(method.name)) {
            new PauseTracingInstrumenter(null, this.tracer).transformMethod(method, methodIt, this.readClass.getClassName());
            return;
        }

        new TracingMethodInstrumenter(this.tracer, readMethod, classNode).transform(method, methodIt);
    }

}
