package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.util.ArrayList;

import org.objectweb.asm.Type;

public class ReadClass {

    private final String internalClassName;
    private final String className;
    private final byte[] classByteCode;
    private final ArrayList<ReadMethod> methods = new ArrayList<ReadMethod>();

    public ReadClass(final String internalClassName, final byte[] classBytecode) {
        this.internalClassName = internalClassName;
        this.className = Type.getObjectType(internalClassName).getClassName();
        this.classByteCode = classBytecode;
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

}
