package de.unisb.cs.st.javaslicer.tracer.traceSequences.gzip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.uncompressed.UncompressedIntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileWriter.MultiplexOutputStream;

public class GZipIntegerTraceSequence extends UncompressedIntegerTraceSequence {

    public GZipIntegerTraceSequence(final Tracer tracer) throws IOException {
        super(tracer);
    }

    @Override
    protected OutputStream getOutputStream(final MultiplexOutputStream out) throws IOException {
        return new GZIPOutputStream(super.getOutputStream(out));
    }

}
