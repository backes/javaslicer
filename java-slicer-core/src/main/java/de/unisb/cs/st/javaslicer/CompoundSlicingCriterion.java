package de.unisb.cs.st.javaslicer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction.Instance;

public class CompoundSlicingCriterion implements SlicingCriterion {

    private final List<SlicingCriterion> criteria = new ArrayList<SlicingCriterion>(2);

    public void add(final SlicingCriterion slicingCriterion) {
        this.criteria.add(slicingCriterion);
    }

    @Override
    public Collection<Variable> getInterestingVariables() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean matches(final Instance instructionInstance) {
        // TODO Auto-generated method stub
        return false;
    }

}
