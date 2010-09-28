/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation.instructions
 *    Class:     TypeInstruction
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/instructions/TypeInstruction.java
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
import de.hammacher.util.streams.OptimizedDataInputStream;
import de.hammacher.util.streams.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstanceFactory;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstanceInfo;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.TraceIterator;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod.MethodReadInformation;
import de.unisb.cs.st.javaslicer.common.exceptions.TracerException;

/**
 * Class representing an instruction that takes a type as argument (NEW, ANEWARRAY, CHECKCAST, INSTANCEOF).
 *
 * @author Clemens Hammacher
 */
public class TypeInstruction extends AbstractInstruction {

    public static class TypeInstrInstanceInfo implements InstructionInstanceInfo {

        public static final TypeInstrInstanceInfo NO_INFO = new TypeInstrInstanceInfo(-1);
        private final long newObjectIdentifier;

        public TypeInstrInstanceInfo(long newObjectIdentifier) {
            this.newObjectIdentifier = newObjectIdentifier;
        }

        public long getNewObjectIdentifier() {
            return this.newObjectIdentifier;
        }

        @Override
        public String toString() {
            if (this.newObjectIdentifier == -1)
                return "";
            return new StringBuilder(10).append('[').
                append(this.newObjectIdentifier).append(']').toString();
        }

        @Override
        public int hashCode() {
            return (int) this.newObjectIdentifier;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TypeInstrInstanceInfo other = (TypeInstrInstanceInfo) obj;
            if (this.newObjectIdentifier != other.newObjectIdentifier)
                return false;
            return true;
        }


    }

    private final String className;
    private String javaClassName = null;
    private final int newObjectIdentifierSeqIndex;

    public TypeInstruction(final ReadMethod readMethod, final int opcode, final int lineNumber,
            final String className, int newObjIdSeqIndex) {
        super(readMethod, opcode, lineNumber);
        assert opcode == Opcodes.NEW
            || opcode == Opcodes.ANEWARRAY
            || opcode == Opcodes.CHECKCAST
            || opcode == Opcodes.INSTANCEOF;
        assert ((opcode == Opcodes.CHECKCAST || opcode == Opcodes.INSTANCEOF) && newObjIdSeqIndex == -1)
            || ((opcode == Opcodes.NEW || opcode == Opcodes.ANEWARRAY) && newObjIdSeqIndex != -1);
        this.className = className;
        this.newObjectIdentifierSeqIndex = newObjIdSeqIndex;
    }

    private TypeInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode, final String typeDesc, final int index, int newObjIdSeqIndex) {
        super(readMethod, opcode, lineNumber, index);
        assert opcode == Opcodes.NEW
            || opcode == Opcodes.ANEWARRAY
            || opcode == Opcodes.CHECKCAST
            || opcode == Opcodes.INSTANCEOF;
        assert ((opcode == Opcodes.CHECKCAST || opcode == Opcodes.INSTANCEOF) && newObjIdSeqIndex == -1)
            || ((opcode == Opcodes.NEW || opcode == Opcodes.ANEWARRAY) && newObjIdSeqIndex != -1);
        this.className = typeDesc;
        this.newObjectIdentifierSeqIndex = newObjIdSeqIndex;
    }

    /**
     * Returns the argument to this TypeInstruction, as internal name as used in the bytecode
     * (i.e. &quot;java/lang/Object&quot;).
     *
     * @return the argument to this TypeInstruction
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * Returns the argument to this TypeInstruction, as java class name
     * (i.e. &quot;java.lang.Object&quot;).
     *
     * @return the argument to this TypeInstruction as java class name
     */
    public String getJavaClassName() {
        if (this.javaClassName == null)
            this.javaClassName = org.objectweb.asm.Type.getObjectType(this.className).getClassName();
        return this.javaClassName;
    }

    public InstructionType getType() {
        return InstructionType.TYPE;
    }

    @Override
    public <InstanceType> InstanceType getNextInstance(TraceIterator infoProv,
            int stackDepth, long instanceNr, InstructionInstanceFactory<InstanceType> instanceFactory)
            throws TracerException {

        TypeInstrInstanceInfo info = this.newObjectIdentifierSeqIndex == -1
            ? TypeInstrInstanceInfo.NO_INFO
            : new TypeInstrInstanceInfo(infoProv.getNextLong(this.newObjectIdentifierSeqIndex));
        return instanceFactory.createInstructionInstance(this,
            infoProv.getNextInstructionOccurenceNumber(getIndex()), stackDepth,
            instanceNr, info);
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        stringCache.writeString(this.className, out);
        if (getOpcode() == Opcodes.NEW || getOpcode() == Opcodes.ANEWARRAY)
            OptimizedDataOutputStream.writeInt0(this.newObjectIdentifierSeqIndex, out);
    }

    public static TypeInstruction readFrom(final DataInputStream in, final MethodReadInformation methodInfo, final StringCacheInput stringCache,
            final int opcode, final int index, final int lineNumber) throws IOException {
        final String type = stringCache.readString(in);
        int newObjIdSeqIndex = opcode == Opcodes.NEW || opcode == Opcodes.ANEWARRAY
            ? OptimizedDataInputStream.readInt0(in) : -1;
        return new TypeInstruction(methodInfo.getMethod(), lineNumber, opcode, type, index, newObjIdSeqIndex);
    }

    @Override
    public String toString() {
        String instruction;
        switch (getOpcode()) {
        case Opcodes.NEW:
            instruction = "NEW";
            break;
        case Opcodes.ANEWARRAY:
            instruction = "ANEWARRAY";
            break;
        case Opcodes.CHECKCAST:
            instruction = "CHECKCAST";
            break;
        case Opcodes.INSTANCEOF:
            instruction = "INSTANCEOF";
            break;
        default:
            instruction = "-ERROR-";
        }
        return new StringBuilder(instruction.length() + this.className.length() + 1).append(instruction).append(' ').append(this.className).toString();
    }

}
