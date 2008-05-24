package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.objectweb.asm.Type;

public class ReadClass {

    private final String internalClassName;
    private final String className;
    private final byte[] classByteCode;
    private final ArrayList<ReadMethod> methods = new ArrayList<ReadMethod>();
    private final int instructionNumberStart;
    private int instructionNumberEnd;
    private String source = null;

    public ReadClass(final String internalClassName, final byte[] classBytecode, final int instructionNumberStart) {
        this.internalClassName = internalClassName;
        this.className = Type.getObjectType(internalClassName).getClassName();
        this.classByteCode = classBytecode;
        this.instructionNumberStart = instructionNumberStart;
    }

    public void addMethod(final ReadMethod method) {
        this.methods.add(method);
    }

    public void ready() {
        this.methods.trimToSize();
    }

    public String getClassName() {
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

    public void writeOut(final ObjectOutputStream out) throws IOException {
        out.writeObject(this.internalClassName);
        out.writeObject(this.className);
        out.writeInt(this.classByteCode.length);
        out.write(this.classByteCode);
        out.writeInt(this.instructionNumberStart);
        out.writeInt(this.instructionNumberEnd);
        out.writeInt(this.methods.size());
        for (final ReadMethod rm: this.methods) {
            rm.writeOut(out);
        }
        if (this.source == null)
            out.writeInt(-1);
        else {
            final char[] sourceChars = this.source.toCharArray();
            out.writeInt(sourceChars.length);
            for (final char ch: sourceChars)
                out.writeChar(ch);
        }
    }

    public static ReadClass readFrom(final ObjectInputStream in) throws IOException {
        try {
            final String intName = (String) in.readObject();
            final String className = (String) in.readObject();
            final byte[] bytecode = new byte[in.readInt()];
            in.read(bytecode, 0, bytecode.length);
            final int instructionNumberStart = in.readInt();
            final int instructionNumberEnd = in.readInt();
            final ReadClass rc = new ReadClass(intName, bytecode, instructionNumberStart);
            rc.setInstructionNumberEnd(instructionNumberEnd);
            assert rc.className != null && rc.className.equals(className);
            int numMethods = in.readInt();
            rc.methods.ensureCapacity(numMethods);
            while (numMethods-- > 0)
                rc.methods.add(ReadMethod.readFrom(in, rc));
            final int sourceCharLen = in.readInt();
            if (sourceCharLen != -1) {
                final char[] sourceChars = new char[sourceCharLen];
                for (int i = 0; i < sourceCharLen; ++i)
                    sourceChars[i] = in.readChar();
                rc.setSource(new String(sourceChars));
            }
            return rc;
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
