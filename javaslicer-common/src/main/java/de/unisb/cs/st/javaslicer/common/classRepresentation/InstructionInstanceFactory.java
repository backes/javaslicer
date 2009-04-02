package de.unisb.cs.st.javaslicer.common.classRepresentation;

import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.AbstractInstruction;


public interface InstructionInstanceFactory {

    InstructionInstance createInstructionInstance(AbstractInstruction instruction,
            long occurenceNumber, int stackDepth, long instanceNr,
            InstructionInstanceInfo additionalInfo);

}
