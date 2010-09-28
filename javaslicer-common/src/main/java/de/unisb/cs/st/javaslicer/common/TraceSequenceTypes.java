/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common
 *    Class:     TraceSequenceTypes
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/TraceSequenceTypes.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
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
