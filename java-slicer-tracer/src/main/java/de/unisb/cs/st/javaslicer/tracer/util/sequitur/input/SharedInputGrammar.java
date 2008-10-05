package de.unisb.cs.st.javaslicer.tracer.util.sequitur.input;

import java.io.ObjectOutputStream;

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

    public void writeOut(final ObjectOutputStream objOut) {
        this.grammar.writeOut(objOut);
    }

}
