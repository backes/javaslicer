package de.unisb.cs.st.javaslicer.tracer.util.sequitur.input;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Set;

public class InputSequence<T> {

    private final Rule<T> firstRule;

    private InputSequence(final Rule<T> firstRule) {
        this.firstRule = firstRule;
    }

    public static InputSequence<Object> readFrom(final ObjectInputStream objIn)
            throws IOException, ClassNotFoundException {
        return readFrom(objIn, SharedInputGrammar.readFrom(objIn));
    }

    public static <T> InputSequence<T> readFrom(final ObjectInputStream objIn,
            final Class<? extends T> checkInstance) throws IOException, ClassNotFoundException {
        return readFrom(objIn, SharedInputGrammar.readFrom(objIn, checkInstance));
    }

    public static <T> InputSequence<T> readFrom(final ObjectInputStream objIn,
            final ObjectReader<? extends T> objectReader) throws IOException, ClassNotFoundException {
        return readFrom(objIn, SharedInputGrammar.readFrom(objIn, objectReader));
    }

    public static <T> InputSequence<T> readFrom(final ObjectInputStream objIn,
            final ObjectReader<? extends T> objectReader, final Class<? extends T> checkInstance)
            throws IOException, ClassNotFoundException {
        return readFrom(objIn, SharedInputGrammar.readFrom(objIn, objectReader, checkInstance));
    }

    public static <T> InputSequence<T> readFrom(final ObjectInputStream objIn, final SharedInputGrammar<T> sharedGrammar) throws IOException {
        if (sharedGrammar == null)
            throw new NullPointerException();
        final long startRuleNr = DataInput.readLong(objIn);
        final Rule<T> rule = sharedGrammar.grammar.getRule(startRuleNr);
        if (rule == null)
            throw new IOException("Unknown rule number");
        return new InputSequence<T>(rule);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final Iterator<Symbol<T>> it = this.firstRule.symbols.iterator();
        if (it.hasNext()) {
            sb.append(it.next());
            while (it.hasNext())
                sb.append(' ').append(it.next());
        }

        final Set<Rule<T>> rules = this.firstRule.getUsedRules();
        for (final Rule<T> r: rules)
            sb.append(System.getProperty("line.separator")).append(r);
        return sb.toString();
    }

}
