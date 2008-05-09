package de.unisb.cs.st.javaslicer.tracer.traceSequences.sequitur;

public class TerminalSymbol<T> implements Symbol<T> {

    private final T value;

    public TerminalSymbol(final T value) {
        this.value = value;
    }

}
