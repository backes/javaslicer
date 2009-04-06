package de.unisb.cs.st.javaslicer;

import java.util.Collection;

import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.instructionSimulation.ExecutionFrame;
import de.unisb.cs.st.javaslicer.variables.Variable;

public interface SlicingCriterion {

    public interface SlicingCriterionInstance {

        boolean matches(InstructionInstance instructionInstance);

        Collection<Variable> getInterestingVariables(ExecutionFrame<InstructionInstance> execFrame);

        Collection<Instruction> getInterestingInstructions(ExecutionFrame<InstructionInstance> currentFrame);

    }

    SlicingCriterionInstance getInstance();

}
