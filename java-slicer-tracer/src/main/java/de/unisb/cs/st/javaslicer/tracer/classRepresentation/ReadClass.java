package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.util.ArrayList;

public class ReadClass {

    private final String className;
    private final byte[] classByteCode;
    private final ArrayList<ReadMethod> methods = new ArrayList<ReadMethod>();

    public ReadClass(final String className, final byte[] classBytecode) {
        this.className = className;
        this.classByteCode = classBytecode;
    }

    public void addMethod(final ReadMethod method) {
        this.methods.add(method);
    }

    public void ready() {
        this.methods.trimToSize();
    }

}
