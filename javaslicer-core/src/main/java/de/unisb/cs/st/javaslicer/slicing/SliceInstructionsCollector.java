package de.unisb.cs.st.javaslicer.slicing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.variables.Variable;


public class SliceInstructionsCollector implements SliceVisitor {

    private final Set<Instruction> dynamicSlice = new HashSet<Instruction>();

    public void visitMatchedInstance(InstructionInstance instance) {
        this.dynamicSlice.add(instance.getInstruction());
    }

    public void visitSliceDependence(InstructionInstance from,
    		InstructionInstance to, Variable variable, int distance) {
        this.dynamicSlice.add(to.getInstruction());
    }

    public Set<Instruction> getDynamicSlice() {
        return Collections.unmodifiableSet(this.dynamicSlice);
    }

}
