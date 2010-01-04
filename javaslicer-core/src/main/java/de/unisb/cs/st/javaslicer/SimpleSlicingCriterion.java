package de.unisb.cs.st.javaslicer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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

public class SimpleSlicingCriterion implements SlicingCriterion {

    public class Instance implements SlicingCriterionInstance {

        private long seenOccurences = 0;
        private boolean[] beingInRun = new boolean[1];
        private int stackDepth = 0;

        public boolean matches(InstructionInstance instructionInstance) {
            int instrStackDepth = instructionInstance.getStackDepth();
            if (this.stackDepth != instrStackDepth) {
                if (instrStackDepth > this.beingInRun.length) {
                    boolean[] newArr = new boolean[Math.max(2*this.beingInRun.length, instrStackDepth)];
                    System.arraycopy(this.beingInRun, 0, newArr, 0, this.beingInRun.length);
                    this.beingInRun = newArr;
                }
                while (this.stackDepth < instrStackDepth) {
                    this.beingInRun[this.stackDepth++] = false;
                }
                this.stackDepth = instrStackDepth;
            }
            if ((SimpleSlicingCriterion.this.occurence != null &&
                    this.seenOccurences == SimpleSlicingCriterion.this.occurence.longValue())
                || instructionInstance.getInstruction().getMethod() != SimpleSlicingCriterion.this.method
                || (SimpleSlicingCriterion.this.lineNumber == null || instructionInstance.getInstruction().getLineNumber() != SimpleSlicingCriterion.this.lineNumber)) {
                if (this.beingInRun[instrStackDepth-1]) {
                    this.beingInRun[instrStackDepth-1] = false;
                }
                return false;
            }

            if (this.beingInRun[instrStackDepth-1])
                return true; // we want to see *all* instruction from that line

            // yeah, we have a new occurence!
            long newOccurenceNumber = ++this.seenOccurences;
            if (SimpleSlicingCriterion.this.occurence == null ||
                    newOccurenceNumber == SimpleSlicingCriterion.this.occurence.longValue()) {
                this.beingInRun[instrStackDepth-1] = true;
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return SimpleSlicingCriterion.this.toString();
        }

        public List<LocalVariable> getLocalVariables() {
            return SimpleSlicingCriterion.this.variables;
        }

        public boolean hasLocalVariables() {
            return SimpleSlicingCriterion.this.variables != null;
        }

        public boolean matchAllData() {
            return SimpleSlicingCriterion.this.matchAllData;
        }

        public long getOccurenceNumber() {
            return this.seenOccurences;
        }

    }

    protected final ReadMethod method;
    protected final Integer lineNumber;
    protected final Long occurence;
    protected final List<LocalVariable> variables;
    protected final boolean matchAllData;

    public SimpleSlicingCriterion(ReadMethod method, Integer lineNumber,
            Long occurence, List<LocalVariable> variables, boolean criterionMatchesAllData) {
        this.method = method;
        this.lineNumber = lineNumber;
        this.occurence = occurence;
        if (variables == null || variables.isEmpty())
            this.variables = null;
        else if (variables.size() == 1)
            this.variables = Collections.singletonList(variables.get(0));
        else
            this.variables = Collections.unmodifiableList(new ArrayList<LocalVariable>(variables));
        this.matchAllData = criterionMatchesAllData;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.method.getReadClass().getName()).append('.').append(this.method.getName());
        if (this.lineNumber != null)
            sb.append(':').append(this.lineNumber.intValue());
        if (this.occurence != null)
            sb.append('(').append(this.occurence.longValue()).append(')');
        if (this.variables != null) {
            Iterator<LocalVariable> it = this.variables.iterator();
            sb.append(":{").append(it.next());
            while (it.hasNext())
                sb.append(',').append(it.next());
            sb.append('}');
        }
        return sb.toString();
    }

    private static final Pattern slicingCriterionPattern = Pattern.compile(
            "([^:{}]+)\\.([^:{}]+?)(?::(-?\\d+))?(?:\\((\\d+)\\))?(?::\\{(.*?)\\}|:(\\*))?");

    public static SlicingCriterion parse(String string, List<ReadClass> readClasses) throws IllegalArgumentException {
        Matcher matcher = slicingCriterionPattern.matcher(string);
        if (!matcher.matches())
            throw new IllegalArgumentException("Slicing criterion could not be parsed: " + string);

        String className = matcher.group(1);
        String methodName = matcher.group(2);
        String lineNumberStr = matcher.group(3);
        String occurenceStr = matcher.group(4);
        String variableDef = matcher.group(5);
        String matchAllData = matcher.group(6);

        Integer lineNumber = null;
        if (lineNumberStr != null)
            try {
                lineNumber = Integer.valueOf(lineNumberStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Expected line number, found '"+lineNumberStr+"'");
            }
        Long occurence = null;
        if (occurenceStr != null)
            try {
                occurence = Long.valueOf(occurenceStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Expected occurrence number, found '"+occurenceStr+"'");
            }

        ReadMethod method = findMethod(readClasses, className, methodName, lineNumber);
        assert method != null;

        List<LocalVariable> variables = variableDef == null ? null : parseVariables(method, variableDef);

        boolean criterionMatchesAllData = "*".equals(matchAllData);

        return new SimpleSlicingCriterion(method, lineNumber, occurence, variables, criterionMatchesAllData);
    }

    private static List<LocalVariable> getUsedVariables(ReadMethod method, Integer lineNumber) {
        Set<Integer> usedLocalVariables = new HashSet<Integer>();
        for (Instruction instr: method.getInstructions()) {
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
            return Collections.emptyList();
        List<LocalVariable> vars = new ArrayList<LocalVariable>(usedLocalVariables.size());
        for (LocalVariable var : method.getLocalVariables())
            if (usedLocalVariables.contains(var.getIndex()))
                vars.add(var);
        return vars;
    }

    private static ReadMethod findMethod(List<ReadClass> readClasses, String className, String methodName,
            Integer lineNumber) {
        // binary search
        int left = 0;
        int right = readClasses.size();
        int mid;

        while ((mid = (left + right) / 2) != left) {
            ReadClass midVal = readClasses.get(mid);
            if (midVal.getName().compareTo(className) <= 0)
                left = mid;
            else
                right = mid;
        }

        ReadClass foundClass = readClasses.get(mid);
        if (!className.equals(foundClass.getName()))
            throw new IllegalArgumentException("Class does not occure in the trace: " + className);

        ArrayList<ReadMethod> methods = foundClass.getMethods();
        left = 0;
        right = methods.size();

        // search for the *highest* method with that name!
        while ((mid = (left + right) / 2) != left) {
            ReadMethod midVal = methods.get(mid);
            if (midVal.getName().compareTo(methodName) <= 0)
                left = mid;
            else
                right = mid;
        }

        ReadMethod foundMethod = methods.get(mid);
        if (foundMethod.getName().equals(methodName)) {
            for (ReadMethod m; mid >= 0 && (m=methods.get(mid)).getName().equals(methodName); --mid) {
                for (AbstractInstruction instr: m.getInstructions()) {
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

    private static List<LocalVariable> parseVariables(ReadMethod method, String variables) throws IllegalArgumentException {
        String[] parts = variables.split(",");
        List<LocalVariable> varList = new ArrayList<LocalVariable>();

        outer:
        for (String part: parts) {
            Matcher matcher = variableDefinitionPattern.matcher(part);
            if (!matcher.matches())
                throw new IllegalArgumentException("Illegal variable definition: " + part);
            String localVarStr = matcher.group(1);
            if (localVarStr == null)
                throw new IllegalArgumentException("Illegal variable definition: " + part);

            for (LocalVariable var: method.getLocalVariables()) {
                if (localVarStr.equals(var.getName())) {
                    varList.add(var);
                    continue outer;
                }
            }
            throw new IllegalArgumentException("Local variable '"+localVarStr+"' not found in method "
                    + method.getReadClass().getName()+"."+method.getName());
        }

        return varList;
    }

    public SlicingCriterionInstance getInstance() {
        return new Instance();
    }

}
