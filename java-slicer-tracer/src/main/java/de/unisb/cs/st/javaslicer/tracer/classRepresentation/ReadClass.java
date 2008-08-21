package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

import org.objectweb.asm.Type;

public class ReadClass {

    private final String internalClassName;
    private final String className;
    private final ArrayList<ReadMethod> methods = new ArrayList<ReadMethod>();
    private final int instructionNumberStart;
    private int instructionNumberEnd;
    private String source = null;
    private final int access;

    public ReadClass(final String internalClassName, final int instructionNumberStart, final int access) {
        this.internalClassName = internalClassName;
        this.className = Type.getObjectType(internalClassName).getClassName();
        this.instructionNumberStart = instructionNumberStart;
        this.access = access;
    }

    public void addMethod(final ReadMethod method) {
        this.methods.add(method);
    }

    public void ready() {
        this.methods.trimToSize();
    }

    public int getAccess() {
        return this.access;
    }

    public String getName() {
        return this.className;
    }

    public void setInstructionNumberEnd(final int instructionNumberEnd) {
        this.instructionNumberEnd = instructionNumberEnd;
    }

    public String getInternalClassName() {
        return this.internalClassName;
    }

    public ArrayList<ReadMethod> getMethods() {
        return this.methods;
    }

    public int getInstructionNumberStart() {
        return this.instructionNumberStart;
    }

    public int getInstructionNumberEnd() {
        return this.instructionNumberEnd;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public void writeOut(final DataOutput out) throws IOException {
        out.writeUTF(this.internalClassName);
        out.writeInt(this.instructionNumberStart);
        out.writeInt(this.instructionNumberEnd);
        out.writeInt(this.access);
        out.writeInt(this.methods.size());
        for (final ReadMethod rm: this.methods) {
            rm.writeOut(out);
        }
        out.writeUTF(this.source == null ? "" : this.source);
    }

    public static ReadClass readFrom(final DataInput in) throws IOException {
        final String intName = in.readUTF();
        final int instructionNumberStart = in.readInt();
        final int instructionNumberEnd = in.readInt();
        final int access = in.readInt();
        final ReadClass rc = new ReadClass(intName, instructionNumberStart, access);
        rc.setInstructionNumberEnd(instructionNumberEnd);
        int numMethods = in.readInt();
        rc.methods.ensureCapacity(numMethods);
        while (numMethods-- > 0)
            rc.methods.add(ReadMethod.readFrom(in, rc));
        rc.methods.trimToSize();
        final String source = in.readUTF();
        if (source.length() > 0)
            rc.setSource(source);
        return rc;
    }

    @Override
    public String toString() {
        return this.className;
    }

}
