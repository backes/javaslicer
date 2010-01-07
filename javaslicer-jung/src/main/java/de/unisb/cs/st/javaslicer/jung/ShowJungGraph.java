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
import de.unisb.cs.st.javaslicer.traceResult.ThreadId;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.variables.ArrayElement;
import de.unisb.cs.st.javaslicer.variables.LocalVariable;
import de.unisb.cs.st.javaslicer.variables.ObjectField;
import de.unisb.cs.st.javaslicer.variables.StackEntry;
import de.unisb.cs.st.javaslicer.variables.StaticField;
import de.unisb.cs.st.javaslicer.variables.Variable;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class ShowJungGraph implements Opcodes {

    private final TraceResult trace;
    private final List<ProgressMonitor> progressMonitors = new ArrayList<ProgressMonitor>(1);
    private final List<SliceVisitor> sliceVisitors = new ArrayList<SliceVisitor>(1);

    public ShowJungGraph(TraceResult trace) {
        this.trace = trace;
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
            sc = SlicingCriterion.parseAll(slicingCriterionString, trace.getReadClasses());
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

        long startTime = System.nanoTime();
        ShowJungGraph showGraph = new ShowJungGraph(trace);
        if (cmdLine.hasOption("progress"))
            showGraph.addProgressMonitor(new ConsoleProgressMonitor());
        boolean multithreaded;
        if (cmdLine.hasOption("multithreaded")) {
            String multithreadedStr = cmdLine.getOptionValue("multithreaded");
            multithreaded = ("1".equals(multithreadedStr) || "true".equals(multithreadedStr));
        } else {
            multithreaded = Runtime.getRuntime().availableProcessors() > 1;
        }

        DirectedGraph<InstructionInstance, SliceEdge> graph = showGraph.getGraph(tracing, sc, multithreaded);
        long endTime = System.nanoTime();

        System.out.format((Locale)null, "%nSlice graph consists of %d nodes.%n", graph.getVertexCount());
        System.out.format((Locale)null, "Computation took %.2f seconds.%n", 1e-9*(endTime-startTime));

        showGraph.displayGraph(graph);
    }

    public void displayGraph(DirectedGraph<InstructionInstance, SliceEdge> graph) {
    	JFrame mainFrame = new JFrame("slice graph display");
    	mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    	Layout<InstructionInstance, SliceEdge> layout = new DAGLayout<InstructionInstance, SliceEdge>(graph);
		final VisualizationViewer<InstructionInstance, SliceEdge> viewer = new VisualizationViewer<InstructionInstance, SliceEdge>(layout);

		viewer.getRenderContext().setVertexLabelTransformer(new Transformer<InstructionInstance, String>() {
			public String transform(InstructionInstance inst) {
				return getShortInstanceText(inst);
			}
		});
		viewer.setVertexToolTipTransformer(new Transformer<InstructionInstance, String>() {
			public String transform(InstructionInstance inst) {
				return getInstanceTooltip(inst);
			}
		});
		viewer.getRenderContext().setEdgeLabelTransformer(new Transformer<SliceEdge, String>() {
			public String transform(SliceEdge edge) {
				Variable var = edge.getVariable();
				if (var instanceof ArrayElement) {
					ArrayElement elem = (ArrayElement) var;
					return "arr"+elem.getArrayId()+"["+elem.getArrayIndex()+"]";
				} else if (var instanceof LocalVariable<?>) {
					LocalVariable<?> localVar = (LocalVariable<?>) var;
					if (localVar.getVarName() != null)
						return localVar.getVarName();
					return "local" + localVar.getVarIndex();
				} else if (var instanceof StackEntry<?>) {
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
		viewer.setEdgeToolTipTransformer(new Transformer<SliceEdge, String>() {
			public String transform(SliceEdge edge) {
				Variable var = edge.getVariable();
				if (var instanceof ArrayElement) {
					ArrayElement elem = (ArrayElement) var;
					return "Array element " + elem.getArrayIndex() + " of array #" + elem.getArrayId();
				} else if (var instanceof LocalVariable<?>) {
					LocalVariable<?> localVar = (LocalVariable<?>) var;
					if (localVar.getVarName() != null)
						return "Local variable \"" + localVar.getVarName() + "\"";
					return "Local variable #" + localVar.getVarIndex();
				} else if (var instanceof StackEntry<?>) {
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
		viewer.getRenderContext().setEdgeDrawPaintTransformer(new Transformer<SliceEdge, Paint>() {
			public Paint transform(SliceEdge edge) {
				return edge.getVariable() == null
					? Color.red
					: Color.black;
			}
		});

		viewer.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		viewer.getRenderContext().setVertexShapeTransformer(new Transformer<InstructionInstance, Shape>() {
			public Shape transform(InstructionInstance inst) {
				Font font = viewer.getFont();
				String text = getShortInstanceText(inst);
				FontRenderContext fontRenderContext = ((Graphics2D)viewer.getGraphics()).getFontRenderContext();
				Rectangle2D bounds = font.getStringBounds(text, fontRenderContext);
				return new RoundRectangle2D.Double(-.6*bounds.getWidth(), -.6*bounds.getHeight(), 1.2*bounds.getWidth(), 1.2*bounds.getHeight(), 8, 8);
			}
		});

		viewer.setBackground(Color.white);

		DefaultModalGraphMouse<InstructionInstance, SliceEdge> mouse =
				new DefaultModalGraphMouse<InstructionInstance, SliceEdge>();
		mouse.setMode(Mode.ANNOTATING);
		viewer.setGraphMouse(mouse);

    	mainFrame.getContentPane().add(viewer);
    	mainFrame.pack();

    	mainFrame.setVisible(true);
	}

	protected String getShortInstanceText(InstructionInstance inst) {
		if (inst.getInstruction().getType() == InstructionType.METHODINVOCATION) {
			MethodInvocationInstruction mtdInv = (MethodInvocationInstruction) inst.getInstruction();
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

			return type + ' ' + getSimpleClassName(mtdInv.getInternalClassName()) + '.' + mtdInv.getMethodName();
		}
		return inst.toString();
	}

	protected String getInstanceTooltip(InstructionInstance inst) {
		Instruction insn = inst.getInstruction();
		String location = insn.getMethod().getReadClass() +
			"." + insn.getMethod() + ":" + insn.getLineNumber();

		if (insn.getType() == InstructionType.METHODINVOCATION) {
			return inst.toString() + " at " + location;
		}

		return location;
	}

	public void addProgressMonitor(ProgressMonitor progressMonitor) {
        this.progressMonitors.add(progressMonitor);
    }

    public void addSliceVisitor(SliceVisitor sliceVisitor) {
        this.sliceVisitors.add(sliceVisitor);
    }

    public DirectedGraph<InstructionInstance, SliceEdge> getGraph(ThreadId threadId, final List<SlicingCriterion> sc, boolean multithreaded) throws InterruptedException {

        CreateJungGraphSliceVisitor visitor = new CreateJungGraphSliceVisitor();

        Slicer slicer = new Slicer(this.trace);
        for (ProgressMonitor mon : this.progressMonitors)
        	slicer.addProgressMonitor(mon);
        slicer.addSliceVisitor(visitor);

        slicer.process(threadId, sc, multithreaded);
        return visitor.getGraph();
    }

    protected String getSimpleClassName(String internalClassName) {
		int lastSlashPos = internalClassName.lastIndexOf('/');
		String simpleClassName = lastSlashPos == -1 ? internalClassName : internalClassName.substring(lastSlashPos+1);
		return simpleClassName;
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
