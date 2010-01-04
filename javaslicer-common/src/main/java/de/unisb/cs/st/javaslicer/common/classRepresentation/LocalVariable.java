package de.unisb.cs.st.javaslicer.common.classRepresentation;

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

    @Override
    public String toString() {
        return this.name == null ? "#" + this.index : this.name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.desc == null) ? 0 : this.desc.hashCode());
        result = prime * result + this.index;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LocalVariable other = (LocalVariable) obj;
        if (this.index != other.index)
            return false;
        if (this.desc == null) {
            if (other.desc != null)
                return false;
        } else if (!this.desc.equals(other.desc))
            return false;
        if (this.name == null) {
            if (other.name != null)
                return false;
        } else if (!this.name.equals(other.name))
            return false;
        return true;
    }

    public static LocalVariable readFrom(final DataInput in) throws IOException {
        final int index = in.readInt();
        final String name = in.readUTF();
        final String desc = in.readUTF();
        return new LocalVariable(index, name, desc);
    }

}
