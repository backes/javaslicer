package de.unisb.cs.st.javaslicer.tracer.classRepresentation;


public abstract class AbstractInstruction implements Instruction {

    private final int index;
    private final ReadMethod method;

    public AbstractInstruction(final ReadMethod readMethod) {
        this.index = readMethod.addInstruction(this);
        this.method = readMethod;
    }

    public int getIndex() {
        return index;
    }

}
