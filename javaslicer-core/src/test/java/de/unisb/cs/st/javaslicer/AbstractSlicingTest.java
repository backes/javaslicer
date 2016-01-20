/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer
 *    Class:     AbstractSlicingTest
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/AbstractSlicingTest.java
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
package de.unisb.cs.st.javaslicer;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;
import de.hammacher.util.Diff;
import de.hammacher.util.Diff.change;
import de.hammacher.util.DiffPrint;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;

public abstract class AbstractSlicingTest {

    protected static List<Instruction> getSlice(String traceResFilename, String thread, String criterion)
            throws IOException {
        File traceFile = null;
        try {
            traceFile = new File(AbstractSlicingTest.class.getResource(traceResFilename).toURI());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        return Utils.getSlice(traceFile, thread, criterion);
    }

    private static Pattern sliceEntryPattern = Pattern.compile("^([^ ]+):([0-9]+) (.*)$");

    protected void checkSlice(List<Instruction> slice, String[] expected) {
        checkSlice("", slice, expected);
    }

    protected void checkSlice(String prefix, List<Instruction> slice, String[] expected) {
        SliceEntry[] gotEntries = new SliceEntry[slice.size()];
        for (int i = 0; i < slice.size(); ++i) {
            gotEntries[i] = new SliceEntry(slice.get(i));
        }

        SliceEntryFilter filter = getSliceEntryFilter();
        if (filter != null) {
            int keptEntries = 0;
            for (int idx = 0; idx < gotEntries.length; ++idx) {
                if (!filter.keepEntry(gotEntries[idx]))
                    continue;
                if (keptEntries != idx)
                    gotEntries[keptEntries] = gotEntries[idx];
                ++keptEntries;
            }
            if (keptEntries != gotEntries.length)
                gotEntries = Arrays.copyOf(gotEntries, keptEntries);
        }

        SliceEntry[] expectedEntries = new SliceEntry[expected.length];
        for (int i = 0; i < expected.length; ++i) {
            Matcher m = sliceEntryPattern.matcher(expected[i]);
            Assert.assertTrue(prefix + "Error in expected output", m.matches());
            expectedEntries[i] = new SliceEntry(m.group(1), m.group(2), m.group(3));
        }

        Diff differ = new Diff(expectedEntries, gotEntries);
        change diff = differ.diff_2(false);
        if (diff == null)
            return;

        StringWriter output = new StringWriter();
        output.append(prefix + "Slice differs from expected slice:").append(System.getProperty("line.separator"));

        if (expectedEntries.length != gotEntries.length) {
            output.append("Expected " + expectedEntries.length + " entries, got " + gotEntries.length + "." +
                    System.getProperty("line.separator"));
        }

        DiffPrint.SimplestPrint diffPrinter = new DiffPrint.SimplestPrint(expectedEntries, gotEntries);
        diffPrinter.setOutput(output);
        diffPrinter.print_script(diff);

        Assert.fail(output.toString());
    }

    // can be overwritten by subclasses
    protected SliceEntryFilter getSliceEntryFilter() {
        return null;
    }
}
