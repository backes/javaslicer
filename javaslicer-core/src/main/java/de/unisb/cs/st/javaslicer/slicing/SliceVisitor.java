package de.unisb.cs.st.javaslicer.slicing;

import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;


public interface SliceVisitor {

    void visitMatchedInstance(InstructionInstance instance);

    void visitSliceDependence(InstructionInstance from, InstructionInstance to,
            int distance);

}
