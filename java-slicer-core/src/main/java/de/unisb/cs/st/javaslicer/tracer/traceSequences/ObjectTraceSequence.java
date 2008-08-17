package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.io.DataOutput;
import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import de.unisb.cs.st.javaslicer.tracer.util.ConcurrentReferenceHashMap;

public class ObjectTraceSequence implements TraceSequence {

    private static final ConcurrentMap<Object, Long> objectMap =
        new ConcurrentReferenceHashMap<Object, Long>(65536, 0.75f, 16, ConcurrentReferenceHashMap.ReferenceType.SOFT,
                ConcurrentReferenceHashMap.ReferenceType.STRONG, EnumSet.of(ConcurrentReferenceHashMap.Option.IDENTITY_COMPARISONS));

    private static final AtomicLong nextId = new AtomicLong(0);

    private final LongTraceSequence longTraceSequence;

    public ObjectTraceSequence(final LongTraceSequence longTraceSequence) {
        this.longTraceSequence = longTraceSequence;
    }

    public int getIndex() {
        return this.longTraceSequence.getIndex();
    }

    public void trace(final Object obj) throws IOException {
        Long id = objectMap.get(obj);
        if (id == null) {
            id = getIdUnderLock(obj);
        }
        this.longTraceSequence.trace(id);
    }

    private static synchronized Long getIdUnderLock(final Object obj) {
        // re-try to get id from map
        Long id = objectMap.get(obj);

        if (id == null) {
            id = nextId.getAndIncrement();
            if (objectMap.put(obj, id) != null)
                throw new AssertionError("illegal non-synchronized map modification");
        }

        return id;
    }

    public void writeOut(final DataOutput out) throws IOException {
        this.longTraceSequence.writeOut(out);
    }

    public void finish() throws IOException {
        this.longTraceSequence.finish();
    }

}
