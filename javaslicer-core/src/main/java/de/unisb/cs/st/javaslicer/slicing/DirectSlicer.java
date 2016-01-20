/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     DirectSlicer
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/slicing/DirectSlicer.java
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
package de.unisb.cs.st.javaslicer.slicing;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.objectweb.asm.Opcodes;

import de.hammacher.util.maps.IntegerMap;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;
import de.unisb.cs.st.javaslicer.common.progress.ConsoleProgressMonitor;
import de.unisb.cs.st.javaslicer.common.progress.ProgressMonitor;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowAnalyser;
import de.unisb.cs.st.javaslicer.instructionSimulation.DynamicInformation;
import de.unisb.cs.st.javaslicer.instructionSimulation.SimulationEnvironment;
import de.unisb.cs.st.javaslicer.instructionSimulation.Simulator;
import de.unisb.cs.st.javaslicer.traceResult.BackwardTraceIterator;
import de.unisb.cs.st.javaslicer.traceResult.ThreadId;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.variables.LocalVariable;
import de.unisb.cs.st.javaslicer.variables.StackEntry;
import de.unisb.cs.st.javaslicer.variables.Variable;

/**
 * This is the first dynamic slicer I wrote.
 * It is much faster than the new version which uses the more general
 * dependences extractor.
 *
 * I try to maintain both versions, also to verify each other's results.
 *
 * You can use this version if you are just interested in the dynamic
 * slice as a set of bytecode instructions.
 *
 * @author Clemens Hammacher
 */
public class DirectSlicer implements Opcodes {

    private final TraceResult trace;
    private final Simulator<InstructionInstance> simulator;
    private final List<ProgressMonitor> progressMonitors = new ArrayList<ProgressMonitor>(1);

    public DirectSlicer(TraceResult trace) {
        this.trace = trace;
        this.simulator = new Simulator<InstructionInstance>(trace);
    }

    public static void main(String[] args) {
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

        long startTime = System.nanoTime();
        DirectSlicer slicer = new DirectSlicer(trace);
        if (cmdLine.hasOption("--progress"))
            slicer.addProgressMonitor(new ConsoleProgressMonitor());
        Set<Instruction> slice = slicer.getDynamicSlice(tracing, sc);
        long endTime = System.nanoTime();

        List<Instruction> sliceList = new ArrayList<Instruction>(slice);
        Collections.sort(sliceList);

        System.out.println("The dynamic slice for criterion " + sc + ":");
        for (Instruction insn: sliceList) {
            System.out.format((Locale)null, "%s.%s:%d %s%n",
                    insn.getMethod().getReadClass().getName(),
                    insn.getMethod().getName(),
                    insn.getLineNumber(),
                    insn.toString());
        }
        System.out.format((Locale)null, "%nSlice consists of %d bytecode instructions.%n", sliceList.size());
        System.out.format((Locale)null, "Computation took %.2f seconds.%n", 1e-9*(endTime-startTime));
    }

    private void addProgressMonitor(ProgressMonitor progressMonitor) {
        this.progressMonitors.add(progressMonitor);
    }

    public Set<Instruction> getDynamicSlice(ThreadId threadId, List<SlicingCriterion> sc) {
        BackwardTraceIterator<InstructionInstance> backwardInsnItr = this.trace.getBackwardIterator(threadId, null);

        IntegerMap<Set<Instruction>> controlDependences = new IntegerMap<Set<Instruction>>();

        Set<Variable> interestingVariables = new HashSet<Variable>();
        Set<Instruction> dynamicSlice = new HashSet<Instruction>();

        long nextFrameNr = 0;
        int stackDepth = 0;

        List<ReadMethod> initialStackMethods = backwardInsnItr.getInitialStackMethods();

        int allocStack = initialStackMethods.size() + 1;
        allocStack = Integer.highestOneBit(allocStack)*2;

        Instruction[] atCatchBlockStart = new Instruction[allocStack];
        boolean[] throwsException = new boolean[allocStack];
        boolean[] interruptedControlFlow = new boolean[allocStack];
        /**
         * <code>true</code> iff this frame was aborted abnormally (NOT by a RETURN
         * instruction)
         */
        boolean[] abnormalTermination = new boolean[allocStack];
        /**
         * is set to true if the methods entry label has been passed
         */
        boolean[] finished = new boolean[allocStack];
        int[] opStack = new int[allocStack];
        int[] minOpStack = new int[allocStack];
        long[] frames = new long[allocStack];
        Instruction[] lastInstruction = new Instruction[allocStack];
        @SuppressWarnings("unchecked")
        HashSet<Instruction>[] interestingInstructions = (HashSet<Instruction>[]) new HashSet<?>[allocStack];
        StackEntry[][] cachedStackEntries = new StackEntry[allocStack][];
        LocalVariable[][] cachedLocalVariables = new LocalVariable[allocStack][];
        ReadMethod[] method = new ReadMethod[allocStack];

		for (ReadMethod method0: initialStackMethods) {
        	++stackDepth;
        	method[stackDepth] = method0;
        	interruptedControlFlow[stackDepth] = true;
        	frames[stackDepth] = nextFrameNr++;
        }

		for (int i = 1; i < allocStack; ++i) {
        	interestingInstructions[i] = new HashSet<Instruction>();
        	cachedStackEntries[i] = new StackEntry[8];
        	cachedLocalVariables[i] = new LocalVariable[8];
		}

		SimulationEnvironment simEnv = new SimulationEnvironment(frames, opStack, minOpStack,
			cachedStackEntries, cachedLocalVariables, throwsException, lastInstruction, method, interruptedControlFlow);

        List<SlicingCriterionInstance> slicingCriteria;
        if (sc.isEmpty())
            slicingCriteria = Collections.emptyList();
        else if (sc.size() == 1)
            slicingCriteria = Collections.singletonList(sc.get(0).getInstance());
        else {
            slicingCriteria = new ArrayList<SlicingCriterionInstance>(sc.size());
            for (SlicingCriterion crit : sc)
                slicingCriteria.add(crit.getInstance());
        }

        for (ProgressMonitor mon : this.progressMonitors)
            mon.start(backwardInsnItr);
        try {

            while (backwardInsnItr.hasNext()) {
                InstructionInstance instance = backwardInsnItr.next();
                Instruction instruction = instance.getInstruction();

                int newStackDepth = instance.getStackDepth();
                assert newStackDepth > 0;

                simEnv.removedMethod = null;
                boolean reenter = false;
                if (newStackDepth != stackDepth || (reenter = finished[stackDepth] || method[stackDepth] != instruction.getMethod())) {
                    if (newStackDepth >= stackDepth) {
                        // in all steps, the stackDepth can change by at most 1 (except for the very first instruction)
                        assert newStackDepth == stackDepth+1 || stackDepth == 0 || reenter;
                        if (newStackDepth >= atCatchBlockStart.length) {
                        	int oldLen = atCatchBlockStart.length;
                        	int newLen = oldLen == 0 ? 8 : 2*oldLen;
                            atCatchBlockStart = Arrays.copyOf(atCatchBlockStart, newLen);
                            throwsException = Arrays.copyOf(throwsException, newLen);
                            interruptedControlFlow = Arrays.copyOf(interruptedControlFlow, newLen);
                            abnormalTermination = Arrays.copyOf(abnormalTermination, newLen);
                            finished = Arrays.copyOf(finished, newLen);
                            opStack = Arrays.copyOf(opStack, newLen);
                            minOpStack = Arrays.copyOf(minOpStack, newLen);
                            frames = Arrays.copyOf(frames, newLen);
                            interestingInstructions = Arrays.copyOf(interestingInstructions, newLen);
                            lastInstruction = Arrays.copyOf(lastInstruction, newLen);
                            cachedStackEntries = Arrays.copyOf(cachedStackEntries, newLen);
                            cachedLocalVariables = Arrays.copyOf(cachedLocalVariables, newLen);
                            method = Arrays.copyOf(method, newLen);
                            for (int i = oldLen; i < newLen; ++i) {
                            	interestingInstructions[i] = new HashSet<Instruction>();
                            	cachedStackEntries[i] = new StackEntry[8];
                            	cachedLocalVariables[i] = new LocalVariable[8];
                            }
                            simEnv.reallocate(frames, opStack, minOpStack,
                                cachedStackEntries, cachedLocalVariables,
                                throwsException, lastInstruction, method,
                                interruptedControlFlow);
                        }
                        frames[newStackDepth] = nextFrameNr++;
                        method[newStackDepth] = instruction.getMethod();

                        atCatchBlockStart[newStackDepth] = null;
                        if (instruction == method[newStackDepth].getAbnormalTerminationLabel()) {
                            throwsException[newStackDepth] = interruptedControlFlow[newStackDepth] = abnormalTermination[newStackDepth] = true;
                            interruptedControlFlow[stackDepth] = true;
                        } else {
                        	throwsException[newStackDepth] = interruptedControlFlow[newStackDepth] = abnormalTermination[newStackDepth] = false;
                        }
                        finished[newStackDepth] = false;
                        opStack[newStackDepth] = 0;
                        minOpStack[newStackDepth] = 0;
                        interestingInstructions[newStackDepth].clear();
                        if (cachedLocalVariables[newStackDepth].length > 128)
                        	cachedLocalVariables[newStackDepth] = new LocalVariable[8];
                        else
                        	Arrays.fill(cachedLocalVariables[newStackDepth], null);
                        if (cachedStackEntries[newStackDepth].length > 128)
                        	cachedStackEntries[newStackDepth] = new StackEntry[8];
                        else
                        	Arrays.fill(cachedStackEntries[newStackDepth], null);
                    } else {
                        assert newStackDepth == stackDepth-1;
                        simEnv.removedMethod = method[stackDepth];
                    }
                }
                stackDepth = newStackDepth;

                if (atCatchBlockStart[stackDepth] != null)
                	throwsException[stackDepth] = true;

                if (instruction == instruction.getMethod().getMethodEntryLabel())
                    finished[stackDepth] = true;
                lastInstruction[stackDepth] = instruction;

                DynamicInformation dynInfo = this.simulator.simulateInstruction(instance, simEnv);

                if (simEnv.removedMethod != null &&
                        !interestingInstructions[stackDepth+1].isEmpty()) {
                    // ok, we have a control dependence since the method was called by (or for) this instruction
                    // checking if this is the instr. that directly called the method is impossible
                    dynamicSlice.add(instruction);
                    interestingInstructions[stackDepth].add(instruction);
                }

                for (SlicingCriterionInstance crit : slicingCriteria) {
                    if (crit.matches(instance)) {
                        if (crit.computeTransitiveClosure()) {
                            interestingVariables.addAll(dynInfo.getUsedVariables());
                            dynamicSlice.add(instruction);
                            interestingInstructions[stackDepth].add(instance.getInstruction());
                        } else if (crit.hasLocalVariables()) {
                            for (de.unisb.cs.st.javaslicer.common.classRepresentation.LocalVariable var : crit.getLocalVariables())
                                interestingVariables.add(simEnv.getLocalVariable(stackDepth, var.getIndex()));
                        } else {
                            interestingInstructions[stackDepth].add(instance.getInstruction());
                        }
                    }
                }

                boolean isExceptionsThrowingInstance = throwsException[stackDepth] &&
                    (instruction.getType() != InstructionType.LABEL || !((LabelMarker)instruction).isAdditionalLabel()) &&
                    (instruction.getOpcode() != Opcodes.GOTO);
                if (!interestingInstructions[stackDepth].isEmpty() || isExceptionsThrowingInstance) {
                    Set<Instruction> instrControlDependences = controlDependences.get(instruction.getIndex());
                    if (instrControlDependences == null) {
                        computeControlDependences(instruction.getMethod(), controlDependences);
                        instrControlDependences = controlDependences.get(instruction.getIndex());
                        assert instrControlDependences != null;
                    }
                    // get all interesting instructions, that are dependent on the current one
                    Set<Instruction> dependantInterestingInstructions = intersect(instrControlDependences,
                    	interestingInstructions[stackDepth]);
                    if (isExceptionsThrowingInstance) {
                        throwsException[stackDepth] = false;
                        // in this case, we have an additional control dependence from the catching to
                        // the throwing instruction, and a data dependence on the thrown instruction
                        for (int i = stackDepth; i > 0; --i) {
                            if (atCatchBlockStart[i] != null) {
                                if (interestingInstructions[i].contains(atCatchBlockStart[i])) {
                                    if (dependantInterestingInstructions.isEmpty())
                                        dependantInterestingInstructions = Collections.singleton(atCatchBlockStart[i]);
                                    else
                                        dependantInterestingInstructions.add(atCatchBlockStart[i]);
                                }
                                atCatchBlockStart[i] = null;

                                // data dependence:
                                // (the stack height has already been decremented when entering the catch block)
                                Variable definedException = simEnv.getOpStackEntry(i, opStack[i]);
                                if (interestingVariables.contains(definedException)) {
                                    interestingInstructions[stackDepth].add(instruction);
                                    dynamicSlice.add(instruction);
                                    interestingVariables.remove(definedException);
                                    interestingVariables.addAll(dynInfo.getUsedVariables());
                                }

                                break;
                            }
                        }
                    }
                    if (!dependantInterestingInstructions.isEmpty()) {
                        dynamicSlice.add(instruction);
                        interestingInstructions[stackDepth].removeAll(dependantInterestingInstructions);
                        interestingInstructions[stackDepth].add(instruction);
                        interestingVariables.addAll(dynInfo.getUsedVariables());
                    }
                }

                if (!interestingVariables.isEmpty()) {
                    for (Variable definedVariable: dynInfo.getDefinedVariables()) {
                        if (interestingVariables.contains(definedVariable)) {
                            interestingInstructions[stackDepth].add(instruction);
                            dynamicSlice.add(instruction);
                            interestingVariables.remove(definedVariable);
                            interestingVariables.addAll(dynInfo.getUsedVariables(definedVariable));
                        }
                    }
                }

                if (dynInfo.isCatchBlock()) {
                    atCatchBlockStart[stackDepth] = instance.getInstruction();
                    interruptedControlFlow[stackDepth] = true;
                } else if (atCatchBlockStart[stackDepth] != null) {
                    atCatchBlockStart[stackDepth] = null;
                }

            }
        } finally {
            for (ProgressMonitor mon : this.progressMonitors)
                mon.end();
        }

        for (Iterator<Instruction> it = dynamicSlice.iterator(); it.hasNext(); ) {
        	Instruction instr = it.next();
        	if (instr.getType() == InstructionType.LABEL || instr.getOpcode() == Opcodes.GOTO)
        		it.remove();
        }

        return dynamicSlice;
    }

    private void computeControlDependences(ReadMethod method, IntegerMap<Set<Instruction>> controlDependences) {
        Map<Instruction, Set<Instruction>> deps = ControlFlowAnalyser.getInstance().getInvControlDependences(method);
        for (Entry<Instruction, Set<Instruction>> entry: deps.entrySet()) {
            int index = entry.getKey().getIndex();
            assert !controlDependences.containsKey(index);
            controlDependences.put(index, entry.getValue());
        }
    }

    private static <T> Set<T> intersect(Set<T> set1,
            Set<T> set2) {
        if (set1.isEmpty() || set2.isEmpty())
            return Collections.emptySet();

        Set<T> smallerSet;
        Set<T> biggerSet;
        if (set1.size() < set2.size()) {
            smallerSet = set1;
            biggerSet = set2;
        } else {
            smallerSet = set2;
            biggerSet = set1;
        }

        Set<T> intersection = null;
        for (T obj: smallerSet) {
            if (biggerSet.contains(obj)) {
                if (intersection == null)
                    intersection = new HashSet<T>();
                intersection.add(obj);
            }
        }

        if (intersection == null)
            return Collections.emptySet();
        return intersection;
    }

    @SuppressWarnings("static-access")
    private static Options createOptions() {
        Options options = new Options();
        options.addOption(OptionBuilder.isRequired(false).withArgName("threadid").hasArg(true).
            withDescription("thread id to select for slicing (default: main thread)").withLongOpt("threadid").create('t'));
        options.addOption(OptionBuilder.isRequired(false).hasArg(false).
            withDescription("show progress while computing the dynamic slice").withLongOpt("progress").create('p'));
        options.addOption(OptionBuilder.isRequired(false).hasArg(false).
            withDescription("print this help and exit").withLongOpt("help").create('h'));
        return options;
    }

    private static void printHelp(Options options, PrintStream out) {
        out.println("Usage: " + DirectSlicer.class.getSimpleName() + " [<options>] <file> <slicing criterion>");
        out.println("where <file> is the input trace file, and <options> may be one or more of");
        out.println("      <slicing criterion> has the form <loc>[(<occ>)]:<var>[,<loc>[(<occ>)]:<var>]*");
        out.println("      <options> may be one or more of");
        HelpFormatter formatter = new HelpFormatter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printOptions(pw, 120, options, 5, 3);
    }

}
