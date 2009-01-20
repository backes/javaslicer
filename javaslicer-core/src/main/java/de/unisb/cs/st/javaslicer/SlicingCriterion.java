package de.unisb.cs.st.javaslicer;

import java.util.Collection;

import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.dependencyAnalysis.ExecutionFrame;
import de.unisb.cs.st.javaslicer.variables.Variable;

public interface SlicingCriterion {

    public interface Instance {

        boolean matches(Instruction.InstructionInstance instructionInstance);

        Collection<Variable> getInterestingVariables(ExecutionFrame execFrame);

        Collection<Instruction> getInterestingInstructions(ExecutionFrame currentFrame);

    }

    Instance getInstance();

}
