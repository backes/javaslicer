package de.unisb.cs.st.javaslicer.tracer.instrumenter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.Identifiable;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.ObjectIdentifier;

public class IdentifiableInstrumenter implements Opcodes {

    private static final String ID_FIELD_NAME = "__tracing_object_id";

    private final Tracer tracer;
    private final ReadClass readClass;

    public IdentifiableInstrumenter(final ReadClass readClass, final Tracer tracer) {
        this.readClass = readClass;
        this.tracer = tracer;
    }

    @SuppressWarnings("unchecked")
    public void transform(final ClassNode classNode) {
        if (!this.tracer.wasRedefined(this.readClass.getName()))
            return;
        if ((classNode.access & ACC_INTERFACE) != 0)
            return;

        // do not modify if the parent class already implements Identifiable
        // (this is the case if the parent class was redefined)
        if (this.tracer.wasRedefined(Type.getObjectType(classNode.superName).getClassName()))
            return;

        classNode.fields.add(new FieldNode(ACC_PRIVATE, ID_FIELD_NAME, "J", null, null));

        classNode.interfaces.add(Type.getInternalName(Identifiable.class));

        final MethodNode getIdMethod = new MethodNode(ACC_PUBLIC | ACC_FINAL, "__tracing_get_object_id", "()J", null, null);
        getIdMethod.instructions.add(new VarInsnNode(ALOAD, 0));
        getIdMethod.instructions.add(new FieldInsnNode(GETFIELD, classNode.name, ID_FIELD_NAME, "J"));
        getIdMethod.instructions.add(new InsnNode(LCONST_0));
        getIdMethod.instructions.add(new InsnNode(LCMP));
        final LabelNode l = new LabelNode();
        getIdMethod.instructions.add(new JumpInsnNode(IFNE, l));
        getIdMethod.instructions.add(new VarInsnNode(ALOAD, 0));
        getIdMethod.instructions.add(new FieldInsnNode(GETSTATIC,
                Type.getInternalName(ObjectIdentifier.class), "instance",
                "L" + Type.getInternalName(ObjectIdentifier.class) + ";"));
        getIdMethod.instructions.add(new VarInsnNode(ALOAD, 0));
        getIdMethod.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,
                Type.getInternalName(ObjectIdentifier.class), "getObjectId",
                "(Ljava/lang/Object;)J"));
        getIdMethod.instructions.add(new FieldInsnNode(PUTFIELD, classNode.name, ID_FIELD_NAME, "J"));
        getIdMethod.instructions.add(l);
        getIdMethod.instructions.add(new VarInsnNode(ALOAD, 0));
        getIdMethod.instructions.add(new FieldInsnNode(GETFIELD, classNode.name, ID_FIELD_NAME, "J"));
        getIdMethod.instructions.add(new InsnNode(LRETURN));

        classNode.methods.add(getIdMethod);
    }

}
