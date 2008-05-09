package de.unisb.cs.st.javaslicer.tracer.traceSequences.sequitur;

import java.util.Collections;


public class SequiturTrace<T> {
    private RuleSymbol<T> startSymbol;
    private final SequiturGrammar<T> grammar;

    private T lastTraced = null;
    private int lastTracedCount = 0;

    public SequiturTrace(final RuleSymbol<T> startSymbol, final SequiturGrammar<T> grammar) {
        this.startSymbol = startSymbol;
        this.grammar = grammar;
    }

    public void trace(final T value) {
        assert value != null;
        if (value.equals(this.lastTraced)) {
            ++this.lastTracedCount;
        } else {
            if (this.lastTracedCount > 0) {
                final SymbolRuleEntry<T> entry = new SymbolRuleEntry<T>(
                        this.grammar.getTerminalSymbol(this.lastTraced), this.lastTracedCount);
                if (this.startSymbol == null) {
                    this.startSymbol = new RuleSymbol<T>(Collections.singletonList(entry));
                    grammar.newStartSymbol(this.startSymbol);
                } else {
                    this.grammar.append(this.startSymbol, entry);
                }
            }
            this.lastTraced = value;
            this.lastTracedCount = 1;
        }
    }

}
