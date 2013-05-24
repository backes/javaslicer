/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation
 *    Class:     Field
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/Field.java
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


public class Field {

    private final String name;
    private final String desc;
    private final int access;

    private final ReadClass readClass;

    public Field(String name, String desc, int access, ReadClass readClass) {
        this.name = name;
        this.desc = desc;
        this.access = access;
        this.readClass = readClass;
    }

    public void writeOut(DataOutputStream out, StringCacheOutput stringCache) throws IOException {
        stringCache.writeString(this.name, out);
        stringCache.writeString(this.desc, out);
        OptimizedDataOutputStream.writeInt0(this.access, out);
    }

    public static Field readFrom(DataInputStream in, StringCacheInput stringCache, ReadClass readClass)
            throws IOException {
        String name = stringCache.readString(in);
        String desc = stringCache.readString(in);
        int access = OptimizedDataInputStream.readInt0(in);
        return new Field(name, desc, access, readClass);
    }

    public String getName() {
        return this.name;
    }

    public String getDesc() {
        return this.desc;
    }

    public int getAccess() {
        return this.access;
    }

    public ReadClass getReadClass() {
        return this.readClass;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result
                        + ((this.readClass == null) ? 0 : this.readClass.hashCode());
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
        Field other = (Field) obj;
        if (this.access != other.access)
            return false;
        if (!this.desc.equals(other.desc))
            return false;
        if (!this.name.equals(other.name))
            return false;
        if (this.readClass != other.readClass)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return this.readClass.getName() + "." + this.name;
    }

}
