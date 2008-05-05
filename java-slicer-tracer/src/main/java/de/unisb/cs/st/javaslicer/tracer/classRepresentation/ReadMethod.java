package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.util.ArrayList;

public class ReadMethod {

    private final ArrayList<Instruction> instructions = new ArrayList<Instruction>();

    public ReadMethod(final ReadClass readClass, final String name, final String desc) {
        // TODO Auto-generated constructor stub
    }

    public int addInstruction(final Instruction instruction) {
        this.instructions.add(instruction);
        return this.instructions.size()-1;
    }

    public void ready() {
        this.instructions.trimToSize();
    }

}
