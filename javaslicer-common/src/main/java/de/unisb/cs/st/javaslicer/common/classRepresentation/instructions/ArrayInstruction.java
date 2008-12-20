package de.unisb.cs.st.javaslicer.common.classRepresentation.instructions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.OptimizedDataInputStream;
import de.hammacher.util.OptimizedDataOutputStream;
import de.hammacher.util.StringCacheInput;
import de.hammacher.util.StringCacheOutput;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.TraceIterationInformationProvider;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod.MethodReadInformation;

/**
 * Class representing an array load or array store (*ALOAD / *ASTORE) instruction.
 *
 * @author Clemens Hammacher
 */
public class ArrayInstruction extends AbstractInstruction {

    public static class ArrayInstrInstance extends AbstractInstance {

        private final long arrayId;
        private final int arrayIndex;

        public ArrayInstrInstance(final ArrayInstruction arrayInstr, final long occurenceNumber, final int stackDepth,
                final long arrayId, final int arrayIndex) {
            super(arrayInstr, occurenceNumber, stackDepth);
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
        public String toString() {
            final String type = super.toString();
            final StringBuilder sb = new StringBuilder(type.length() + 20);
            sb.append(type).append(" [").append(this.arrayId).append(", ").append(this.arrayIndex).append(']');
            return sb.toString();
        }

    }

    private final int arrayTraceSeqIndex;
    private final int indexTraceSeqIndex;

    public ArrayInstruction(final ReadMethod readMethod, final int opcode,
            final int lineNumber,
            final int arrayTraceSeqIndex, final int indexTraceSeqIndex) {
        super(readMethod, opcode, lineNumber);
        this.arrayTraceSeqIndex = arrayTraceSeqIndex;
        this.indexTraceSeqIndex = indexTraceSeqIndex;
    }

    private ArrayInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode,
            final int arrayTraceSeqIndex, final int indexTraceSeqIndex, final int index) {
        super(readMethod, opcode, lineNumber, index);
        this.arrayTraceSeqIndex = arrayTraceSeqIndex;
        this.indexTraceSeqIndex = indexTraceSeqIndex;
    }

    @Override
    public ArrayInstrInstance getNextInstance(
            final TraceIterationInformationProvider infoProv, final int stackDepth) {
        final long arrayId = infoProv.getNextLong(this.arrayTraceSeqIndex);
        final int index = infoProv.getNextInteger(this.indexTraceSeqIndex);
        return new ArrayInstrInstance(this, infoProv.getNextInstructionOccurenceNumber(getIndex()),
                stackDepth, arrayId, index);
    }

    public Type getType() {
        return Type.ARRAY;
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        OptimizedDataOutputStream.writeInt0(this.arrayTraceSeqIndex, out);
        OptimizedDataOutputStream.writeInt0(this.indexTraceSeqIndex, out);
    }

    public static ArrayInstruction readFrom(final DataInputStream in, final MethodReadInformation methodInfo,
            @SuppressWarnings("unused") final StringCacheInput stringCache,
            final int opcode, final int index, final int lineNumber) throws IOException {
        final int arrayTraceSeqIndex = OptimizedDataInputStream.readInt0(in);
        final int indexTraceSeqIndex = OptimizedDataInputStream.readInt0(in);
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
