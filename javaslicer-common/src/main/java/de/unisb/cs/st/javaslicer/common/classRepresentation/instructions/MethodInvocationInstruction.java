/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation.instructions
 *    Class:     MethodInvocationInstruction
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/instructions/MethodInvocationInstruction.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.common.classRepresentation.instructions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.StringCacheInput;
import de.hammacher.util.StringCacheOutput;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod.MethodReadInformation;

/**
 * Class representing a method invokation instruction (INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC or
 * INVOKEINTERFACE).
 *
 * @author Clemens Hammacher
 */
public class MethodInvocationInstruction extends AbstractInstruction {

    private final String internalClassName;
    private final String methodName;
    private final String methodDesc;
    private final boolean[] parameterIsLong;
    private final byte returnedSize; // 0, 1 or 2

    public MethodInvocationInstruction(final ReadMethod readMethod, final int opcode,
            final int lineNumber, final String internalClassName, final String methodName,
            final String methodDesc) {
        super(readMethod, opcode, lineNumber);
        assert opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKESPECIAL
            || opcode == Opcodes.INVOKESTATIC || opcode == Opcodes.INVOKEINTERFACE;
        this.internalClassName = internalClassName;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        final org.objectweb.asm.Type[] parameterTypes = org.objectweb.asm.Type.getArgumentTypes(methodDesc);
        this.parameterIsLong = new boolean[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; ++i) {
            this.parameterIsLong[i] = parameterTypes[i].getSize() == 2;
        }
        org.objectweb.asm.Type returnType = org.objectweb.asm.Type.getReturnType(methodDesc);
        this.returnedSize = returnType == org.objectweb.asm.Type.VOID_TYPE ? 0 : (byte) returnType.getSize();
    }

    private MethodInvocationInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode,
            final String internalClassName, final String methodName, final String methodDesc, final int index) {
        super(readMethod, opcode, lineNumber, index);
        assert opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKESPECIAL
            || opcode == Opcodes.INVOKESTATIC || opcode == Opcodes.INVOKEINTERFACE;
        this.internalClassName = internalClassName;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        final org.objectweb.asm.Type[] parameterTypes = org.objectweb.asm.Type.getArgumentTypes(methodDesc);
        this.parameterIsLong = new boolean[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; ++i) {
            this.parameterIsLong[i] = parameterTypes[i].getSize() == 2;
        }
        org.objectweb.asm.Type returnType = org.objectweb.asm.Type.getReturnType(methodDesc);
        this.returnedSize = returnType == org.objectweb.asm.Type.VOID_TYPE ? 0 : (byte) returnType.getSize();
    }

    public String getInvokedInternalClassName() {
        return this.internalClassName;
    }

    public String getInvokedMethodName() {
        return this.methodName;
    }

    public String getInvokedMethodDesc() {
        return this.methodDesc;
    }

    public int getParameterCount() {
        return this.parameterIsLong.length;
    }

    public boolean parameterIsLong(final int paramIndex) {
        return this.parameterIsLong[paramIndex];
    }

    public byte getReturnedSize() {
        return this.returnedSize;
    }

    public InstructionType getType() {
        return InstructionType.METHODINVOCATION;
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        stringCache.writeString(this.internalClassName, out);
        stringCache.writeString(this.methodName, out);
        stringCache.writeString(this.methodDesc, out);
    }

    public static MethodInvocationInstruction readFrom(final DataInputStream in, final MethodReadInformation methodInfo,
            final StringCacheInput stringCache,
            final int opcode, final int index, final int lineNumber) throws IOException {
        final String internalClassName = stringCache.readString(in);
        final String methodName = stringCache.readString(in);
        final String methodDesc = stringCache.readString(in);
        return new MethodInvocationInstruction(methodInfo.getMethod(), lineNumber, opcode, internalClassName, methodName, methodDesc, index);
    }

    @Override
    public String toString() {
        String type;
        switch (getOpcode()) {
        case Opcodes.INVOKEVIRTUAL:
            type = "INVOKEVIRTUAL";
            break;
        case Opcodes.INVOKESPECIAL:
            type = "INVOKESPECIAL";
            break;
        case Opcodes.INVOKESTATIC:
            type = "INVOKESTATIC";
            break;
        case Opcodes.INVOKEINTERFACE:
            type = "INVOKEINTERFACE";
            break;
        default:
            assert false;
            type = "--ERROR--";
        }

        final StringBuilder sb = new StringBuilder(type.length() + this.internalClassName.length() + this.methodName.length() + this.methodDesc.length() + 2);
        sb.append(type).append(' ').append(this.internalClassName).append('.').append(this.methodName).append(this.methodDesc);
        return sb.toString();
    }

}
