package de.unisb.cs.st.javaslicer.tracer.instrumentation;

import java.util.ListIterator;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.Tracer;

public class ThreadInstrumenter extends TracingClassInstrumenter {

    public ThreadInstrumenter(final ReadClass readClass, final Tracer tracer) {
        super(readClass, tracer, false);
        if (tracer.debug && readClass != null)
            System.out.println("instrumenting " + readClass.getName() + " (special)");
    }

    @Override
    protected void transformMethod(final ClassNode classNode, final MethodNode method, final ListIterator<MethodNode> methodIt) {
        if ("exit".equals(method.name) && "()V".equals(method.desc) &&
                (method.access & (ACC_NATIVE | ACC_ABSTRACT | ACC_STATIC)) == 0) {
            transformExitMethod(method);
        }
    }

    @SuppressWarnings("unchecked")
    public void transformExitMethod(final MethodNode method) {
        final ListIterator<AbstractInsnNode> insnIt = method.instructions.iterator();

        insnIt.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Thread",
                "currentThread", "()Ljava/lang/Thread;"));
        insnIt.add(new VarInsnNode(ALOAD, 0));
        final LabelNode l0 = new LabelNode();
        insnIt.add(new JumpInsnNode(IF_ACMPNE, l0));
        insnIt.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Tracer.class),
                "getInstance", "()L" + Type.getInternalName(Tracer.class) + ";"));
        insnIt.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Tracer.class),
                "threadExits", "()V"));
        insnIt.add(l0);
    }

}
