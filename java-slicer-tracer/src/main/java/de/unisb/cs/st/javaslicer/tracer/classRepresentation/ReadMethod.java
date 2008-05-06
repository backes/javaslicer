package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.util.ArrayList;

public class ReadMethod {

    private final ArrayList<Instruction> instructions = new ArrayList<Instruction>();
    private final ReadClass readClass;
    private final String name;
    private final String desc;

    public ReadMethod(final ReadClass readClass, final String name, final String desc) {
        this.readClass = readClass;
        this.name = name;
        this.desc = desc;
    }

    public int addInstruction(final Instruction instruction) {
        this.instructions.add(instruction);
        return this.instructions.size()-1;
    }

    public void ready() {
        this.instructions.trimToSize();
    }

    public ArrayList<Instruction> getInstructions() {
        return this.instructions;
    }

    public ReadClass getReadClass() {
        return this.readClass;
    }

    public String getName() {
        return this.name;
    }

    public String getDesc() {
        return this.desc;
    }

}
