/** License information:
 *    Component: javaslicer-jung
 *    Package:   de.unisb.cs.st.javaslicer.jung
 *    Class:     ShowJungGraph
 *    Filename:  javaslicer-jung/src/main/java/de/unisb/cs/st/javaslicer/jung/ShowJungGraph.java
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
package de.unisb.cs.st.javaslicer.jung;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JFrame;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections15.Transformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MethodInvocationInstruction;
import de.unisb.cs.st.javaslicer.common.progress.ConsoleProgressMonitor;
import de.unisb.cs.st.javaslicer.common.progress.ProgressMonitor;
import de.unisb.cs.st.javaslicer.slicing.SliceVisitor;
import de.unisb.cs.st.javaslicer.slicing.Slicer;
import de.unisb.cs.st.javaslicer.slicing.SlicingCriterion;
import de.unisb.cs.st.javaslicer.slicing.StaticSlicingCriterion;
import de.unisb.cs.st.javaslicer.traceResult.ThreadId;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.variables.ArrayElement;
import de.unisb.cs.st.javaslicer.variables.LocalVariable;
import de.unisb.cs.st.javaslicer.variables.ObjectField;
import de.unisb.cs.st.javaslicer.variables.StackEntry;
import de.unisb.cs.st.javaslicer.variables.StaticField;
import de.unisb.cs.st.javaslicer.variables.Variable;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/**
 * This class implements a main method which creates a graph showing the dependences
 * leading to the dynamic slice, and visualizes it.
 *
 * @author Clemens Hammacher
 *
 * @param <VertexType> the type of the vertices in the graph
 */
public class ShowJungGraph<VertexType> {

	private final TraceResult trace;
    private final List<ProgressMonitor> progressMonitors = new ArrayList<ProgressMonitor>(1);
    private final List<SliceVisitor> sliceVisitors = new ArrayList<SliceVisitor>(1);

    private Transformer<InstructionInstance, VertexType> vertexTransformer;
	private Transformer<VertexType, String> vertexLabelTransformer;
	private Transformer<VertexType, String> vertexTooltipTransformer;
	private int maxLevel;

    public ShowJungGraph(TraceResult trace, Transformer<InstructionInstance, VertexType> vertexTransformer) {
        this.trace = trace;
        this.vertexTransformer = vertexTransformer;
    }

	public static void main(String[] args) throws InterruptedException {
        Options options = createOptions();
        CommandLineParser parser = new GnuParser();
        CommandLine cmdLine;

        try {
            cmdLine = parser.parse(options, args, true);
        } catch (ParseException e) {
            System.err.println("Error parsing the command line arguments: " + e.getMessage());
            return;
        }

        if (cmdLine.hasOption('h')) {
            printHelp(options, System.out);
            System.exit(0);
        }

        String[] additionalArgs = cmdLine.getArgs();
        if (additionalArgs.length != 2) {
            printHelp(options, System.err);
            System.exit(-1);
        }
        File traceFile = new File(additionalArgs[0]);
        String slicingCriterionString = additionalArgs[1];

        Long threadId = null;
        if (cmdLine.hasOption('t')) {
            try {
                threadId = Long.parseLong(cmdLine.getOptionValue('t'));
            } catch (NumberFormatException e) {
                System.err.println("Illegal thread id: " + cmdLine.getOptionValue('t'));
                System.exit(-1);
            }
        }

        TraceResult trace;
        try {
            trace = TraceResult.readFrom(traceFile);
        } catch (IOException e) {
            System.err.format("Could not read the trace file \"%s\": %s%n", traceFile, e);
            System.exit(-1);
            return;
        }

        List<SlicingCriterion> sc = null;
        try {
            sc = StaticSlicingCriterion.parseAll(slicingCriterionString, trace.getReadClasses());
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing slicing criterion: " + e.getMessage());
            System.exit(-1);
            return;
        }

        List<ThreadId> threads = trace.getThreads();
        if (threads.size() == 0) {
            System.err.println("The trace file contains no tracing information.");
            System.exit(-1);
        }

        ThreadId tracing = null;
        for (ThreadId t: threads) {
            if (threadId == null) {
                if ("main".equals(t.getThreadName()) && (tracing == null || t.getJavaThreadId() < tracing.getJavaThreadId()))
                    tracing = t;
            } else if (t.getJavaThreadId() == threadId.longValue()) {
                tracing = t;
            }
        }

        if (tracing == null) {
            System.err.println(threadId == null ? "Couldn't find the main thread."
                    : "The thread you specified was not found.");
            System.exit(-1);
            return;
        }

        Transformer<InstructionInstance, Object> transformer;
    	Transformer<Object, String> vertexLabelTransformer;
    	Transformer<Object, String> vertexTooltipTransformer;

		String granularity = cmdLine.getOptionValue("granularity");
		if (granularity == null || "instance".equals(granularity)) {
			transformer = new Transformer<InstructionInstance, Object>() {
				@Override
				public InstructionInstance transform(InstructionInstance inst) {
					return inst;
				}
			};
			vertexLabelTransformer = new Transformer<Object, String>() {
				@Override
				public String transform(Object inst) {
					return getShortInstructionText(((InstructionInstance) inst).getInstruction());
				}
			};
			vertexTooltipTransformer = new Transformer<Object, String>() {
				@Override
				public String transform(Object inst) {
					return getInstructionTooltip(((InstructionInstance) inst).getInstruction());
				}
			};
		} else if ("instruction".equals(granularity)) {
			transformer = new Transformer<InstructionInstance, Object>() {
				@Override
				public Instruction transform(InstructionInstance inst) {
					return inst.getInstruction();
				}
			};
			vertexLabelTransformer = new Transformer<Object, String>() {
				@Override
				public String transform(Object inst) {
					return getShortInstructionText(((Instruction) inst));
				}
			};
			vertexTooltipTransformer = new Transformer<Object, String>() {
				@Override
				public String transform(Object inst) {
					return getInstructionTooltip(((Instruction) inst));
				}
			};
		} else if ("line".equals(granularity)) {
			transformer = new Transformer<InstructionInstance, Object>() {
				@Override
				public Line transform(InstructionInstance inst) {
					return new Line(inst.getInstruction().getMethod(), inst.getInstruction().getLineNumber());
				}
			};
			vertexLabelTransformer = new Transformer<Object, String>() {
				@Override
				public String transform(Object inst) {
					Line line = (Line) inst;
					return line.getMethod().getName() + ":" + line.getLineNr();
				}
			};
			vertexTooltipTransformer = new Transformer<Object, String>() {
				@Override
				public String transform(Object inst) {
					Line line = (Line) inst;
					return "Line " + line.getLineNr() + " in method " + line.getMethod().getReadClass().getName() + "." + line.getMethod();
				}
			};
		} else {
			System.err.println("Illegal granularity specification: " + granularity);
			System.exit(-1);
			return;
		}

		int maxLevel = Integer.MAX_VALUE;
		if (cmdLine.hasOption("maxlevel")) {
			try {
				maxLevel = Integer.parseInt(cmdLine.getOptionValue("maxlevel"));
			} catch (NumberFormatException e) {
				System.err.println("Argument to \"maxlevel\" must be an integer.");
				System.exit(-1);
				return;
			}
		}

        long startTime = System.nanoTime();
        ShowJungGraph<Object> showGraph = new ShowJungGraph<Object>(trace, transformer);
        showGraph.setMaxLevel(maxLevel);
        showGraph.setVertexLabelTransformer(vertexLabelTransformer);
        showGraph.setVertexTooltipTransformer(vertexTooltipTransformer);
        if (cmdLine.hasOption("progress"))
            showGraph.addProgressMonitor(new ConsoleProgressMonitor());
        boolean multithreaded;
        if (cmdLine.hasOption("multithreaded")) {
            String multithreadedStr = cmdLine.getOptionValue("multithreaded");
            multithreaded = ("1".equals(multithreadedStr) || "true".equals(multithreadedStr));
        } else {
            multithreaded = Runtime.getRuntime().availableProcessors() > 1;
        }

        DirectedGraph<Object, SliceEdge<Object>> graph = showGraph.getGraph(tracing, sc, multithreaded);
        long endTime = System.nanoTime();

        System.out.format((Locale)null, "%nSlice graph consists of %d nodes.%n", graph.getVertexCount());
        System.out.format((Locale)null, "Computation took %.2f seconds.%n", 1e-9*(endTime-startTime));

        showGraph.displayGraph(graph);
    }

    private void setMaxLevel(int maxLevel) {
    	this.maxLevel = maxLevel;
	}

	public void displayGraph(DirectedGraph<VertexType, SliceEdge<VertexType>> graph) {
    	JFrame mainFrame = new JFrame("slice graph display");
    	mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    	Layout<VertexType, SliceEdge<VertexType>> layout = new KKLayout<VertexType, SliceEdge<VertexType>>(graph);
		final VisualizationViewer<VertexType, SliceEdge<VertexType>> viewer = new VisualizationViewer<VertexType, SliceEdge<VertexType>>(layout);

		viewer.getRenderContext().setVertexLabelTransformer(this.vertexLabelTransformer);
		viewer.setVertexToolTipTransformer(this.vertexTooltipTransformer);
		viewer.getRenderContext().setEdgeLabelTransformer(new Transformer<SliceEdge<VertexType>, String>() {
			@Override
			public String transform(SliceEdge<VertexType> edge) {
				Variable var = edge.getVariable();
				if (var instanceof ArrayElement) {
					ArrayElement elem = (ArrayElement) var;
					return "arr"+elem.getArrayId()+"["+elem.getArrayIndex()+"]";
				} else if (var instanceof LocalVariable) {
					LocalVariable localVar = (LocalVariable) var;
					if (localVar.getVarName() != null)
						return localVar.getVarName();
					return "local" + localVar.getVarIndex();
				} else if (var instanceof StackEntry) {
					return "stack";
				} else if (var instanceof ObjectField) {
					ObjectField objField = (ObjectField) var;
					return "obj" + objField.getObjectId() + "." + objField.getFieldName();
				} else if (var instanceof StaticField) {
					StaticField objField = (StaticField) var;
					String ownerInternalClassName = objField.getOwnerInternalClassName();
					String simpleClassName =
							getSimpleClassName(ownerInternalClassName);
					return simpleClassName + "." + objField.getFieldName();
				} else {
					assert var == null;
					return "";
				}
			}
		});
		viewer.setEdgeToolTipTransformer(new Transformer<SliceEdge<VertexType>, String>() {
			@Override
			public String transform(SliceEdge<VertexType> edge) {
				Variable var = edge.getVariable();
				if (var instanceof ArrayElement) {
					ArrayElement elem = (ArrayElement) var;
					return "Array element " + elem.getArrayIndex() + " of array #" + elem.getArrayId();
				} else if (var instanceof LocalVariable) {
					LocalVariable localVar = (LocalVariable) var;
					if (localVar.getVarName() != null)
						return "Local variable \"" + localVar.getVarName() + "\"";
					return "Local variable #" + localVar.getVarIndex();
				} else if (var instanceof StackEntry) {
					return "Dependency over the operand stack";
				} else if (var instanceof ObjectField) {
					ObjectField objField = (ObjectField) var;
					return "Field \"" + objField.getFieldName() + "\" of object #" + objField.getObjectId();
				} else if (var instanceof StaticField) {
					StaticField objField = (StaticField) var;
					String ownerInternalClassName = objField.getOwnerInternalClassName();
					return "Static field \"" + objField.getFieldName() + "\" of class \"" + Type.getType(ownerInternalClassName).getClassName();
				} else {
					assert var == null;
					return "Control dependency";
				}
			}
		});
		viewer.getRenderContext().setEdgeDrawPaintTransformer(new Transformer<SliceEdge<VertexType>, Paint>() {
			@Override
			public Paint transform(SliceEdge<VertexType> edge) {
				return edge.getVariable() == null
					? Color.red
					: Color.black;
			}
		});

		viewer.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		viewer.getRenderContext().setVertexShapeTransformer(new Transformer<VertexType, Shape>() {
			@Override
			public Shape transform(VertexType inst) {
				Font font = viewer.getFont();
				String text = viewer.getRenderContext().getVertexLabelTransformer().transform(inst);
				FontRenderContext fontRenderContext = ((Graphics2D)viewer.getGraphics()).getFontRenderContext();
				Rectangle2D bounds = font.getStringBounds(text, fontRenderContext);
				return new RoundRectangle2D.Double(-.6*bounds.getWidth(), -.6*bounds.getHeight(), 1.2*bounds.getWidth(), 1.2*bounds.getHeight(), 8, 8);
			}
		});

		viewer.setBackground(Color.white);

		DefaultModalGraphMouse<InstructionInstance, SliceEdge<VertexType>> mouse =
				new DefaultModalGraphMouse<InstructionInstance, SliceEdge<VertexType>>();
		mouse.setMode(Mode.ANNOTATING);
		viewer.setGraphMouse(mouse);

    	mainFrame.getContentPane().add(viewer);
    	mainFrame.pack();

    	mainFrame.setVisible(true);
	}

	protected static String getShortInstructionText(Instruction inst) {
		if (inst.getType() == InstructionType.METHODINVOCATION) {
			MethodInvocationInstruction mtdInv = (MethodInvocationInstruction) inst;
	        String type;
			switch (mtdInv.getOpcode()) {
	        case Opcodes.INVOKEVIRTUAL:
	            type = "INVOKEVIRTUAL";
	            break;
	        case Opcodes.INVOKESPECIAL:
	            type = "INVOKESPECIAL";
	            break;
	        case Opcodes.INVOKESTATIC:
	            type = "INVOKESTATIC";
	            break;
	        case Opcodes.INVOKEINTERFACE:
	            type = "INVOKEINTERFACE";
	            break;
	        default:
	            assert false;
	            type = "--ERROR--";
	        }

			return type + ' ' + getSimpleClassName(mtdInv.getInvokedInternalClassName()) + '.' + mtdInv.getInvokedMethodName();
		}
		return inst.toString();
	}

	protected static String getInstructionTooltip(Instruction insn) {
		String location = insn.getMethod().getReadClass() +
			"." + insn.getMethod() + ":" + insn.getLineNumber();

		if (insn.getType() == InstructionType.METHODINVOCATION) {
			return insn.toString() + " at " + location;
		}

		return location;
	}

	public void addProgressMonitor(ProgressMonitor progressMonitor) {
        this.progressMonitors.add(progressMonitor);
    }

    public void addSliceVisitor(SliceVisitor sliceVisitor) {
        this.sliceVisitors.add(sliceVisitor);
    }

    public DirectedGraph<VertexType, SliceEdge<VertexType>> getGraph(ThreadId threadId,
    		List<SlicingCriterion> sc, boolean multithreaded) throws InterruptedException {

        CreateJungGraphSliceVisitor<VertexType> visitor = new CreateJungGraphSliceVisitor<VertexType>(this.vertexTransformer, this.maxLevel);

        Slicer slicer = new Slicer(this.trace);
        for (ProgressMonitor mon : this.progressMonitors)
        	slicer.addProgressMonitor(mon);
        slicer.addSliceVisitor(visitor);

        slicer.process(threadId, sc, multithreaded);
        return visitor.getGraph();
    }

    protected static String getSimpleClassName(String internalClassName) {
		int lastSlashPos = internalClassName.lastIndexOf('/');
		String simpleClassName = lastSlashPos == -1 ? internalClassName : internalClassName.substring(lastSlashPos+1);
		return simpleClassName;
	}

	public void setVertexLabelTransformer(
			Transformer<VertexType, String> vertexLabelTransformer) {
		this.vertexLabelTransformer = vertexLabelTransformer;
	}

	public void setVertexTooltipTransformer(
			Transformer<VertexType, String> vertexTooltipTransformer) {
		this.vertexTooltipTransformer = vertexTooltipTransformer;
	}

	public void setVertexTransformer(
			Transformer<InstructionInstance, VertexType> vertexTransformer) {
		this.vertexTransformer = vertexTransformer;
	}

	@SuppressWarnings("static-access")
    private static Options createOptions() {
        Options options = new Options();
        options.addOption(OptionBuilder.isRequired(false).withArgName("threadid").hasArg(true).
            withDescription("thread id to select for slicing (default: main thread)").withLongOpt("threadid").create('t'));
        options.addOption(OptionBuilder.isRequired(false).hasArg(false).
            withDescription("show progress while computing the slice graph").withLongOpt("progress").create('p'));
        options.addOption(OptionBuilder.isRequired(false).hasArg(false).
            withDescription("print this help and exit").withLongOpt("help").create('h'));
        options.addOption(OptionBuilder.isRequired(false).hasArg(true).withArgName("value").
            withDescription("process the trace in a multithreaded way (pass 'true' or '1' to enable, anything else to disable). Default is true iff we have more than one processor").
            withLongOpt("multithreaded").create('m'));
        options.addOption(OptionBuilder.isRequired(false).hasArg(true).withArgName("instance|instruction|line").
            withDescription("defines the granularity of the created graph. default is instance").
            withLongOpt("granularity").create('g'));
        options.addOption(OptionBuilder.isRequired(false).hasArg(true).withArgName("int").
            withDescription("defines the maximum distance from the slicing criterion to visualize").
            withLongOpt("maxlevel").create('l'));
        return options;
    }

    private static void printHelp(Options options, PrintStream out) {
        out.println("Usage: " + ShowJungGraph.class.getSimpleName() + " [<options>] <file> <slicing criterion>");
        out.println("where <file> is the input trace file");
        out.println("      <slicing criterion> has the form <loc>[(<occ>)]:<var>[,<loc>[(<occ>)]:<var>]*");
        out.println("      <options> may be one or more of");
        HelpFormatter formatter = new HelpFormatter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printOptions(pw, 120, options, 5, 3);
    }

}
