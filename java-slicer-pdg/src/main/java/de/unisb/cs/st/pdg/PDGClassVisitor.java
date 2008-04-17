package de.unisb.cs.st.pdg;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;


public class PDGClassVisitor implements ClassVisitor {

    private final PDGModel model;

    public PDGClassVisitor(PDGModel model) {
        this.model = model;
        // TODO Auto-generated constructor stub
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // TODO Auto-generated method stub

    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        // TODO Auto-generated method stub
        return null;
    }

    public void visitAttribute(Attribute attr) {
        // TODO Auto-generated method stub

    }

    public void visitEnd() {
        // TODO Auto-generated method stub

    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        // TODO Auto-generated method stub

    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new PDGMethodVisitor(model);
    }

    public void visitOuterClass(String owner, String name, String desc) {
        // TODO Auto-generated method stub

    }

    public void visitSource(String source, String debug) {
        // TODO Auto-generated method stub

    }



}
