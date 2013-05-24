/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer.traceSequences
 *    Class:     ObjectIdentifier
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/traceSequences/ObjectIdentifier.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import de.hammacher.util.maps.ConcurrentReferenceHashMap;

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
