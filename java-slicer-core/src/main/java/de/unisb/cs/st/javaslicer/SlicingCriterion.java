package de.unisb.cs.st.javaslicer;

import java.util.Collection;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction.Instance;

public interface SlicingCriterion {

    boolean matches(Instance instructionInstance);

    Collection<Variable> getInterestingVariables();

}
