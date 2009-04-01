package de.unisb.cs.st.javaslicer.common.classRepresentation;

import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.AbstractInstruction;

/**
 * Standard implementation of an {@link InstructionInstance}.
 * Consists of the wrapped {@link AbstractInstruction}, an occurence number,
 * the stack depth, the absolute instance number, the instance type and
 * additonal information provided depending on the type.
 *
 * @author Clemens Hammacher
 */
public class AbstractInstructionInstance implements InstructionInstance {

    private final AbstractInstruction instruction;
    private final long occurenceNumber;
    private final int stackDepth;
    private final long instanceNr;
    private final InstructionInstanceType type;
    private final InstructionInstanceInfo additionalInfo;

    public AbstractInstructionInstance(final AbstractInstruction instr,
            final long occurenceNumber, final int stackDepth,
            long instanceNr, InstructionInstanceType type,
            InstructionInstanceInfo additionalInfo) {
        assert instr != null;
        assert occurenceNumber >= 0;
        assert stackDepth >= 0;
        assert instanceNr >= 0;
        assert type != null;

        this.instruction = instr;
        this.occurenceNumber = occurenceNumber;
        this.stackDepth = stackDepth;
        this.instanceNr = instanceNr;
        this.type = type;
        this.additionalInfo = additionalInfo;
    }

    public long getOccurrenceNumber() {
        return this.occurenceNumber;
    }

    public Instruction getInstruction() {
        return this.instruction;
    }

    public long getInstanceNr() {
        return this.instanceNr;
    }

    public int getStackDepth() {
        return this.stackDepth;
    }

    public InstructionInstanceType getType() {
        return this.type;
    }

    public InstructionInstanceInfo getAdditionalInfo() {
        return this.additionalInfo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.instanceNr ^ (this.instanceNr >>> 32));
        result = prime * result + this.instruction.hashCode();
        result = prime * result + (int) (this.occurenceNumber ^ (this.occurenceNumber >>> 32));
        result = prime * result + this.stackDepth;
        result = prime * result + this.type.ordinal();
        result = prime * result + (this.additionalInfo == null ? 0 : this.additionalInfo.hashCode());
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
        AbstractInstructionInstance other = (AbstractInstructionInstance) obj;
        if (this.instanceNr != other.instanceNr)
            return false;
        if (this.occurenceNumber != other.occurenceNumber)
            return false;
        if (this.stackDepth != other.stackDepth)
            return false;
        if (!this.instruction.equals(other.instruction))
            return false;
        if (!this.type.equals(other.type))
            return false;
        if (this.additionalInfo == null) {
            if (other.additionalInfo != null)
                return false;
        } else {
            if (!this.additionalInfo.equals(other.additionalInfo))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String instrStr = this.instruction.toString();
        if (this.additionalInfo == null)
            return instrStr;
        String infoStr = this.additionalInfo.toString();
        if (infoStr == null || infoStr.length() == 0)
            return instrStr;
        return new StringBuilder(instrStr.length() + infoStr.length() + 1).
            append(instrStr).append(' ').append(infoStr).toString();
    }

    public int compareTo(InstructionInstance o) {
        long thisInstanceNr = getInstanceNr();
        long otherInstanceNr = o.getInstanceNr();
        return thisInstanceNr < otherInstanceNr ? -1 :
            (thisInstanceNr == otherInstanceNr ? 0 : 1);
    }

}
