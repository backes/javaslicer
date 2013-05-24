/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common
 *    Class:     TraceSequenceTypes
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/TraceSequenceTypes.java
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
package de.unisb.cs.st.javaslicer.common;

public interface TraceSequenceTypes {

    public static enum Type { INTEGER, LONG }

    // some constants
    public static final byte FORMAT_SEQUITUR = 1<<0;
    public static final byte FORMAT_GZIP = 1<<1;
    public static final byte FORMAT_UNCOMPRESSED = 1<<2;

    public static final byte TYPE_INTEGER = 1<<5;
    public static final byte TYPE_LONG = 1<<6;

}
