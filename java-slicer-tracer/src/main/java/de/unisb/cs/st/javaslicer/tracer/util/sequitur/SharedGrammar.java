package de.unisb.cs.st.javaslicer.tracer.util.sequitur;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SharedGrammar<T> {

    protected final Grammar<T> grammar;

    public SharedGrammar() {
        this(new Grammar<T>());
    }

    protected SharedGrammar(final Grammar<T> grammar) {
        if (grammar == null)
            throw new NullPointerException();
        this.grammar = grammar;
    }

    public static SharedGrammar<Object> readFrom(final ObjectInputStream objIn) {
        final Grammar<Object> grammar = Grammar.readFrom(objIn, null);
        return new SharedGrammar<Object>(grammar);
    }

    public static <T> SharedGrammar<T> readFrom(final ObjectInputStream objIn, final Class<? extends T> checkInstance) {
        final Grammar<T> grammar = Grammar.readFrom(objIn, checkInstance);
        return new SharedGrammar<T>(grammar);
    }

    public void writeOut(final ObjectOutputStream objOut) {
        this.grammar.writeOut(objOut);
    }

}
