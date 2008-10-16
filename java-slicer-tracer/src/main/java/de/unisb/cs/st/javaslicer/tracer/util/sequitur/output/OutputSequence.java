package de.unisb.cs.st.javaslicer.tracer.util.sequitur.output;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Set;

public class OutputSequence<T> {

    private final Grammar<T> grammar;
    protected final Rule<T> firstRule;
    private final ObjectWriter<? super T> objectWriter;

    public OutputSequence() {
        this(new Rule<T>(false), new Grammar<T>(), null);
    }

    public OutputSequence(final SharedOutputGrammar<T> g) {
        this(new Rule<T>(false), g.grammar, null);
    }

    public OutputSequence(final ObjectWriter<? super T> objectWriter) {
        this(new Rule<T>(false), new Grammar<T>(), objectWriter);
    }

    public OutputSequence(final SharedOutputGrammar<T> g, final ObjectWriter<? super T> objectWriter) {
        this(new Rule<T>(false), g.grammar, objectWriter);
    }

    private OutputSequence(final Rule<T> firstRule, final Grammar<T> grammar,
            final ObjectWriter<? super T> objectWriter) {
        this.grammar = grammar;
        this.firstRule = firstRule;
        this.objectWriter = objectWriter;
        grammar.newSequence(this);
    }

    public void append(final T obj) {
        this.firstRule.append(new Terminal<T>(obj), this.grammar);
    }

    public long getStartRuleNumber() {
        return this.grammar.getRuleNr(this.firstRule);
    }

    public void writeOut(final ObjectOutputStream objOut, final boolean includeGrammar) throws IOException {
        if (includeGrammar)
            writeOutGrammar(objOut);
        DataOutput.writeLong(objOut, getStartRuleNumber());
    }

    public void writeOutGrammar(final ObjectOutputStream objOut) throws IOException {
        this.grammar.writeOut(objOut, this.objectWriter);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (this.firstRule.dummy.next != this.firstRule.dummy) {
            sb.append(this.firstRule.dummy.next);
            for (Symbol<T> s = this.firstRule.dummy.next.next; s != this.firstRule.dummy; s = s.next)
                sb.append(" ").append(s);
        }

        final Set<Rule<T>> rules = this.firstRule.getUsedRules();
        for (final Rule<T> r: rules)
            sb.append(System.getProperty("line.separator")).append(r);
        return sb.toString();
    }

}
