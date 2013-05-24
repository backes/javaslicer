/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer.instrumentation
 *    Class:     IdentifiableInstrumenter
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/instrumentation/IdentifiableInstrumenter.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.javaslicer.tracer.instrumentation;

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
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.ThreadTracer;
import de.unisb.cs.st.javaslicer.tracer.Tracer;
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

        classNode.fields.add(new FieldNode(ACC_PRIVATE | ACC_SYNTHETIC | ACC_TRANSIENT,
            ID_FIELD_NAME, "J", null, null));

        classNode.interfaces.add(Type.getInternalName(Identifiable.class));

        final MethodNode getIdMethod = new MethodNode(ACC_PUBLIC | ACC_FINAL | ACC_SYNTHETIC,
            "__tracing_get_object_id", "()J", null, null);
        // first, check if the id field is already set:
        getIdMethod.instructions.add(new VarInsnNode(ALOAD, 0));
        getIdMethod.instructions.add(new FieldInsnNode(GETFIELD, classNode.name, ID_FIELD_NAME, "J"));
        getIdMethod.instructions.add(new InsnNode(LCONST_0));
        getIdMethod.instructions.add(new InsnNode(LCMP));
        final LabelNode l0 = new LabelNode();
        getIdMethod.instructions.add(new JumpInsnNode(IFNE, l0));

        // code to be executed if it is not set:
        // synchronized, so that the id is only set once
        getIdMethod.instructions.add(new VarInsnNode(ALOAD, 0));
        getIdMethod.instructions.add(new InsnNode(MONITORENTER));
        // label for the try-catch block around the synchronized block
        final LabelNode l1 = new LabelNode(), l2 = new LabelNode(), l3 = new LabelNode();
        getIdMethod.tryCatchBlocks.add(new TryCatchBlockNode(l1, l2, l3, null));
        getIdMethod.instructions.add(l1);
        // recheck if the id is set:
        getIdMethod.instructions.add(new VarInsnNode(ALOAD, 0));
        getIdMethod.instructions.add(new FieldInsnNode(GETFIELD, classNode.name, ID_FIELD_NAME, "J"));
        getIdMethod.instructions.add(new InsnNode(LCONST_0));
        getIdMethod.instructions.add(new InsnNode(LCMP));
        getIdMethod.instructions.add(new JumpInsnNode(IFNE, l2));

        // it is still unset:
        getIdMethod.instructions.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Tracer.class),
                "getInstance", "()L"+Type.getInternalName(Tracer.class)+";"));
        getIdMethod.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Tracer.class),
                "getThreadTracer", "()L"+Type.getInternalName(ThreadTracer.class)+";"));
        getIdMethod.instructions.add(new InsnNode(DUP));
        getIdMethod.instructions.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(ThreadTracer.class),
                "pauseTracing", "()V"));
        getIdMethod.instructions.add(new VarInsnNode(ALOAD, 0));
        getIdMethod.instructions.add(new FieldInsnNode(GETSTATIC,
                Type.getInternalName(ObjectIdentifier.class), "instance",
                "L" + Type.getInternalName(ObjectIdentifier.class) + ";"));
        getIdMethod.instructions.add(new InsnNode(ACONST_NULL));
        getIdMethod.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,
                Type.getInternalName(ObjectIdentifier.class), "getNewId",
                "(Ljava/lang/Object;)J"));
        getIdMethod.instructions.add(new FieldInsnNode(PUTFIELD, classNode.name, ID_FIELD_NAME, "J"));
        getIdMethod.instructions.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(ThreadTracer.class),
                "resumeTracing", "()V"));

        getIdMethod.instructions.add(l2);
        getIdMethod.instructions.add(new VarInsnNode(ALOAD, 0));
        getIdMethod.instructions.add(new InsnNode(MONITOREXIT));
        getIdMethod.instructions.add(new JumpInsnNode(GOTO, l0));
        getIdMethod.instructions.add(l3);
        getIdMethod.instructions.add(new VarInsnNode(ALOAD, 0));
        getIdMethod.instructions.add(new InsnNode(MONITOREXIT));
        getIdMethod.instructions.add(new InsnNode(ATHROW));


        getIdMethod.instructions.add(l0);
        getIdMethod.instructions.add(new VarInsnNode(ALOAD, 0));
        getIdMethod.instructions.add(new FieldInsnNode(GETFIELD, classNode.name, ID_FIELD_NAME, "J"));
        getIdMethod.instructions.add(new InsnNode(LRETURN));

        classNode.methods.add(getIdMethod);

        // now, instrument all constructors: initialize object id at the beginning of each constructor
        /*
        for (final Object methodObj: classNode.methods) {
            final MethodNode method = (MethodNode) methodObj;
            if (method.name.equals("<init>") && (method.access & ACC_ABSTRACT) == 0) {
                final InsnList newInsns = new InsnList();
                newInsns.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Tracer.class),
                        "getInstance", "()L"+Type.getInternalName(Tracer.class)+";"));
                newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Tracer.class),
                        "getThreadTracer", "()L"+Type.getInternalName(ThreadTracer.class)+";"));
                newInsns.add(new InsnNode(DUP));
                newInsns.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(ThreadTracer.class),
                        "pauseTracing", "()V"));
                newInsns.add(new VarInsnNode(ALOAD, 0));
                newInsns.add(new FieldInsnNode(GETSTATIC,
                        Type.getInternalName(ObjectIdentifier.class), "instance",
                        "L" + Type.getInternalName(ObjectIdentifier.class) + ";"));
                // TODO this may give different ids to the same object!!
                newInsns.add(new InsnNode(ACONST_NULL));
                newInsns.add(new MethodInsnNode(INVOKEVIRTUAL,
                        Type.getInternalName(ObjectIdentifier.class), "getNewId",
                        "(Ljava/lang/Object;)J"));
                newInsns.add(new FieldInsnNode(PUTFIELD, classNode.name, ID_FIELD_NAME, "J"));
                newInsns.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(ThreadTracer.class),
                        "resumeTracing", "()V"));
                method.instructions.insert(newInsns);
            }
        }
        */
    }

}
