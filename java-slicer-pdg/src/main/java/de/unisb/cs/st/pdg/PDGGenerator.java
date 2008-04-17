package de.unisb.cs.st.pdg;

import org.objectweb.asm.ClassReader;

public class PDGGenerator {

    private final PDGModel model;
    private final PDGClassVisitor classVisitor;

    public PDGGenerator(PDGModel model) {
        this.model = model;
        this.classVisitor = new PDGClassVisitor(model);
    }

    public void readClass(ClassReader classReader) {
        classReader.accept(classVisitor, 0);
    }

}
