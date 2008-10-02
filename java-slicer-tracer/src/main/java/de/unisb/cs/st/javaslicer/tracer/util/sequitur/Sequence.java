package de.unisb.cs.st.javaslicer.tracer.util.sequitur;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

public class Sequence<T> {

    private final Grammar<T> grammar;
    private final Rule<T> firstRule;

    public Sequence() {
        this(new Rule<T>(false), new Grammar<T>());
    }

    public Sequence(final SharedGrammar<T> g) {
        this(new Rule<T>(false), g.grammar);
    }

    private Sequence(final Rule<T> firstRule, final Grammar<T> grammar) {
        this.grammar = grammar;
        this.firstRule = firstRule;
    }

    public void append(final T obj) {
        this.firstRule.append(new Terminal<T>(obj), this.grammar);
    }

    public void writeOut(final ObjectOutputStream objOut, final boolean includeGrammar) {
        if (includeGrammar)
            this.grammar.writeOut(objOut);
        this.firstRule.writeOut(objOut, this.grammar);
    }

    public void writeOutGrammar(final ObjectOutputStream objOut) {
        this.grammar.writeOut(objOut);
    }

    public static Sequence<Object> readFrom(final ObjectInputStream objIn) {
        return readFrom(objIn, SharedGrammar.readFrom(objIn));
    }

    public static <T> Sequence<T> readFrom(final ObjectInputStream objIn, final Class<? extends T> checkInstance) {
        if (checkInstance == null)
            throw new NullPointerException();
        return readFrom(objIn, SharedGrammar.readFrom(objIn, checkInstance), checkInstance);
    }

    public static Sequence<Object> readFrom(final ObjectInputStream objIn, final SharedGrammar<Object> sharedGrammar) {
        if (sharedGrammar == null)
            throw new NullPointerException();
        final Rule<Object> rule = Rule.readFrom(objIn, sharedGrammar.grammar, null);
        return new Sequence<Object>(rule, sharedGrammar.grammar);
    }

    public static <T> Sequence<T> readFrom(final ObjectInputStream objIn, final SharedGrammar<T> sharedGrammar,
            final Class<? extends T> checkInstance) {
        if (sharedGrammar == null || checkInstance == null)
            throw new NullPointerException();
        final Rule<T> rule = Rule.readFrom(objIn, sharedGrammar.grammar, checkInstance);
        return new Sequence<T>(rule, sharedGrammar.grammar);
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
