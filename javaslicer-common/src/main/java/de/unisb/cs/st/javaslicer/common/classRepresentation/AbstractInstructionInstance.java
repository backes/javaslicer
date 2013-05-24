/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation
 *    Class:     AbstractInstructionInstance
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/AbstractInstructionInstance.java
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
    private final InstructionInstanceInfo additionalInfo;

    public AbstractInstructionInstance(final AbstractInstruction instr,
            final long occurenceNumber, final int stackDepth,
            long instanceNr, InstructionInstanceInfo additionalInfo) {
        assert instr != null;
        assert occurenceNumber >= 0;
        assert stackDepth >= 0;
        assert instanceNr >= 0;

        this.instruction = instr;
        this.occurenceNumber = occurenceNumber;
        this.stackDepth = stackDepth;
        this.instanceNr = instanceNr;
        this.additionalInfo = additionalInfo;
    }

    @Override
	public long getOccurrenceNumber() {
        return this.occurenceNumber;
    }

    @Override
	public Instruction getInstruction() {
        return this.instruction;
    }

    @Override
	public long getInstanceNr() {
        return this.instanceNr;
    }

    @Override
	public int getStackDepth() {
        return this.stackDepth;
    }

    @Override
	public InstructionInstanceInfo getAdditionalInfo() {
        return this.additionalInfo;
    }

    @Override
    public int hashCode() {
    	return (int) this.instanceNr;
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
        return new StringBuilder(instrStr.length() + infoStr.length()).
            append(instrStr).append(infoStr).toString();
    }

    @Override
	public int compareTo(InstructionInstance o) {
        long thisInstanceNr = getInstanceNr();
        long otherInstanceNr = o.getInstanceNr();
        return thisInstanceNr < otherInstanceNr ? -1 :
            (thisInstanceNr == otherInstanceNr ? 0 : 1);
    }

}
