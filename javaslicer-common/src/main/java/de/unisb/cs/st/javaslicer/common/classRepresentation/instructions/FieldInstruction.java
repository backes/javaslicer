/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation.instructions
 *    Class:     FieldInstruction
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/instructions/FieldInstruction.java
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
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod.MethodReadInformation;
import de.unisb.cs.st.javaslicer.common.classRepresentation.TraceIterator;
import de.unisb.cs.st.javaslicer.common.exceptions.TracerException;


/**
 * Class representing a field instruction (GETFIELD, PUTFIELD, GETSTATIC or PUTSTATIC).
 *
 * @author Clemens Hammacher
 */
public class FieldInstruction extends AbstractInstruction {

    public static class FieldInstrInstanceInfo implements InstructionInstanceInfo {

        private final long objectId;

        public FieldInstrInstanceInfo(long objectId) {
            this.objectId = objectId;
        }

        public long getObjectId() {
            return this.objectId;
        }

        @Override
        public String toString() {
            if (this.objectId == -1)
                return "";
            return new StringBuilder(10).append('[').
                append(this.objectId).append(']').toString();
        }

        @Override
        public int hashCode() {
            return (int) this.objectId;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FieldInstrInstanceInfo other = (FieldInstrInstanceInfo) obj;
            if (this.objectId != other.objectId)
                return false;
            return true;
        }

    }

    private final String ownerInternalClassName;
    private final String fieldName;
    private final String fieldDesc;
    private final int objectTraceSeqIndex;
    private final boolean longValue;

    public FieldInstruction(ReadMethod readMethod, int opcode,
            int lineNumber, String ownerInternalClassName,
            String fieldName, String fieldDesc,
            int objectTraceSeqIndex) {
        super(readMethod, opcode, lineNumber);
        this.ownerInternalClassName = ownerInternalClassName;
        this.fieldName = fieldName;
        this.fieldDesc = fieldDesc;
        this.objectTraceSeqIndex = objectTraceSeqIndex;
        this.longValue = org.objectweb.asm.Type.getType(fieldDesc).getSize() == 2;
    }

    private FieldInstruction(ReadMethod readMethod, int opcode, int lineNumber,
            String ownerInternalClassName, String fieldName,
            String fieldDesc, int objectTraceSeqIndex, int index) {
        super(readMethod, opcode, lineNumber, index);
        this.ownerInternalClassName = ownerInternalClassName;
        this.fieldName = fieldName;
        this.fieldDesc = fieldDesc;
        this.objectTraceSeqIndex = objectTraceSeqIndex;
        this.longValue = org.objectweb.asm.Type.getType(fieldDesc).getSize() == 2;
    }

    public String getOwnerInternalClassName() {
        return this.ownerInternalClassName;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public String getFieldDesc() {
        return this.fieldDesc;
    }

    public boolean isLongValue() {
        return this.longValue;
    }

    @Override
	public InstructionType getType() {
        return InstructionType.FIELD;
    }

    @Override
    public <InstanceType> InstanceType getNextInstance(TraceIterator infoProv,
            int stackDepth, long instanceNr, InstructionInstanceFactory<InstanceType> instanceFactory)
            throws TracerException {

        long objectId = this.objectTraceSeqIndex == -1 ? -1 :
            infoProv.getNextLong(this.objectTraceSeqIndex);
        return instanceFactory.createInstructionInstance(this,
            infoProv.getNextInstructionOccurenceNumber(getIndex()), stackDepth, instanceNr,
            new FieldInstrInstanceInfo(objectId));
    }

    @Override
    public String toString() {
        String type;
        switch (getOpcode()) {
        case Opcodes.PUTSTATIC:
            type = "PUTSTATIC";
            break;
        case Opcodes.GETSTATIC:
            type = "GETSTATIC";
            break;

        case Opcodes.GETFIELD:
            type = "GETFIELD";
            break;

        case Opcodes.PUTFIELD:
            type = "PUTFIELD";
            break;

        default:
            assert false;
            type = "--ERROR--";
            break;
        }

        StringBuilder sb = new StringBuilder(type.length() + this.ownerInternalClassName.length() + this.fieldName.length() + this.fieldDesc.length() + 3);
        sb.append(type).append(' ').append(this.ownerInternalClassName).append('.').append(this.fieldName).append(' ').append(this.fieldDesc);
        return sb.toString();
    }

    @Override
    public void writeOut(DataOutputStream out, StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        stringCache.writeString(this.fieldDesc, out);
        stringCache.writeString(this.fieldName, out);
        OptimizedDataOutputStream.writeInt0(this.objectTraceSeqIndex, out);
        stringCache.writeString(this.ownerInternalClassName, out);
    }

    public static FieldInstruction readFrom(DataInputStream in, MethodReadInformation methodInfo,
            StringCacheInput stringCache,
            int opcode, int index, int lineNumber) throws IOException {
        String fieldDesc = stringCache.readString(in);
        String fieldName = stringCache.readString(in);
        int objectTraceSeqIndex = OptimizedDataInputStream.readInt0(in);
        String ownerInternalClassName = stringCache.readString(in);
        return new FieldInstruction(methodInfo.getMethod(), opcode, lineNumber, ownerInternalClassName, fieldName, fieldDesc, objectTraceSeqIndex, index);
    }

}
