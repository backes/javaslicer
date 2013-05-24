/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.controlflowanalysis
 *    Class:     CFG2Dot
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/controlflowanalysis/CFG2Dot.java
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
package de.unisb.cs.st.javaslicer.controlflowanalysis;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.hammacher.util.graph.Graph2Dot;
import de.hammacher.util.graph.NodeLabelProvider;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowGraph.AbstractNodeFactory;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowGraph.InstrNode;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;

public class CFG2Dot {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Illegal argument count: Usage is \"" + CFG2Dot.class.getSimpleName() +
			    " <trace file> <method name pattern> [<dot export filename> [<\"1\" to include throw-catch-edges>]]\"");
			System.exit(1);
		}

		String traceFileName = args[0];
        Pattern mtdNamePattern = null;
        try {
            mtdNamePattern = Pattern.compile(args[1]);
        } catch (PatternSyntaxException e) {
            System.err.println("Cannot parse the method name pattern: " + e);
            System.exit(1);
            // to satisfy some static checks:
            return;
        }
        String dotExport = (args.length < 3) ? "cfg.dot" : args[2];
        boolean includeCatchEdges = (args.length < 4) ? false : "1".equals(args[3]);

		File traceFile = new File(traceFileName);

		try {
			TraceResult traceResult = TraceResult.readFrom(traceFile);

			for (ReadClass clazz: traceResult.getReadClasses()) {
				for (ReadMethod mtd: clazz.getMethods()) {
					String completeName = clazz.getName()+"."+mtd.getName()+mtd.getDesc();
					if (mtdNamePattern.matcher(completeName).matches()) {
						System.out.format("Exporting CFG of %s to %s...%n", completeName, dotExport);
						ControlFlowGraph cfg = new ControlFlowGraph(mtd, new AbstractNodeFactory(), includeCatchEdges, true);
						Graph2Dot<InstrNode> exporter = new Graph2Dot<InstrNode>();
	                    exporter.setGraphName("cfg");
	                    exporter.setNodeShape("box");
	                    exporter.setGraphAttribute("rankdir", "TB");
	                    exporter.setNodeLabelProvider(new NodeLabelProvider<InstrNode>() {
							@Override
							public String getNodeLabel(InstrNode node) {
								return node.getInstruction().toString() + "\n" + node.getInstruction().getLineNumber();
							}
						});
						exporter.export(cfg, new File(dotExport));
						return;
					}
				}
			}

			System.out.println("No method matches the given pattern!");

		} catch (IOException e) {
			System.err.println("Error while reading file. Message is: " + e.getMessage());
			System.exit(1);
		}
	}

}
