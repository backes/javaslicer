package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.unisb.cs.st.javaslicer.tracer.util.OptimizedDataOutputStream;

public class StringCacheOutput {

    private final Map<String, Integer> cache = new HashMap<String, Integer>();

    public void writeString(final String s, final DataOutputStream out) throws IOException {
        Integer id = this.cache.get(s);
        if (id == null) {
            id = getId(this.cache.size());
            this.cache.put(s, id);
            OptimizedDataOutputStream.writeInt0(id, out);
            out.writeUTF(s);
        } else {
            OptimizedDataOutputStream.writeInt0(id, out);
        }
    }

    private int getId(final int id) {
        return (id & 1) == 0 ? (id+1)/2 : -(id+1)/2;
    }

}
