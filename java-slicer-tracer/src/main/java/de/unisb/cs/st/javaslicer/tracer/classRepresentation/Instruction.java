package de.unisb.cs.st.javaslicer.tracer.classRepresentation;



public abstract class Instruction {

    private static int nextIndex = 0;

    private final int index;
    protected final ReadMethod method;
    private final int opcode;
    private final int lineNumber;

    public Instruction(final ReadMethod readMethod, final int opcode, final int lineNumber) {
        this.index = nextIndex++;
        if (nextIndex < 0)
            throw new RuntimeException("Integer overflow in instruction index");
        this.method = readMethod;
        this.opcode = opcode;
        this.lineNumber = lineNumber;
    }

    public int getIndex() {
        return this.index;
    }

    public ReadMethod getMethod() {
        return this.method;
    }

    public int getOpcode() {
        return this.opcode;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public static int getNextIndex() {
        return nextIndex;
    }

    public int getBackwardInstructionIndex() {
        return this.index - 1;
    }

}
