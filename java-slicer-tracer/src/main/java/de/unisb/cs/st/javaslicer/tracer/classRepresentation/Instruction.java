package de.unisb.cs.st.javaslicer.tracer.classRepresentation;



public abstract class Instruction {

    private static int nextIndex = 0;

    private final int index;
    protected final ReadMethod method;

    public Instruction(final ReadMethod readMethod) {
        this.index = nextIndex++;
        if (nextIndex < 0)
            throw new RuntimeException("Integer overflow in instruction index");
        this.method = readMethod;
    }

    public int getIndex() {
        return this.index;
    }

    public ReadMethod getMethod() {
        return this.method;
    }

    public static int getNextIndex() {
        return nextIndex;
    }

    public int getBackwardInstructionIndex() {
        return this.index - 1;
    }

}
