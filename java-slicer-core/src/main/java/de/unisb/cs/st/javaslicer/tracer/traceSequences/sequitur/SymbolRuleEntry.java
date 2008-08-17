package de.unisb.cs.st.javaslicer.tracer.traceSequences.sequitur;

public class SymbolRuleEntry<T> {
    private final Symbol<T> symbol;
    private final int count;

    public SymbolRuleEntry(final Symbol<T> symbol, final int count) {
        super();
        this.symbol = symbol;
        this.count = count;
    }

    public Symbol<T> getSymbol() {
        return this.symbol;
    }

    public int getCount() {
        return this.count;
    }

}
