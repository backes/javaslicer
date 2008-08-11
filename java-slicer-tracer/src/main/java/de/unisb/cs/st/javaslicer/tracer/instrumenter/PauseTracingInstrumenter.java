package de.unisb.cs.st.javaslicer.tracer.instrumenter;

import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.unisb.cs.st.javaslicer.tracer.ThreadTracer;
import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;

// TODO
public class PauseTracingInstrumenter implements Opcodes {

    public PauseTracingInstrumenter(final ReadClass readClass) {
        if (Tracer.debug && readClass != null)
            System.out.println("instrumenting " + readClass.getClassName() + " (pause tracing)");
    }

    public void transform(final ClassNode classNode) {
        final ListIterator<?> methodIt = classNode.methods.listIterator();
        while (methodIt.hasNext()) {
            final MethodNode method = (MethodNode) methodIt.next();
            transformMethod(method);
        }
    }

    @SuppressWarnings("unchecked")
    public void transformMethod(final MethodNode method) {
        if ((method.access & ACC_ABSTRACT) != 0 || (method.access & ACC_NATIVE) != 0)
            return;

        final LabelNode l0 = new LabelNode();
        final LabelNode l1 = new LabelNode();

        final ListIterator<AbstractInsnNode> insnIt = method.instructions.iterator();

        insnIt.add(new MethodInsnNode(INVOKESTATIC,
                Type.getInternalName(Tracer.class), "getInstance",
                "()L"+Type.getInternalName(Tracer.class)+";"));
        insnIt.add(new MethodInsnNode(INVOKESTATIC,
                "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;"));
        insnIt.add(new MethodInsnNode(INVOKEVIRTUAL,
                Type.getInternalName(Tracer.class), "getThreadTracer",
                "(Ljava/lang/Thread;)L"+Type.getInternalName(ThreadTracer.class)+";"));
        insnIt.add(new InsnNode(DUP));
        insnIt.add(new VarInsnNode(ASTORE, method.maxLocals));
        insnIt.add(new MethodInsnNode(INVOKEVIRTUAL,
                Type.getInternalName(ThreadTracer.class), "pauseTracing", "()V"));
        insnIt.add(l0);

        while (insnIt.hasNext()) {
            final AbstractInsnNode insn = insnIt.next();
            switch (insn.getOpcode()) {
            case IRETURN: case LRETURN: case FRETURN: case DRETURN: case ARETURN: case RETURN:
                insnIt.previous();
                insnIt.add(new VarInsnNode(ALOAD, method.maxLocals));
                insnIt.add(new MethodInsnNode(INVOKEVIRTUAL,
                        Type.getInternalName(ThreadTracer.class), "unpauseTracing", "()V"));
                insnIt.next();
                break;
            default:
                break;
            }
        }

        method.instructions.add(l1);

        method.instructions.add(new VarInsnNode(ALOAD, method.maxLocals));
        method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,
                Type.getInternalName(ThreadTracer.class), "unpauseTracing", "()V"));
        method.instructions.add(new InsnNode(ATHROW));

        method.tryCatchBlocks.add(new TryCatchBlockNode(l0, l1, l1, null));
    }

}
