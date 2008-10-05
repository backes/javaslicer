package de.unisb.cs.st.javaslicer.tracer.util.sequitur.input;

import java.io.ObjectInputStream;
import java.util.Set;

public class InputSequence<T> {

    private final Grammar<T> grammar;
    private final Rule<T> firstRule;

    public InputSequence() {
        this(new Rule<T>(false), new Grammar<T>());
    }

    public InputSequence(final SharedInputGrammar<T> g) {
        this(new Rule<T>(false), g.grammar);
    }

    private InputSequence(final Rule<T> firstRule, final Grammar<T> grammar) {
        this.grammar = grammar;
        this.firstRule = firstRule;
    }

    public static InputSequence<Object> readFrom(final ObjectInputStream objIn) {
        return readFrom(objIn, SharedInputGrammar.readFrom(objIn));
    }

    public static <T> InputSequence<T> readFrom(final ObjectInputStream objIn, final Class<? extends T> checkInstance) {
        if (checkInstance == null)
            throw new NullPointerException();
        return readFrom(objIn, SharedInputGrammar.readFrom(objIn, checkInstance), checkInstance);
    }

    public static InputSequence<Object> readFrom(final ObjectInputStream objIn, final SharedInputGrammar<Object> sharedGrammar) {
        if (sharedGrammar == null)
            throw new NullPointerException();
        final Rule<Object> rule = Rule.readFrom(objIn, sharedGrammar.grammar, null);
        return new InputSequence<Object>(rule, sharedGrammar.grammar);
    }

    public static <T> InputSequence<T> readFrom(final ObjectInputStream objIn, final SharedInputGrammar<T> sharedGrammar,
            final Class<? extends T> checkInstance) {
        if (sharedGrammar == null || checkInstance == null)
            throw new NullPointerException();
        final Rule<T> rule = Rule.readFrom(objIn, sharedGrammar.grammar, checkInstance);
        return new InputSequence<T>(rule, sharedGrammar.grammar);
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
