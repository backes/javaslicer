/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation
 *    Class:     TryCatchBlock
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/TryCatchBlock.java
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
package de.unisb.cs.st.javaslicer.common.classRepresentation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.hammacher.util.StringCacheInput;
import de.hammacher.util.StringCacheOutput;
import de.hammacher.util.streams.OptimizedDataInputStream;
import de.hammacher.util.streams.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod.MethodReadInformation;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;


public class TryCatchBlock {

    private final LabelMarker start, end, handler;
    private final String type;

    public TryCatchBlock(LabelMarker start, LabelMarker end,
            LabelMarker handler, String type) {
        assert start != null && end != null && handler != null;
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
        stringCache.writeString(this.type, out);
    }

    public static TryCatchBlock readFrom(DataInputStream in,
            MethodReadInformation mri, StringCacheInput stringCache) throws IOException {
        LabelMarker start = mri.getLabel(OptimizedDataInputStream.readInt0(in));
        LabelMarker end = mri.getLabel(OptimizedDataInputStream.readInt0(in));
        LabelMarker handler = mri.getLabel(OptimizedDataInputStream.readInt0(in));
        String type = stringCache.readString(in);
        return new TryCatchBlock(start, end, handler, type);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.start.hashCode();
        result = prime * result + this.end.hashCode();
        result = prime * result + this.handler.hashCode();
        result = prime * result + (this.type == null ? 0 : this.type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TryCatchBlock other = (TryCatchBlock) obj;
        if (!this.start.equals(other.start))
            return false;
        if (!this.end.equals(other.end))
            return false;
        if (!this.handler.equals(other.handler))
            return false;
        if (this.type == null) {
            if (other.type != null)
                return false;
        } else if (!this.type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        String sStart = this.start.toString();
        String sEnd = this.end.toString();
        String sHandler = this.handler.toString();
        String sType = this.type == null ? "*" : this.type.toString();
        StringBuilder sb = new StringBuilder(sStart.length() + sEnd.length() + sHandler.length() + sType.length() + 8);
        return sb.append(sStart).append(" - ").append(sEnd).append(": ").append(sHandler).
            append(" (").append(sType).append(')').toString();
    }

}
