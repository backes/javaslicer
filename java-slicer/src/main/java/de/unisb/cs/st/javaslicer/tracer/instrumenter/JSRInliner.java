package de.unisb.cs.st.javaslicer.tracer.instrumenter;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.JSRInlinerAdapter;

public class JSRInliner extends ClassAdapter {

    public JSRInliner(final ClassVisitor cv) {
        super(cv);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        return visitor == null ? null : new JSRInlinerAdapter(visitor, access, name, desc, signature, exceptions);
    }

}
