package de.unisb.cs.st.javaslicer.tracer.util.sequitur.input;

import java.io.ObjectInputStream;

public class SharedInputGrammar<T> {

    protected final Grammar<T> grammar;

    public SharedInputGrammar() {
        this(new Grammar<T>());
    }

    protected SharedInputGrammar(final Grammar<T> grammar) {
        if (grammar == null)
            throw new NullPointerException();
        this.grammar = grammar;
    }

    public static SharedInputGrammar<Object> readFrom(final ObjectInputStream objIn) {
        // TODO Auto-generated method stub
        return null;
    }

    public static <T> SharedInputGrammar<T> readFrom(final ObjectInputStream objIn, final Class<? extends T> checkInstance) {
        // TODO
        return null;
    }

}
