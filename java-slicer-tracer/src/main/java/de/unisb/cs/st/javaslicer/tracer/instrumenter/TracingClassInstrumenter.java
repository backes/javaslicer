package de.unisb.cs.st.javaslicer.tracer.instrumenter;

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

    public void transform(final ClassNode classNode) {
        this.readClass.setSource(classNode.sourceFile);
        for (final Object o: classNode.methods) {
            final MethodNode method = (MethodNode) o;
            transformMethod(classNode, method);
        }
        this.readClass.ready();
    }

	private void transformMethod(final ClassNode classNode, final MethodNode method) {
        final ReadMethod readMethod = new ReadMethod(this.readClass, method.access,
                method.name, method.desc, AbstractInstruction.getNextIndex());
        this.readClass.addMethod(readMethod);

        // do not modify abstract or native methods
        if ((method.access & ACC_ABSTRACT) != 0 || (method.access & ACC_NATIVE) != 0)
            return;

        new TracingMethodInstrumenter(this.tracer, readMethod).transform(method);
    }

}
