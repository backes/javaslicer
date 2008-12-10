package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import de.hammacher.util.ConcurrentReferenceHashMap;

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
        final Long id = this.objectMap.get(obj);
        return id == null ? getNewId(obj) : id;
    }

    // if obj != null, the id is stored in the objectMap
    public long getNewId(final Object obj) {
        Long newId = this.freeIds.poll();
        if (newId == null) {
            newId = this.nextId.getAndIncrement();
            if (newId.longValue() == 0)
                throw new RuntimeException("long overflow in object ids");
        }
        if (obj != null) {
            final Long oldId = this.objectMap.putIfAbsent(obj, newId);
            if (oldId != null) {
                this.freeIds.add(newId);
                return oldId;
            }
        }

        return newId;
    }

}
