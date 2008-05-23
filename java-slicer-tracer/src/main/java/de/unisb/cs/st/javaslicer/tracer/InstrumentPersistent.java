package de.unisb.cs.st.javaslicer.tracer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;

public class InstrumentPersistent {

    public static void main(final String[] args) {

        if (args.length != 1) {
            System.out.println("Expecting exaclty 1 argument: the folder to search for .class files to manipulate");
            System.exit(1);
        }

        final File sourceDir = new File(args[0]);
        if (!sourceDir.exists()) {
            System.out.println("ERROR: file or directory " + sourceDir.getAbsolutePath() + " does not exist");
            System.exit(1);
        }

        final Tracer tracer = Tracer.getInstance();
        try {
            manipulate(tracer, sourceDir);
        } catch (final IOException e) {
            System.err.println("ERROR:");
            e.printStackTrace();
        }
    }

    private static void manipulate(final Tracer tracer, final File sourceFile) throws IOException {
        if (sourceFile.isDirectory()) {
            for (final File file : sourceFile.listFiles())
                manipulate(tracer, file);
            return;
        }

        System.out.println("Manipulating " + sourceFile.getAbsolutePath());
        final FileInputStream in = new FileInputStream(sourceFile);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] buf = new byte[1024];
        int read;
        while ((read = in.read(buf)) >= 0) {
            out.write(buf, 0, read);
        }

        byte[] classBytes = out.toByteArray();
        in.close();
        out.close();
        final ReadClass readClass = new ReadClass(sourceFile.getName(), classBytes, tracer.getNextSequenceIndex());

        final ClassReader reader = new ClassReader(classBytes);
        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        final ClassInstrumenter instrumenter = new ClassInstrumenter(writer, tracer, readClass);

        reader.accept(instrumenter, 0);

        readClass.setSequenceNumberEnd(tracer.getNextSequenceIndex());

        classBytes = writer.toByteArray();

        final FileOutputStream fout = new FileOutputStream(sourceFile);
        fout.write(classBytes);
        fout.close();
    }
}
