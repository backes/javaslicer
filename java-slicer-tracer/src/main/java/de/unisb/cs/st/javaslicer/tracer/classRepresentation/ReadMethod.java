package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

import de.unisb.cs.st.javaslicer.tracer.util.IntegerMap;

public class ReadMethod {

    public static class MethodReadInformation {

        private final ReadMethod method;
        protected final IntegerMap<LabelMarker> labels = new IntegerMap<LabelMarker>();

        public MethodReadInformation(final ReadMethod method) {
            this.method = method;
        }

        public ReadMethod getMethod() {
            return this.method;
        }

        public LabelMarker getLabel(final int labelNr) throws IOException {
            final LabelMarker lm = this.labels.get(labelNr);
            if (lm == null)
                throw new IOException("corrupted data (illegal label)");
            return lm;
        }

    }

    private final ArrayList<AbstractInstruction> instructions = new ArrayList<AbstractInstruction>();
    private final ReadClass readClass;
    private final String name;
    private final String desc;
    private final int instructionNumberStart;
    private int instructionNumberEnd;

    public ReadMethod(final ReadClass readClass, final String name, final String desc, final int instructionNumberStart) {
        this.readClass = readClass;
        this.name = name;
        this.desc = desc;
        this.instructionNumberStart = instructionNumberStart;
    }

    public int addInstruction(final AbstractInstruction instruction) {
        this.instructions.add(instruction);
        return this.instructions.size()-1;
    }

    public void ready() {
        this.instructions.trimToSize();
    }

    public ArrayList<AbstractInstruction> getInstructions() {
        return this.instructions;
    }

    public ReadClass getReadClass() {
        return this.readClass;
    }

    public String getName() {
        return this.name;
    }

    public String getDesc() {
        return this.desc;
    }

    public int getInstructionNumberStart() {
        return this.instructionNumberStart;
    }

    public int getInstructionNumberEnd() {
        return this.instructionNumberEnd;
    }

    public void setInstructionNumberEnd(final int instructionNumberEnd) {
        this.instructionNumberEnd = instructionNumberEnd;
    }

    public void writeOut(final DataOutput out) throws IOException {
        out.writeUTF(this.name);
        out.writeUTF(this.desc);
        out.writeInt(this.instructionNumberStart);
        out.writeInt(this.instructionNumberEnd);
        out.writeInt(this.instructions.size());
        for (final Instruction instr: this.instructions)
            if (instr instanceof LabelMarker)
                instr.writeOut(out);
        for (final Instruction instr: this.instructions)
            if (!(instr instanceof LabelMarker))
                instr.writeOut(out);
    }

    public static ReadMethod readFrom(final DataInput in, final ReadClass readClass) throws IOException {
        final String name = in.readUTF();
        final String desc = in.readUTF();
        final int instructionNumberStart = in.readInt();
        final int instructionNumberEnd = in.readInt();
        final ReadMethod rm = new ReadMethod(readClass, name, desc, instructionNumberStart);
        rm.setInstructionNumberEnd(instructionNumberEnd);
        int numInstr = in.readInt();
        rm.instructions.ensureCapacity(numInstr);
        final Queue<LabelMarker> labels = new ArrayDeque<LabelMarker>();
        final MethodReadInformation mri = new MethodReadInformation(rm);
        AbstractInstruction instr = null;
        while (numInstr-- > 0) {
            instr = AbstractInstruction.readFrom(in, mri);
            if (!(instr instanceof LabelMarker))
                break;
            final LabelMarker lm = (LabelMarker) instr;
            labels.add(lm);
            mri.labels.put(lm.getLabelNr(), lm);
        }
        while (instr != null || numInstr-- > 0) {
            if (instr == null)
                instr = AbstractInstruction.readFrom(in, mri);
            while (!labels.isEmpty() && labels.peek().getIndex() < instr.getIndex())
                rm.instructions.add(labels.poll());
            rm.instructions.add(instr);
            instr = null;
        }
        rm.instructions.addAll(labels);
        rm.instructions.trimToSize();

        return rm;
    }

    @Override
    public String toString() {
        return this.name + this.desc;
    }

}
