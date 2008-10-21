package de.unisb.cs.st.javaslicer;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction.Instance;

public class SimpleSlicingCriterion implements SlicingCriterion {

    private final String className;
    private final String methodName;
    private final Integer lineNumber;
    private final Long occurence;
    private final Collection<Variable> variables;

    public SimpleSlicingCriterion(final String className, final String methodName, final Integer lineNumber, final Long occurence, final Collection<Variable> variables) {
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.occurence = occurence;
        this.variables = variables;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(this.className.length()+this.methodName.length()
                +(this.lineNumber == null ? 1 : 8)+(this.occurence == null ? 0 : 10));
        sb.append(this.className).append('.').append(this.methodName);
        if (this.lineNumber != null)
            sb.append(':').append(this.lineNumber.intValue());
        if (this.occurence != null)
            sb.append('(').append(this.occurence.longValue()).append(')');
        return sb.toString();
    }

    private static final Pattern slicingCriterionPattern = Pattern.compile(
            "(.*)\\.(.*?)(?::(\\d+))?(?:\\((\\d+)\\))?(?::(.*?))");

    public static SlicingCriterion parse(final String string) {
        final Matcher matcher = slicingCriterionPattern.matcher(string);
        if (matcher.matches()) {
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
                    // TODO
                    return null;
                }
            Long occurence = null;
            if (occurenceStr != null)
                try {
                    occurence = Long.valueOf(occurenceStr);
                } catch (final NumberFormatException e) {
                    // TODO
                    return null;
                }
            final Collection<Variable> variables = parseVariables(variableDef);
            if (className.length() > 0 && methodName.length() > 0)
                return new SimpleSlicingCriterion(className, methodName, lineNumber, occurence, variables);
        }

        // on error: return null
        return null;
    }

    private static Collection<Variable> parseVariables(final String variables) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Variable> getInterestingVariables() {
        return this.variables;
    }

    @Override
    public boolean matches(final Instance instructionInstance) {
        // TODO Auto-generated method stub
        return false;
    }

}
