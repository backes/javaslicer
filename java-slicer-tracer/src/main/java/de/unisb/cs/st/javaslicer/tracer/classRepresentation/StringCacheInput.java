package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.javaslicer.tracer.util.OptimizedDataInputStream;

public class StringCacheInput {

    private final List<String> strings = new ArrayList<String>();

    private int reverseId(final int id) {
        return id < 0 ? -2*id - 1 : 2*id;
    }

    public String readString(final DataInputStream in) throws IOException {
        final int id = reverseId(OptimizedDataInputStream.readInt0(in));
        if (id < 0)
            throw new IOException("Corrupted data (negative integer)");
        if (id >= this.strings.size()) {
            if (id != this.strings.size())
                throw new IOException("Corrupted data (unexpected integer)");
            final String newString = in.readUTF();
            this.strings.add(newString);
            return newString;
        }

        return this.strings.get(id);
    }

}
