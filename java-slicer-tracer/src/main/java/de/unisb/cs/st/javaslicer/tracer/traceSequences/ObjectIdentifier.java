package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import de.unisb.cs.st.javaslicer.tracer.util.ConcurrentReferenceHashMap;

public class ObjectIdentifier {

    public final static ObjectIdentifier instance = new ObjectIdentifier();

    private final ConcurrentMap<Object, Long> objectMap =
        new ConcurrentReferenceHashMap<Object, Long>(65536, 0.75f, 16, ConcurrentReferenceHashMap.ReferenceType.WEAK,
                ConcurrentReferenceHashMap.ReferenceType.STRONG, EnumSet.of(ConcurrentReferenceHashMap.Option.IDENTITY_COMPARISONS));

    private final ConcurrentLinkedQueue<Long> freeIds = new ConcurrentLinkedQueue<Long>();

    private final AtomicLong nextId = new AtomicLong(1);

    private ObjectIdentifier() {
        // private constructor ==> singleton
    }

    public long getObjectId(final Object obj) {
        if (obj instanceof Identifiable)
            return ((Identifiable)obj).__tracing_get_object_id();

        final Long id = this.objectMap.get(obj);
        return id == null ? getNewId(obj, false) : id;
    }

    public long getNewId(final Object obj, final boolean isIdentifiable) {
        Long newId = this.freeIds.poll();
        if (newId == null) {
            newId = this.nextId.getAndIncrement();
            if (newId.longValue() == 0)
                throw new RuntimeException("long overflow in object ids");
        }
        if (!isIdentifiable) {
            final Long oldId = this.objectMap.putIfAbsent(obj, newId);
            if (oldId != null) {
                this.freeIds.add(newId);
                return oldId;
            }
        }

        return newId;
    }

}
