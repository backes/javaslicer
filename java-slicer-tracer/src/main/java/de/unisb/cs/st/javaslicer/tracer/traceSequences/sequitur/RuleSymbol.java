package de.unisb.cs.st.javaslicer.tracer.traceSequences.sequitur;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class RuleSymbol<T> implements Symbol<T> {

    private final LinkedList<SymbolRuleEntry<T>> rule;

    public RuleSymbol(final List<SymbolRuleEntry<T>> rule) {
        this.rule = new LinkedList<SymbolRuleEntry<T>>(rule);
    }

    public ListIterator<SymbolRuleEntry<T>> append(final SymbolRuleEntry<T> newEntry) {
        this.rule.add(newEntry);
        return this.rule.listIterator(this.rule.size()-1);
    }

    public ListIterator<SymbolRuleEntry<T>> getRuleEntryIterator() {
        return this.rule.listIterator();
    }
}
