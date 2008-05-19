package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.map.ReferenceIdentityMap;

@SuppressWarnings("unchecked")
public class ObjectTraceSequence implements TraceSequence {

    protected static final Map<Object, Long> objectMap = Collections.synchronizedMap(
            LazyMap.decorate(
                    new ReferenceIdentityMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.HARD),
                    new Factory() {
                        public Object create() {
                            return new Long(objectMap.size());
                        }
                    }));

    private final LongTraceSequence longTraceSequence;

    public ObjectTraceSequence(final LongTraceSequence longTraceSequence) {
        this.longTraceSequence = longTraceSequence;
    }

    public int getIndex() {
        return this.longTraceSequence.getIndex();
    }

    public void trace(final Object obj) {
        this.longTraceSequence.trace(objectMap.get(obj));
    }

    public void writeOut(final ObjectOutputStream out) throws IOException {
        this.longTraceSequence.writeOut(out);
    }

}
