package de.unisb.cs.st.pdg;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class PDGMethodVisitor implements MethodVisitor {

    private final PDGModel model;

    public PDGMethodVisitor(PDGModel model) {
        this.model = model;
        // TODO Auto-generated constructor stub
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        // TODO Auto-generated method stub
        return null;
    }

    public AnnotationVisitor visitAnnotationDefault() {
        // TODO Auto-generated method stub
        return null;
    }

    public void visitAttribute(Attribute attr) {
        // TODO Auto-generated method stub

    }

    public void visitCode() {
        // TODO Auto-generated method stub

    }

    public void visitEnd() {
        // TODO Auto-generated method stub

    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        // TODO Auto-generated method stub

    }

    public void visitFrame(int type, int local, Object[] local2, int stack, Object[] stack2) {
        // TODO Auto-generated method stub

    }

    public void visitIincInsn(int var, int increment) {
        // TODO Auto-generated method stub

    }

    public void visitInsn(int opcode) {
        // TODO Auto-generated method stub

    }

    public void visitIntInsn(int opcode, int operand) {
        // TODO Auto-generated method stub

    }

    public void visitJumpInsn(int opcode, Label label) {
        // TODO Auto-generated method stub

    }

    public void visitLabel(Label label) {
        // TODO Auto-generated method stub

    }

    public void visitLdcInsn(Object cst) {
        // TODO Auto-generated method stub

    }

    public void visitLineNumber(int line, Label start) {
        // TODO Auto-generated method stub

    }

    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        // TODO Auto-generated method stub

    }

    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        // TODO Auto-generated method stub

    }

    public void visitMaxs(int maxStack, int maxLocals) {
        // TODO Auto-generated method stub

    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        // TODO Auto-generated method stub

    }

    public void visitMultiANewArrayInsn(String desc, int dims) {
        // TODO Auto-generated method stub

    }

    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        // TODO Auto-generated method stub
        return null;
    }

    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
        // TODO Auto-generated method stub

    }

    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        // TODO Auto-generated method stub

    }

    public void visitTypeInsn(int opcode, String type) {
        // TODO Auto-generated method stub

    }

    public void visitVarInsn(int opcode, int var) {
        // TODO Auto-generated method stub

    }

}
