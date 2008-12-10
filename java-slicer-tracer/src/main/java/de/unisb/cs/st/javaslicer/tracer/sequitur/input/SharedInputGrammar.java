package de.unisb.cs.st.javaslicer.tracer.sequitur.input;

import java.io.IOException;
import java.io.ObjectInputStream;

public class SharedInputGrammar<T> {

    protected final Grammar<T> grammar;

    protected SharedInputGrammar(final Grammar<T> grammar) {
        if (grammar == null)
            throw new NullPointerException();
        this.grammar = grammar;
    }

    public static SharedInputGrammar<Object> readFrom(final ObjectInputStream objIn) throws IOException, ClassNotFoundException {
        return new SharedInputGrammar<Object>(Grammar.readFrom(objIn, null, null));
    }

    public static <T> SharedInputGrammar<T> readFrom(final ObjectInputStream objIn,
            final ObjectReader<? extends T> objectReader) throws IOException, ClassNotFoundException {
        return new SharedInputGrammar<T>(Grammar.readFrom(objIn, objectReader, null));
    }

    public static <T> SharedInputGrammar<T> readFrom(final ObjectInputStream objIn,
            final Class<? extends T> checkInstance) throws IOException, ClassNotFoundException {
        return new SharedInputGrammar<T>(Grammar.readFrom(objIn, null, checkInstance));
    }

    public static <T> SharedInputGrammar<T> readFrom(final ObjectInputStream objIn,
            final ObjectReader<? extends T> objectReader, final Class<? extends T> checkInstance) throws IOException, ClassNotFoundException {
        return new SharedInputGrammar<T>(Grammar.readFrom(objIn, objectReader, checkInstance));
    }

}
