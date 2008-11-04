package de.unisb.cs.st.javaslicer;

import java.util.Collection;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;

public interface SlicingCriterion {

    public interface Instance {

        boolean matches(Instruction.Instance instructionInstance);

        Collection<Variable> getInterestingVariables(ExecutionFrame execFrame);

        Collection<Instruction> getInterestingInstructions(ExecutionFrame currentFrame);

    }

    Instance getInstance();

}
