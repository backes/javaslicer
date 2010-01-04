package de.unisb.cs.st.javaslicer;

import java.util.List;

import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.LocalVariable;

/**
 * Interface for a slicing criterion.
 * Each slicing criterion may match an arbitrary number of dynamic instruction
 * instances, originating from an arbitrary number of static instructions.
 *
 * @author Clemens Hammacher
 */
public interface SlicingCriterion {

    public interface SlicingCriterionInstance {

        boolean matches(InstructionInstance instructionInstance);

        boolean hasLocalVariables();

        List<LocalVariable> getLocalVariables();

        boolean matchAllData();

        long getOccurenceNumber();

    }

    SlicingCriterionInstance getInstance();

}
