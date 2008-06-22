package de.unisb.cs.st.javaslicer.tracer.instrumenter.pausetracing;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;

import de.unisb.cs.st.javaslicer.tracer.Tracer;

public class ClassInstrumenter extends ClassAdapter implements Opcodes {

	public ClassInstrumenter(final ClassVisitor cv, final String className) {
		super(cv);
		if (Tracer.debug)
		    System.out.println("instrumenting " + className + " (pause tracing)");
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
		final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

		if (mv == null)
		    return null;

		// do not modify abstract or native methods
		if ((access & ACC_ABSTRACT) != 0 || (access & ACC_NATIVE) != 0)
		    return mv;

        final MethodInstrumenter myInstrumenter = new MethodInstrumenter(mv, name);
        // necessary for frame computation
        return new JSRInlinerAdapter(myInstrumenter, access, name, desc, signature, exceptions);
        //return myInstrumenter;
	}

}
