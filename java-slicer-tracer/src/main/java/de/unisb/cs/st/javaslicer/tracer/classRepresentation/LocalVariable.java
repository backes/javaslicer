package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LocalVariable {

    private final int index;
    private final String name;
    private final String desc;

    public LocalVariable(final int index, final String name, final String desc) {
        this.index = index;
        this.name = name;
        this.desc = desc;
    }

    public int getIndex() {
        return this.index;
    }

    public String getName() {
        return this.name;
    }

    public String getDesc() {
        return this.desc;
    }

    public void writeOut(final DataOutput out) throws IOException {
        out.writeInt(this.index);
        out.writeUTF(this.name);
        out.writeUTF(this.desc);
    }

    public static LocalVariable readFrom(final DataInput in) throws IOException {
        final int index = in.readInt();
        final String name = in.readUTF();
        final String desc = in.readUTF();
        return new LocalVariable(index, name, desc);
    }

}
