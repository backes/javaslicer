package de.unisb.cs.st.javaslicer.tracer;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
		    System.out.println("instrumenting: " + readClass.getClassName());
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
		final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

		if (mv == null)
		    return null;

		// do not modify abstract or native methods
		if ((access & ACC_ABSTRACT) != 0 || (access & ACC_NATIVE) != 0)
		    return mv;

		final ReadMethod readMethod = new ReadMethod(this.readClass, name, desc);
        final MethodInstrumenter myInstrumenter = new MethodInstrumenter(mv, this.tracer, readMethod);
        //return new JSRInlinerAdapter(myInstrumenter, access, name, desc, signature, exceptions);
        return myInstrumenter;
	}

	@Override
	public void visitEnd() {
        super.visitEnd();
	    this.readClass.ready();
	}

}
