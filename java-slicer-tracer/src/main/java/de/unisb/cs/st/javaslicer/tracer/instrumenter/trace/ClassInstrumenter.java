package de.unisb.cs.st.javaslicer.tracer.instrumenter.trace;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.AbstractInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;

public class ClassInstrumenter extends ClassAdapter implements Opcodes {

    private final Tracer tracer;
    private final ReadClass readClass;

	public ClassInstrumenter(final ClassVisitor cv, final Tracer tracer, final ReadClass readClass) {
		super(cv);
		this.tracer = tracer;
		this.readClass = readClass;
		if (Tracer.debug)
		    System.out.println("instrumenting " + readClass.getClassName());
	}

	@Override
	public void visitSource(final String source, final String debug) {
	    super.visitSource(source, debug);
	    this.readClass.setSource(source);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
		final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

		if (mv == null)
		    return null;

		// do not modify abstract or native methods
		if ((access & ACC_ABSTRACT) != 0 || (access & ACC_NATIVE) != 0)
		    return mv;

		final ReadMethod readMethod = new ReadMethod(this.readClass, access, name, desc, AbstractInstruction.getNextIndex());
		this.readClass.addMethod(readMethod);

        final MethodInstrumenter myInstrumenter = new MethodInstrumenter(mv, this.tracer, readMethod);
        // necessary for frame computation
        //return new JSRInlinerAdapter(myInstrumenter, access, name, desc, signature, exceptions);
        return myInstrumenter;
	}

	@Override
	public void visitEnd() {
        super.visitEnd();
	    this.readClass.ready();
	}

}
