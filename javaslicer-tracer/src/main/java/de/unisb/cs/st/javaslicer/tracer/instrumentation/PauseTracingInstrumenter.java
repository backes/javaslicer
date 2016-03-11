/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer.instrumentation
 *    Class:     PauseTracingInstrumenter
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/instrumentation/PauseTracingInstrumenter.java
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.ThreadTracer;
import de.unisb.cs.st.javaslicer.tracer.Tracer;

/**
 * This class instruments methods to stop tracing as soon as the method is entered
 * and continue when it is left (either by a return statement or by a thrown exception).
 *
 * @author Clemens Hammacher
 */
public class PauseTracingInstrumenter implements Opcodes {

    private final Tracer tracer;

    public PauseTracingInstrumenter(final ReadClass readClass, final Tracer tracer) {
        this.tracer = tracer;
        if (tracer.debug && readClass != null)
            System.out.println("instrumenting " + readClass.getName() + " (pause tracing)");
    }

    @SuppressWarnings("unchecked")
    public void transform(final ClassNode classNode) {
        final ListIterator<MethodNode> methodIt = classNode.methods.listIterator();
        while (methodIt.hasNext()) {
            final MethodNode method = methodIt.next();
            transformMethod(method, methodIt, Type.getObjectType(classNode.name).getClassName());
        }
    }

    @SuppressWarnings("unchecked")
    public void transformMethod(final MethodNode method, final ListIterator<MethodNode> methodIt, final String className) {
        if ((method.access & ACC_ABSTRACT) != 0 || (method.access & ACC_NATIVE) != 0)
            return;

        int tracerLocalVarIndex = (method.access & Opcodes.ACC_STATIC) == 0 ? 1 : 0;
        for (final Type t: Type.getArgumentTypes(method.desc))
            tracerLocalVarIndex += t.getSize();

        // increment number of local variables by one (for the threadtracer)
        ++method.maxLocals;

        // and increment all local variable indexes after the new one by one
        for (final Object o: method.localVariables) {
            final LocalVariableNode localVar = (LocalVariableNode) o;
            if (localVar.index >= tracerLocalVarIndex)
                ++localVar.index;
        }
        final LabelNode l0 = new LabelNode();
        final LabelNode l1 = new LabelNode();

        final ListIterator<AbstractInsnNode> insnIt = method.instructions.iterator();

        insnIt.add(new MethodInsnNode(INVOKESTATIC,
                Type.getInternalName(Tracer.class), "getInstance",
                "()L"+Type.getInternalName(Tracer.class)+";", false));
        insnIt.add(new MethodInsnNode(INVOKEVIRTUAL,
                Type.getInternalName(Tracer.class), "getThreadTracer",
                "()L"+Type.getInternalName(ThreadTracer.class)+";", false));
        insnIt.add(new InsnNode(DUP));
        insnIt.add(new VarInsnNode(ASTORE, tracerLocalVarIndex));
        insnIt.add(new MethodInsnNode(INVOKEINTERFACE,
                Type.getInternalName(ThreadTracer.class), "pauseTracing", "()V", true));
        insnIt.add(l0);

        while (insnIt.hasNext()) {
            final AbstractInsnNode insn = insnIt.next();
            switch (insn.getType()) {
            case AbstractInsnNode.INSN:
                switch (insn.getOpcode()) {
                case IRETURN: case LRETURN: case FRETURN: case DRETURN: case ARETURN: case RETURN:
                    insnIt.previous();
                    insnIt.add(new VarInsnNode(ALOAD, tracerLocalVarIndex));
                    insnIt.add(new MethodInsnNode(INVOKEINTERFACE,
                            Type.getInternalName(ThreadTracer.class), "resumeTracing", "()V", true));
                    insnIt.next();
                }
                break;
            case AbstractInsnNode.IINC_INSN:
                if (((IincInsnNode)insn).var >= tracerLocalVarIndex)
                    ++((IincInsnNode)insn).var;
                break;
            case AbstractInsnNode.VAR_INSN:
                if (((VarInsnNode)insn).var >= tracerLocalVarIndex)
                    ++((VarInsnNode)insn).var;
                break;
            default:
                break;
            }
        }

        method.instructions.add(l1);

        method.instructions.add(new VarInsnNode(ALOAD, tracerLocalVarIndex));
        method.instructions.add(new MethodInsnNode(INVOKEINTERFACE,
                Type.getInternalName(ThreadTracer.class), "resumeTracing", "()V", true));
        method.instructions.add(new InsnNode(ATHROW));

        method.tryCatchBlocks.add(new TryCatchBlockNode(l0, l1, l1, null));

        // finally: create a copy of the method that gets the ThreadTracer as argument
        if (!"<clinit>".equals(method.name) && this.tracer.wasRedefined(className)) {
            final Type[] oldMethodArguments = Type.getArgumentTypes(method.desc);
            final Type[] newMethodArguments = Arrays.copyOf(oldMethodArguments, oldMethodArguments.length+1);
            newMethodArguments[oldMethodArguments.length] = Type.getType(ThreadTracer.class);
            final String newMethodDesc = Type.getMethodDescriptor(Type.getReturnType(method.desc), newMethodArguments);
            final MethodNode newMethod = new MethodNode(method.access, method.name, newMethodDesc,
                    method.signature, (String[]) method.exceptions.toArray(new String[method.exceptions.size()]));
            methodIt.add(newMethod);

            final Map<LabelNode, LabelNode> newMethodLabels = new LazyLabelMap();

            // copy the local variables information to the new method
            for (final Object o: method.localVariables) {
                final LocalVariableNode lv = (LocalVariableNode) o;
                newMethod.localVariables.add(new LocalVariableNode(
                    lv.name, lv.desc, lv.signature, newMethodLabels.get(lv.start),
                    newMethodLabels.get(lv.end), lv.index
                ));
            }


            newMethod.maxLocals = method.maxLocals;
            newMethod.maxStack = method.maxStack;

            // copy the try-catch-blocks
            for (final Object o: method.tryCatchBlocks) {
                final TryCatchBlockNode tcb = (TryCatchBlockNode) o;
                newMethod.tryCatchBlocks.add(new TryCatchBlockNode(
                        newMethodLabels.get(tcb.start),
                        newMethodLabels.get(tcb.end),
                        newMethodLabels.get(tcb.handler),
                        tcb.type));
            }

            // skip the first 4 instructions, replace them with this:
            newMethod.instructions.add(new VarInsnNode(ALOAD, tracerLocalVarIndex));
            final Iterator<AbstractInsnNode> oldInsnIt = method.instructions.iterator(4);
            // and add all the other instructions
            while (oldInsnIt.hasNext()) {
                final AbstractInsnNode insn = oldInsnIt.next();
                newMethod.instructions.add(insn.clone(newMethodLabels));
            }
        }
    }

}
