package de.unisb.cs.st.javaslicer.common.classRepresentation;

import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance.InstructionInstanceType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.AbstractInstruction;


public class AbstractInstructionInstanceFactory implements InstructionInstanceFactory {

    public InstructionInstance createInstructionInstance(AbstractInstruction instruction,
            long occurenceNumber, int stackDepth, long instanceNr,
            InstructionInstanceType type, InstructionInstanceInfo additionalInfo) {

        return new AbstractInstructionInstance(instruction, occurenceNumber, stackDepth, instanceNr, type, additionalInfo);
    }

}
