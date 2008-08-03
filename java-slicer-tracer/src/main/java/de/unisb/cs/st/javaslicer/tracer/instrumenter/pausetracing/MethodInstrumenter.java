package de.unisb.cs.st.javaslicer.tracer.instrumenter.pausetracing;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import de.unisb.cs.st.javaslicer.tracer.Tracer;

public class MethodInstrumenter extends MethodAdapter implements Opcodes {

    private final Label l0 = new Label();
    private final Label l1 = new Label();
    private final String methodName;

    public MethodInstrumenter(final MethodVisitor mv, final String methodName) {
        super(mv);
        this.methodName = methodName;
    }

    @Override
    public void visitCode() {
        super.visitCode();

        visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "pauseTracing", "()V");

        //visitTryCatchBlock(this.l0, this.l1, this.l1, null);
        visitLabel(this.l0);
    }

    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        visitLabel(this.l1);
        /*
        visitVarInsn(ASTORE, maxLocals);

        visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "unpauseTracing", "()V");

        visitVarInsn(ALOAD, maxLocals);
        visitInsn(ATHROW);
        */
        super.visitMaxs(maxStack, maxLocals+1);
    }

    @Override
    public void visitInsn(final int opcode) {
        switch (opcode) {
        case IRETURN: case LRETURN: case FRETURN: case DRETURN: case ARETURN: case RETURN:
            visitMethodInsn(INVOKESTATIC, Type.getInternalName(Tracer.class), "unpauseTracing", "()V");
            break;
        }
        super.visitInsn(opcode);
    }

}
