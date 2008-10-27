package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.objectweb.asm.Type;

import de.unisb.cs.st.javaslicer.tracer.util.OptimizedDataInputStream;
import de.unisb.cs.st.javaslicer.tracer.util.OptimizedDataOutputStream;

public class ReadClass implements Comparable<ReadClass> {

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

    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        stringCache.writeString(this.internalClassName, out);
        OptimizedDataOutputStream.writeInt0(this.instructionNumberStart, out);
        OptimizedDataOutputStream.writeInt0(this.access, out);
        OptimizedDataOutputStream.writeInt0(this.methods.size(), out);
        for (final ReadMethod rm: this.methods) {
            rm.writeOut(out, stringCache);
        }
        stringCache.writeString(this.source == null ? "" : this.source, out);
    }

    public static ReadClass readFrom(final DataInputStream in, final StringCacheInput stringCache) throws IOException {
        final String intName = stringCache.readString(in);
        if (intName.isEmpty())
            throw new IOException("corrupted data");
        final int instructionNumberStart = OptimizedDataInputStream.readInt0(in);
        final int access = OptimizedDataInputStream.readInt0(in);
        final ReadClass rc = new ReadClass(intName, instructionNumberStart, access);
        int numMethods = OptimizedDataInputStream.readInt0(in);
        rc.methods.ensureCapacity(numMethods);
        int instrIndex = instructionNumberStart;
        while (numMethods-- > 0) {
            final ReadMethod newMethod = ReadMethod.readFrom(in, rc, instrIndex, stringCache);
            instrIndex = newMethod.getInstructionNumberEnd();
            rc.methods.add(newMethod);
        }
        rc.setInstructionNumberEnd(instrIndex);
        rc.methods.trimToSize();
        Collections.sort(rc.methods, new Comparator<ReadMethod>() {
            @Override
            public int compare(final ReadMethod o1, final ReadMethod o2) {
                int cmp = o1.getName().compareTo(o2.getName());
                if (cmp != 0)
                    return cmp;
                cmp = o1.getDesc().compareTo(o2.getDesc());
                if (cmp != 0)
                    return cmp;
                return o1.getAccess() - o2.getAccess();
            }
        });
        final String source = stringCache.readString(in);
        if (source.length() > 0)
            rc.setSource(source);
        return rc;
    }

    @Override
    public String toString() {
        return this.className;
    }

    @Override
    public int compareTo(final ReadClass o) {
        return this.className.compareTo(o.className);
    }

}
