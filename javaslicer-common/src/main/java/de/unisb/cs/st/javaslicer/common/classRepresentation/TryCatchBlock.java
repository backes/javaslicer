package de.unisb.cs.st.javaslicer.common.classRepresentation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.hammacher.util.OptimizedDataInputStream;
import de.hammacher.util.OptimizedDataOutputStream;
import de.hammacher.util.StringCacheInput;
import de.hammacher.util.StringCacheOutput;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod.MethodReadInformation;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;


public class TryCatchBlock {

    private final LabelMarker start, end, handler;
    private final String type;

    public TryCatchBlock(LabelMarker start, LabelMarker end,
            LabelMarker handler, String type) {
        super();
        this.start = start;
        this.end = end;
        this.handler = handler;
        this.type = type;
    }

    public LabelMarker getStart() {
        return this.start;
    }

    public LabelMarker getEnd() {
        return this.end;
    }

    public LabelMarker getHandler() {
        return this.handler;
    }

    public String getType() {
        return this.type;
    }

    public void writeOut(DataOutputStream out, StringCacheOutput stringCache) throws IOException {
        OptimizedDataOutputStream.writeInt0(this.start.getLabelNr(), out);
        OptimizedDataOutputStream.writeInt0(this.end.getLabelNr(), out);
        OptimizedDataOutputStream.writeInt0(this.handler.getLabelNr(), out);
        out.writeBoolean(this.type != null);
        if (this.type != null)
            stringCache.writeString(this.type, out);
    }

    public static TryCatchBlock readFrom(DataInputStream in,
            MethodReadInformation mri, StringCacheInput stringCache) throws IOException {
        return new TryCatchBlock(
            mri.getLabel(OptimizedDataInputStream.readInt0(in)),
            mri.getLabel(OptimizedDataInputStream.readInt0(in)),
            mri.getLabel(OptimizedDataInputStream.readInt0(in)),
            in.readBoolean() ? stringCache.readString(in) : null
            );
    }


}
