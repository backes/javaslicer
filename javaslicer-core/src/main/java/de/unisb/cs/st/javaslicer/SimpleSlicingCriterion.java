package de.unisb.cs.st.javaslicer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.LocalVariable;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.AbstractInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.IIncInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.VarInstruction;
import de.unisb.cs.st.javaslicer.instructionSimulation.ExecutionFrame;
import de.unisb.cs.st.javaslicer.variables.Variable;

public class SimpleSlicingCriterion implements SlicingCriterion {

    public static class LocalVariableCriterion implements CriterionVariable {

        private final ReadMethod method;
        private final int varIndex;

        public LocalVariableCriterion(final ReadMethod method, final int varIndex) {
            this.method = method;
            this.varIndex = varIndex;
        }

        public Variable instantiate(final ExecutionFrame<InstructionInstance> execFrame) {
            return execFrame.getLocalVariable(this.varIndex);
        }

        @Override
        public String toString() {
            final List<LocalVariable> locals = this.method.getLocalVariables();
            for (final LocalVariable loc: locals)
                if (loc.getIndex() == this.varIndex)
                    return loc.getName();
            return "<unknown>";
        }

    }

    public static interface CriterionVariable {

        Variable instantiate(ExecutionFrame<InstructionInstance> execFrame);

    }

    public class Instance implements SlicingCriterion.SlicingCriterionInstance {

        private long seenOccurences = 0;
        private boolean[] beingInRun = new boolean[1];
        private int stackDepth = 0;
        private Instruction lastMatch = null;

        public Collection<Variable> getInterestingVariables(ExecutionFrame<InstructionInstance> execFrame) {
            final List<Variable> varList = new ArrayList<Variable>(SimpleSlicingCriterion.this.variables.size());
            for (final CriterionVariable var: SimpleSlicingCriterion.this.variables)
                varList.add(var.instantiate(execFrame));

            return varList;
        }

        public Collection<Instruction> getInterestingInstructions(ExecutionFrame<InstructionInstance> currentFrame) {
            if (this.lastMatch == null)
                return Collections.emptySet();
            return Collections.singleton(this.lastMatch);
        }

        public boolean matches(final InstructionInstance instructionInstance) {
            final int instrStackDepth = instructionInstance.getStackDepth();
            if (this.stackDepth != instrStackDepth) {
                if (instrStackDepth > this.beingInRun.length) {
                    final boolean[] newArr = new boolean[Math.max(2*this.beingInRun.length, instrStackDepth)];
                    System.arraycopy(this.beingInRun, 0, newArr, 0, this.beingInRun.length);
                    this.beingInRun = newArr;
                }
                while (this.stackDepth < instrStackDepth) {
                    this.beingInRun[this.stackDepth++] = false;
                }
                this.stackDepth = instrStackDepth;
            }
            if ((SimpleSlicingCriterion.this.occurence != null &&
                    this.seenOccurences == SimpleSlicingCriterion.this.occurence)
                || instructionInstance.getInstruction().getMethod() != SimpleSlicingCriterion.this.method
                || (SimpleSlicingCriterion.this.lineNumber == null || instructionInstance.getInstruction().getLineNumber() != SimpleSlicingCriterion.this.lineNumber)) {
                if (this.beingInRun[instrStackDepth-1]) {
                    this.beingInRun[instrStackDepth-1] = false;
                    this.lastMatch = null;
                }
                return false;
            }

            if (this.beingInRun[instrStackDepth-1])
                return false;
            if (SimpleSlicingCriterion.this.occurence == null ||
                ++this.seenOccurences == SimpleSlicingCriterion.this.occurence) {
                this.beingInRun[instrStackDepth-1] = true;
                this.lastMatch = instructionInstance.getInstruction();
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return SimpleSlicingCriterion.this.toString();
        }

    }

    protected final ReadMethod method;
    protected final Integer lineNumber;
    protected final Long occurence;
    protected final Collection<CriterionVariable> variables;

    public SimpleSlicingCriterion(final ReadMethod method, final Integer lineNumber,
            final Long occurence, final Collection<CriterionVariable> variables) {
        this.method = method;
        this.lineNumber = lineNumber;
        this.occurence = occurence;
        this.variables = variables;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.method.getReadClass().getName()).append('.').append(this.method.getName());
        if (this.lineNumber != null)
            sb.append(':').append(this.lineNumber.intValue());
        if (this.occurence != null)
            sb.append('(').append(this.occurence.longValue()).append(')');
        if (this.variables != null && !this.variables.isEmpty()) {
            final Iterator<CriterionVariable> it = this.variables.iterator();
            sb.append(":{").append(it.next());
            while (it.hasNext())
                sb.append(',').append(it.next());
            sb.append('}');
        }
        return sb.toString();
    }

    private static final Pattern slicingCriterionPattern = Pattern.compile(
            "([^:{}]+)\\.([^:{}]+?)(?::(-?\\d+))?(?:\\((\\d+)\\))?(?::\\{(.*?)\\})?");

    public static SlicingCriterion parse(final String string, final List<ReadClass> readClasses) throws IllegalArgumentException {
        final Matcher matcher = slicingCriterionPattern.matcher(string);
        if (!matcher.matches())
            throw new IllegalArgumentException("Slicing criterion could not be parsed: " + string);

        final String className = matcher.group(1);
        final String methodName = matcher.group(2);
        final String lineNumberStr = matcher.group(3);
        final String occurenceStr = matcher.group(4);
        final String variableDef = matcher.group(5);
        Integer lineNumber = null;
        if (lineNumberStr != null)
            try {
                lineNumber = Integer.valueOf(lineNumberStr);
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException("Expected line number, found '"+lineNumberStr+"'");
            }
        Long occurence = null;
        if (occurenceStr != null)
            try {
                occurence = Long.valueOf(occurenceStr);
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException("Expected occurrence number, found '"+occurenceStr+"'");
            }

        final ReadMethod method = findMethod(readClasses, className, methodName, lineNumber);
        assert method != null;

        final Collection<CriterionVariable> variables = variableDef == null
            ? getUsedVariables(method, lineNumber)
            : parseVariables(method, variableDef);

        return new SimpleSlicingCriterion(method, lineNumber, occurence, variables);
    }

    private static Collection<CriterionVariable> getUsedVariables(final ReadMethod method, final Integer lineNumber) {
        final Set<Integer> usedLocalVariables = new TreeSet<Integer>();
        for (final Instruction instr: method.getInstructions()) {
            if (lineNumber == null || instr.getLineNumber() == lineNumber) {
                switch (instr.getOpcode()) {
                case Opcodes.ILOAD:
                case Opcodes.LLOAD:
                case Opcodes.FLOAD:
                case Opcodes.DLOAD:
                case Opcodes.ALOAD:
                    usedLocalVariables.add(((VarInstruction) instr).getLocalVarIndex());
                    break;
                case Opcodes.IINC:
                    usedLocalVariables.add(((IIncInstruction)instr).getLocalVarIndex());
                    break;
                }
            }
        }
        if (usedLocalVariables.isEmpty())
            return Collections.emptySet();
        final List<CriterionVariable> vars = new ArrayList<CriterionVariable>(usedLocalVariables.size());
        for (final Integer i: usedLocalVariables)
            vars.add(new LocalVariableCriterion(method, i));
        return vars;
    }

    private static ReadMethod findMethod(final List<ReadClass> readClasses, final String className, final String methodName,
            final Integer lineNumber) {
        // binary search
        int left = 0;
        int right = readClasses.size();
        int mid;

        while ((mid = (left + right) / 2) != left) {
            final ReadClass midVal = readClasses.get(mid);
            if (midVal.getName().compareTo(className) <= 0)
                left = mid;
            else
                right = mid;
        }

        final ReadClass foundClass = readClasses.get(mid);
        if (!className.equals(foundClass.getName()))
            throw new IllegalArgumentException("Class does not occure in the trace: " + className);

        final ArrayList<ReadMethod> methods = foundClass.getMethods();
        left = 0;
        right = methods.size();

        // search for the *highest* method with that name!
        while ((mid = (left + right) / 2) != left) {
            final ReadMethod midVal = methods.get(mid);
            if (midVal.getName().compareTo(methodName) <= 0)
                left = mid;
            else
                right = mid;
        }

        final ReadMethod foundMethod = methods.get(mid);
        if (foundMethod.getName().equals(methodName)) {
            for (ReadMethod m; mid >= 0 && (m=methods.get(mid)).getName().equals(methodName); --mid) {
                for (final AbstractInstruction instr: m.getInstructions()) {
                    if (lineNumber == null || instr.getLineNumber() == lineNumber)
                        return m;
                }
            }
            throw new IllegalArgumentException("Found no method with name " + methodName +
                    " in class " + className + " which contains line number " + lineNumber);
        }
        throw new IllegalArgumentException("Method \"" + methodName + "\" does not exist in class \"" + className + "\"");
    }

    private static final Pattern variableDefinitionPattern = Pattern.compile(
        "\\s*(?:([a-zA-Z_][a-zA-Z0-9_\\-]*)" // local variable
            + ")\\s*");

    private static Collection<CriterionVariable> parseVariables(final ReadMethod method, final String variables) throws IllegalArgumentException {
        final String[] parts = variables.split(",");
        final List<CriterionVariable> varList = new ArrayList<CriterionVariable>();
        for (final String part: parts) {
            final Matcher matcher = variableDefinitionPattern.matcher(part);
            if (!matcher.matches())
                throw new IllegalArgumentException("Illegal variable definition: " + part);
            final String localVarStr = matcher.group(1);
            if (localVarStr == null)
                throw new IllegalArgumentException("Illegal variable definition: " + part);

            int localVarIndex = -1;
            for (final LocalVariable var: method.getLocalVariables()) {
                if (localVarStr.equals(var.getName())) {
                    localVarIndex = var.getIndex();
                    break;
                }
            }
            if (localVarIndex == -1)
                throw new IllegalArgumentException("Local variable '"+localVarStr+"' not found in method "
                        + method.getReadClass().getName()+"."+method.getName());
            varList.add(new LocalVariableCriterion(method, localVarIndex));
        }

        return varList;
    }

    public SlicingCriterionInstance getInstance() {
        return new Instance();
    }

}
