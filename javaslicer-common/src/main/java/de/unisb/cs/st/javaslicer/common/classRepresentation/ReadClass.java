package de.unisb.cs.st.javaslicer.common.classRepresentation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.Type;

import de.hammacher.util.StringCacheInput;
import de.hammacher.util.StringCacheOutput;
import de.hammacher.util.streams.OptimizedDataInputStream;
import de.hammacher.util.streams.OptimizedDataOutputStream;

public class ReadClass implements Comparable<ReadClass> {

    private final String internalClassName;
    private final String className;
    private final ArrayList<ReadMethod> methods = new ArrayList<ReadMethod>();
    private final List<Field> fields;
    private final int instructionNumberStart;
    private int instructionNumberEnd;
    private final String source;
    private final int access;
    private final String superClassName;

    public ReadClass(final String internalClassName, final int instructionNumberStart,
            final int access, final String sourceFile, final List<Field> fields,
            final String superClassName) {
        this.internalClassName = internalClassName;
        this.className = Type.getObjectType(internalClassName).getClassName();
        this.instructionNumberStart = instructionNumberStart;
        this.access = access;
        this.source = sourceFile;
        this.fields = fields;
        this.superClassName = superClassName;
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

    public String getSource() {
        return this.source;
    }

    public List<Field> getFields() {
        return this.fields;
    }

    public String getSuperClassName() {
        return this.superClassName;
    }

    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        stringCache.writeString(this.internalClassName, out);
        OptimizedDataOutputStream.writeInt0(this.instructionNumberStart, out);
        OptimizedDataOutputStream.writeInt0(this.access, out);
        stringCache.writeString(this.source, out);
        stringCache.writeString(this.superClassName, out);
        OptimizedDataOutputStream.writeInt0(this.fields.size(), out);
        for (final Field field: this.fields)
            field.writeOut(out, stringCache);
        OptimizedDataOutputStream.writeInt0(this.methods.size(), out);
        for (final ReadMethod rm: this.methods)
            rm.writeOut(out, stringCache);
    }

    public static ReadClass readFrom(final DataInputStream in, final StringCacheInput stringCache) throws IOException {
        final String intName = stringCache.readString(in);
        if (intName == null || intName.length() == 0)
            throw new IOException("corrupted data");
        final int instructionNumberStart = OptimizedDataInputStream.readInt0(in);
        final int access = OptimizedDataInputStream.readInt0(in);
        final String source = stringCache.readString(in);
        final String superClass = stringCache.readString(in);
        int numFields = OptimizedDataInputStream.readInt0(in);
        List<Field> fields;
        if (numFields == 0)
            fields = Collections.emptyList();
        else
            fields = new ArrayList<Field>(numFields);
        final ReadClass rc = new ReadClass(intName, instructionNumberStart, access, source, fields, superClass);
        while (numFields-- > 0)
            fields.add(Field.readFrom(in, stringCache, rc));
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
        Collections.sort(rc.methods);
        return rc;
    }

    /**
     * Returns the class name (in Java notation, not internal).
     */
    @Override
    public String toString() {
        return this.className;
    }

    public int compareTo(final ReadClass o) {
        return this.className.compareTo(o.className);
    }

    @Override
    public int hashCode() {
        return this.className.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ReadClass other = (ReadClass) obj;
        if (this.instructionNumberStart != other.instructionNumberStart)
            return false;
        if (!this.className.equals(other.className))
            return false;
        return true;
    }

}
