package de.unisb.cs.st.javaslicer.common.classRepresentation;

import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.AbstractInstruction;

/**
 * Standard implementation of an {@link InstructionInstance}.
 * Consists of the wrapped {@link AbstractInstruction}, an occurence number,
 * the stack depth and the absolute instance number.
 *
 * @author Clemens Hammacher
 */
public class AbstractInstance implements InstructionInstance {

    private final AbstractInstruction instruction;
    private final long occurenceNumber;
    private final int stackDepth;
    private final long instanceNr;

    public AbstractInstance(final AbstractInstruction instr,
            final long occurenceNumber, final int stackDepth,
            long instanceNr) {
        assert instr != null;
        assert occurenceNumber >= 0;
        assert stackDepth >= 0;
        assert instanceNr >= 0;

        this.instruction = instr;
        this.occurenceNumber = occurenceNumber;
        this.stackDepth = stackDepth;
        this.instanceNr = instanceNr;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.instanceNr ^ (this.instanceNr >>> 32));
        result = prime * result + this.instruction.hashCode();
        result = prime * result + (int) (this.occurenceNumber ^ (this.occurenceNumber >>> 32));
        result = prime * result + this.stackDepth;
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
        AbstractInstance other = (AbstractInstance) obj;
        if (this.instanceNr != other.instanceNr)
            return false;
        if (this.occurenceNumber != other.occurenceNumber)
            return false;
        if (this.stackDepth != other.stackDepth)
            return false;
        if (!this.instruction.equals(other.instruction))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return this.instruction.toString();
    }

    public int compareTo(InstructionInstance o) {
        long thisInstanceNr = getInstanceNr();
        long otherInstanceNr = o.getInstanceNr();
        return thisInstanceNr < otherInstanceNr ? -1 :
            (thisInstanceNr == otherInstanceNr ? 0 : 1);
    }

}
