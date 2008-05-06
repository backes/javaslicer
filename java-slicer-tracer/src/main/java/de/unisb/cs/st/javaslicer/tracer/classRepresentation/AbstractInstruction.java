package de.unisb.cs.st.javaslicer.tracer.classRepresentation;


public abstract class AbstractInstruction implements Instruction {

    private static int nextIndex = 0;

    private final int index;
    private final ReadMethod method;

    public AbstractInstruction(final ReadMethod readMethod) {
        this.index = nextIndex++;
        this.method = readMethod;
    }

    public int getIndex() {
        return this.index;
    }

}
