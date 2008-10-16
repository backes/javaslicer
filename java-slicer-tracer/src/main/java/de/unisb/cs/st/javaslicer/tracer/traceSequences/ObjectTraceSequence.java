package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.io.DataOutputStream;
import java.io.IOException;

public class ObjectTraceSequence implements TraceSequence {

    private final LongTraceSequence longTraceSequence;

    public ObjectTraceSequence(final LongTraceSequence longTraceSequence) {
        this.longTraceSequence = longTraceSequence;
    }

    public void trace(final Object obj) throws IOException {
        if (obj instanceof Identifiable) {
            this.longTraceSequence.trace(((Identifiable)obj).__tracing_get_object_id());
        } else {
            this.longTraceSequence.trace(ObjectIdentifier.instance.getObjectId(obj));
        }
    }

    public void writeOut(final DataOutputStream out) throws IOException {
        this.longTraceSequence.writeOut(out);
    }

    public void finish() throws IOException {
        this.longTraceSequence.finish();
    }

}
