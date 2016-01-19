/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     StaticSlicingCriterion
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/slicing/StaticSlicingCriterion.java
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.LocalVariable;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.AbstractInstruction;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;

/**
 * Representation of a static slicing criterion.
 * Each slicing criterion may match an arbitrary number of dynamic instruction
 * instances, originating from an arbitrary number of static instructions.
 *
 * @author Clemens Hammacher
 */
public class StaticSlicingCriterion implements SlicingCriterion {

    public class StaticSlicingCriterionInstance implements SlicingCriterionInstance {

        private long seenOccurences = 0;
        private boolean[] beingInRun = new boolean[1];
        private int stackDepth = 0;

        /* (non-Javadoc)
		 * @see de.unisb.cs.st.javaslicer.slicing.SlicingCriterionInstance#matches(de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance)
		 */
        @Override
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
            if ((StaticSlicingCriterion.this.occurence != null &&
                    this.seenOccurences == StaticSlicingCriterion.this.occurence.longValue())
                || instructionInstance.getInstruction().getMethod() != StaticSlicingCriterion.this.method
                || (StaticSlicingCriterion.this.lineNumber != null && instructionInstance.getInstruction().getLineNumber() != StaticSlicingCriterion.this.lineNumber)) {
                if (this.beingInRun[instrStackDepth-1]) {
                    this.beingInRun[instrStackDepth-1] = false;
                }
                return false;
            }

            if (this.beingInRun[instrStackDepth-1])
                return true; // we want to see *all* instruction from that line

            // yeah, we have a new occurence!
            long newOccurenceNumber = ++this.seenOccurences;
            if (StaticSlicingCriterion.this.occurence == null ||
                    newOccurenceNumber == StaticSlicingCriterion.this.occurence.longValue()) {
                this.beingInRun[instrStackDepth-1] = true;
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return StaticSlicingCriterion.this.toString();
        }

        /* (non-Javadoc)
		 * @see de.unisb.cs.st.javaslicer.slicing.SlicingCriterionInstance#getLocalVariables()
		 */
        @Override
		public List<LocalVariable> getLocalVariables() {
            return StaticSlicingCriterion.this.variables;
        }

        /* (non-Javadoc)
		 * @see de.unisb.cs.st.javaslicer.slicing.SlicingCriterionInstance#hasLocalVariables()
		 */
        @Override
		public boolean hasLocalVariables() {
            return StaticSlicingCriterion.this.variables != null;
        }

        /* (non-Javadoc)
		 * @see de.unisb.cs.st.javaslicer.slicing.SlicingCriterionInstance#matchAllData()
		 */
        @Override
		public boolean computeTransitiveClosure() {
            return StaticSlicingCriterion.this.matchAllData;
        }

        @Override
		public long getOccurenceNumber() {
            return this.seenOccurences;
        }

    }

    protected final ReadMethod method;
    protected final Integer lineNumber;
    protected final Long occurence;
    protected final List<LocalVariable> variables;
    protected final boolean matchAllData;

    public StaticSlicingCriterion(ReadMethod method, Integer lineNumber,
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
        if (this.matchAllData)
            sb.append(":*");
        else if (this.variables != null) {
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

    /**
     * Parses and returns a slicing criterion consisting of a class name, method name, line
     * number (optional), occurence number (optional), and variable definitions (optional).
     *
     * The form of the given string to represent the slicing criterion is as follows:
     * It always starts with a location in the form <i>fullyQualifiedClassName.methodName</i>,
     * possibly followed by <i>:lineNumber</i>. If no line number is given, the criterion
     * matches as soon as the first instruction of the specified method is executed, which
     * would be the last instruction of the method when traversing the trace backwards.
     *
     * After the specification of the location, there can be given a set of local variables
     * covered by the slicing criterion, in the form <i>:{var1,var2,var3}</i> where var1...var3
     * must be the names of local variables occuring in the given method.
     * To cover all local variables, you can also specify <i>:*</i>.
     *
     * In total, the slicing criterion could look like this:
     * <ul><li> java.lang.String.indexOf:1768:{target,max} </li></ul>
     *
     * @param string the input string specifying a slicing criterion
     * @param readClasses the list of all classes of the trace result (see {@link TraceResult#getReadClasses()})
     * @return the parsed slicing criterion
     * @throws IllegalArgumentException if a problem with the input string is encountered. this
     *                                  could be that the overall form did not match, or that the
     *                                  class, method, line number or local variable(s) did not
     *                                  exist
     */
    public static StaticSlicingCriterion parse(String string, List<ReadClass> readClasses) throws IllegalArgumentException {
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

        return new StaticSlicingCriterion(method, lineNumber, occurence, variables, criterionMatchesAllData);
    }

    /**
     * Parses a sequence of comma separated slicing criterion, given in a string.
     * See {@link #parse(String, List)}
     *
     * @param string the input string specifying the slicing criteria (at least one), comma separated
     * @param readClasses the list of all classes of the trace result (see {@link TraceResult#getReadClasses()})
     * @return a list of all slicing criteria parsed
     * @throws IllegalArgumentException if a problem with the input string is encountered. this
     *                                  could be that the overall form did not match, or that a
     *                                  class, method, line number or local variable(s) did not
     *                                  exist
     */
    public static List<SlicingCriterion> parseAll(String string,
            List<ReadClass> readClasses) throws IllegalArgumentException {
        List<SlicingCriterion> crit = new ArrayList<SlicingCriterion>(2);
        int oldPos = 0;
        while (true) {
            int bracketPos = string.indexOf('{', oldPos);
            int commaPos = string.indexOf(',', oldPos);
            while (bracketPos != -1 && bracketPos < commaPos) {
                int closeBracketPos = string.indexOf('}', bracketPos + 1);
                if (closeBracketPos == -1)
                    throw new IllegalArgumentException(
                            "Couldn't find matching '}'");
                bracketPos = string.indexOf('{', closeBracketPos + 1);
                commaPos = string.indexOf(',', closeBracketPos + 1);
            }

            StaticSlicingCriterion newCrit =
                    StaticSlicingCriterion.parse(string.substring(oldPos,
                        commaPos == -1 ? string.length() : commaPos),
                        readClasses);
            oldPos = commaPos + 1;

            crit.add(newCrit);

            if (commaPos == -1)
                return crit;
        }
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
            throw new IllegalArgumentException("Class \"" + className + "\" does not occure in the trace.");

        ArrayList<ReadMethod> methods = foundClass.getMethods();
        left = 0;
        right = methods.size();
        if (right == 0)
            throw new IllegalArgumentException("Class \"" + className + "\" contains no methods.");

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
            StringBuilder errorMsg = new StringBuilder(64);
            errorMsg.append("Found no method with name ").append(methodName).append(" in class ").append(className);
            if (lineNumber != null)
                errorMsg.append(" which contains line number ").append(lineNumber);
            throw new IllegalArgumentException(errorMsg.toString());
        }
        throw new IllegalArgumentException("Method \"" + methodName + "\" does not exist in class \"" + className + "\"");
    }

    private static final Pattern variableDefinitionPattern = Pattern.compile(
        "\\s*(?:([a-zA-Z_][a-zA-Z0-9_\\-]*))\\s*");

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
                if (var != null && localVarStr.equals(var.getName())) {
                    varList.add(var);
                    continue outer;
                }
            }
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("Local variable '").append(localVarStr).append("' not found in method ");
            errorMsg.append(method.getReadClass().getName()).append(".").append(method.getName());
            errorMsg.append(". The method contains the following local variables: ").append(Arrays.toString(method.getLocalVariables()));
            throw new IllegalArgumentException(errorMsg.toString());
        }

        return varList;
    }

    /* (non-Javadoc)
	 * @see de.unisb.cs.st.javaslicer.slicing.SlicingCriterion#getInstance()
	 */
    @Override
	public StaticSlicingCriterionInstance getInstance() {
        return new StaticSlicingCriterionInstance();
    }

}
