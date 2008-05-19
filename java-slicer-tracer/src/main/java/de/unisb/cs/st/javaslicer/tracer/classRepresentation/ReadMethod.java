package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
        readClass.addMethod(this);
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

    public void writeOut(final ObjectOutputStream out) throws IOException {
        final char[] nameChars = this.name.toCharArray();
        out.writeInt(nameChars.length);
        for (final char ch: nameChars)
            out.writeChar(ch);
        final char[] descChars = this.desc.toCharArray();
        out.writeInt(descChars.length);
        for (final char ch: descChars)
            out.writeChar(ch);
        out.writeInt(this.instructions.size());
        for (final Instruction instr: this.instructions)
            out.writeObject(instr);
    }

    public static ReadMethod readFrom(final ObjectInputStream in, final ReadClass readClass) throws IOException {
        final char[] nameChars = new char[in.readInt()];
        for (int i = 0; i < nameChars.length; ++i)
            nameChars[i] = in.readChar();
        final char[] descChars = new char[in.readInt()];
        for (int i = 0; i < descChars.length; ++i)
            descChars[i] = in.readChar();
        final ReadMethod rm = new ReadMethod(readClass, new String(nameChars), new String(descChars));
        int numInstr = in.readInt();
        rm.instructions.ensureCapacity(numInstr);
        while (numInstr-- > 0)
            try {
                rm.instructions.add((Instruction)in.readObject());
            } catch (final ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        return rm;
    }

}
