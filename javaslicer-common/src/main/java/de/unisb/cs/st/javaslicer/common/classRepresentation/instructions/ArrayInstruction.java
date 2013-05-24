/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation.instructions
 *    Class:     ArrayInstruction
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/instructions/ArrayInstruction.java
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
 * Class representing an array load or array store (*ALOAD / *ASTORE) instruction.
 *
 * @author Clemens Hammacher
 */
public class ArrayInstruction extends AbstractInstruction {

    public static class ArrayInstrInstanceInfo implements InstructionInstanceInfo {

        private final long arrayId;
        private final int arrayIndex;


        public ArrayInstrInstanceInfo(long arrayId, int arrayIndex) {
            super();
            this.arrayId = arrayId;
            this.arrayIndex = arrayIndex;
        }

        public long getArrayId() {
            return this.arrayId;
        }

        public int getArrayIndex() {
            return this.arrayIndex;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (this.arrayId ^ (this.arrayId >>> 32));
            result = prime * result + this.arrayIndex;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ArrayInstrInstanceInfo other = (ArrayInstrInstanceInfo) obj;
            if (this.arrayId != other.arrayId)
                return false;
            if (this.arrayIndex != other.arrayIndex)
                return false;
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(20);
            sb.append(" [").append(this.arrayId).append(", ").append(this.arrayIndex).append(']');
            return sb.toString();
        }

    }

    private final int arrayTraceSeqIndex;
    private final int indexTraceSeqIndex;

    public ArrayInstruction(ReadMethod readMethod, int opcode,
            int lineNumber,
            int arrayTraceSeqIndex, int indexTraceSeqIndex) {
        super(readMethod, opcode, lineNumber);
        this.arrayTraceSeqIndex = arrayTraceSeqIndex;
        this.indexTraceSeqIndex = indexTraceSeqIndex;
    }

    private ArrayInstruction(ReadMethod readMethod, int lineNumber, int opcode,
            int arrayTraceSeqIndex, int indexTraceSeqIndex, int index) {
        super(readMethod, opcode, lineNumber, index);
        this.arrayTraceSeqIndex = arrayTraceSeqIndex;
        this.indexTraceSeqIndex = indexTraceSeqIndex;
    }

    @Override
    public <InstanceType> InstanceType getNextInstance(
            TraceIterator infoProv, int stackDepth, long instanceNr,
            InstructionInstanceFactory<InstanceType> instanceFactory)
            throws TracerException {

        long arrayId = infoProv.getNextLong(this.arrayTraceSeqIndex);
        int arrayIndex = infoProv.getNextInteger(this.indexTraceSeqIndex);
        return instanceFactory.createInstructionInstance(this,
            infoProv.getNextInstructionOccurenceNumber(getIndex()), stackDepth, instanceNr,
            new ArrayInstrInstanceInfo(arrayId, arrayIndex));
    }

    @Override
	public InstructionType getType() {
        return InstructionType.ARRAY;
    }

    @Override
    public void writeOut(DataOutputStream out, StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        OptimizedDataOutputStream.writeInt0(this.arrayTraceSeqIndex, out);
        OptimizedDataOutputStream.writeInt0(this.indexTraceSeqIndex, out);
    }

    public static ArrayInstruction readFrom(DataInputStream in, MethodReadInformation methodInfo,
            @SuppressWarnings("unused") StringCacheInput stringCache,
            int opcode, int index, int lineNumber) throws IOException {
        int arrayTraceSeqIndex = OptimizedDataInputStream.readInt0(in);
        int indexTraceSeqIndex = OptimizedDataInputStream.readInt0(in);
        return new ArrayInstruction(methodInfo.getMethod(), lineNumber, opcode, arrayTraceSeqIndex, indexTraceSeqIndex, index);
    }

    @Override
    public String toString() {
        switch (getOpcode()) {
        case Opcodes.IALOAD:
            return "IALOAD";
        case Opcodes.LALOAD:
            return "LALOAD";
        case Opcodes.FALOAD:
            return "FALOAD";
        case Opcodes.DALOAD:
            return "DALOAD";
        case Opcodes.AALOAD:
            return "AALOAD";
        case Opcodes.BALOAD:
            return "BALOAD";
        case Opcodes.CALOAD:
            return "CALOAD";
        case Opcodes.SALOAD:
            return "SALOAD";

        // array store:
        case Opcodes.IASTORE:
            return "IASTORE";
        case Opcodes.LASTORE:
            return "LASTORE";
        case Opcodes.FASTORE:
            return "FASTORE";
        case Opcodes.DASTORE:
            return "DASTORE";
        case Opcodes.AASTORE:
            return "AASTORE";
        case Opcodes.BASTORE:
            return "BASTORE";
        case Opcodes.CASTORE:
            return "CASTORE";
        case Opcodes.SASTORE:
            return "SASTORE";

        default:
            assert false;
            return "--ERROR--";
        }
    }

}
