package de.unisb.cs.st.javaslicer.tracer;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;

public class ClassInstrumenter extends ClassAdapter implements Opcodes {

    private final Tracer tracer;
    private final ReadClass readClass;

	public ClassInstrumenter(final ClassVisitor cv, final Tracer tracer, final ReadClass readClass) {
		super(cv);
		this.tracer = tracer;
		this.readClass = readClass;
		System.out.println("ci: " + readClass.getClassName());
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
        return new MethodInstrumenter(mv, this.tracer, readMethod);
	}

	@Override
	public void visitEnd() {
	    // if this is the "java.lang.Object" class, add a new field for the object identity
	    if (Object.class.getName().equals(this.readClass.getClassName())) {
	        final FieldVisitor fv = super.visitField(ACC_PUBLIC | ACC_FINAL, "__tracer_object_id",
	                Type.INT_TYPE.getDescriptor(), null, null);
	        fv.visitEnd();
	    }

        super.visitEnd();
	    this.readClass.ready();
	}

}
