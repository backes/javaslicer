package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;

import de.unisb.cs.st.javaslicer.tracer.util.ConcurrentReferenceHashMap;

public class ObjectIdentifier {

    public final static ObjectIdentifier instance = new ObjectIdentifier();

    private final ConcurrentMap<Object, Long> objectMap =
        new ConcurrentReferenceHashMap<Object, Long>(65536, 0.75f, 16, ConcurrentReferenceHashMap.ReferenceType.WEAK,
                ConcurrentReferenceHashMap.ReferenceType.STRONG, EnumSet.of(ConcurrentReferenceHashMap.Option.IDENTITY_COMPARISONS));

    private long nextId = 1;

    private ObjectIdentifier() {
        // private constructor ==> singleton
    }

    public long getObjectId(final Object obj) {
        final Long id = this.objectMap.get(obj);
        return id == null ? getIdUnderLock(obj) : id;
    }

    // TODO try non-blocking synchronization
    private synchronized long getIdUnderLock(final Object obj) {
        // re-try to get id from map
        final Long id = this.objectMap.get(obj);
        if (id != null)
            return id;

        final long l = this.nextId++;
        if (l == 0)
            throw new RuntimeException("long overflow in object ids");
        if (this.objectMap.put(obj, l) != null)
            throw new AssertionError("illegal non-synchronized map modification");
        return l;
    }

}
