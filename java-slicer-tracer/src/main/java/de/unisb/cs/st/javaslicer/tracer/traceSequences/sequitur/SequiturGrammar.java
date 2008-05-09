package de.unisb.cs.st.javaslicer.tracer.traceSequences.sequitur;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class SequiturGrammar<T> {

    private static class SymbolUse<T> {
        public Symbol<T> symbol;
        public SymbolRuleEntry<T> successorEntry;

        public SymbolUse(final Symbol<T> symbol, final SymbolRuleEntry<T> successorEntry) {
            this.symbol = symbol;
            this.successorEntry = successorEntry;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.successorEntry.hashCode();
            result = prime * result + this.symbol.hashCode();
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final SymbolUse<?> other = (SymbolUse<?>) obj;
            if (this.successorEntry == null) {
                if (other.successorEntry != null)
                    return false;
            } else if (!this.successorEntry.equals(other.successorEntry))
                return false;
            if (!this.symbol.equals(other.symbol))
                return false;
            return true;
        }

    }

    private final Map<T, TerminalSymbol<T>> terminals = new WeakHashMap<T, TerminalSymbol<T>>();

    // stores all symbols and where they are used
    private final Map<Symbol<T>, Set<SymbolUse<T>>> symbols =
        new IdentityHashMap<Symbol<T>, Set<SymbolUse<T>>>();

    private final Map<RuleSymbol<T>, Object> startSymbols = new IdentityHashMap<RuleSymbol<T>, Object>();

    public void newStartSymbol(final RuleSymbol<T> sym) {
        this.startSymbols.put(sym, 1);
    }

    public TerminalSymbol<T> getTerminalSymbol(final T value) {
        TerminalSymbol<T> sym = this.terminals.get(value);
        if (sym == null)
            this.terminals.put(value, sym = new TerminalSymbol<T>(value));
        return sym;
    }

    public void append(final RuleSymbol<T> sym, final SymbolRuleEntry<T> entry) {
        final ListIterator<SymbolRuleEntry<T>> it = sym.append(entry);
        final Symbol<T> usedSymbol = entry.getSymbol();

        // register this "new" symbol in the symbol uses
        Set<SymbolUse<T>> otherUses = this.symbols.get(usedSymbol);
        if (otherUses == null)
            this.symbols.put(usedSymbol, otherUses = new HashSet<SymbolUse<T>>());
        otherUses.add(new SymbolUse<T>(usedSymbol, null));

        // change symbol use of preceding symbol
        if (it.hasPrevious()) {
            final SymbolRuleEntry<T> precedingEntry = it.previous();
            it.next();
            final Symbol<T> precedingSymbol = precedingEntry.getSymbol();
            final Set<SymbolUse<T>> precedingSymbolUses = this.symbols.get(precedingSymbol);
            final SymbolUse<T> searchedSymbolUse = new SymbolUse<T>(sym, null);
            final boolean deleted = precedingSymbolUses.remove(searchedSymbolUse);
            assert deleted;
            final SymbolUse<T> newSymbolUse = new SymbolUse<T>(sym, entry);
            final boolean added = precedingSymbolUses.add(newSymbolUse);
            assert added;
        }

        // now, that the symbols table is updated, we check for the invariants
        changed(sym, it);
    }

    private void changed(final RuleSymbol<T> symbol, final ListIterator<SymbolRuleEntry<T>> it) {
        assert it.hasNext();
        final SymbolRuleEntry<T> thisEntry = it.next();
        it.previous();

        // check if the combination of the last rule and this rule also occurs somewhere else
        if (it.hasPrevious()) {
            final SymbolRuleEntry<T> precedingEntry = it.previous();
            it.next();
            final Set<SymbolUse<T>> usesOfPrecedingEntry = this.symbols.get(precedingEntry);
            RuleSymbol<T> otherUseOfThisPair = null;
            for (final Iterator<SymbolUse<T>> useIt = usesOfPrecedingEntry.iterator(); useIt.hasNext(); ) {
                final SymbolUse<T> useOfPrecedingEntry = useIt.next();
                if (thisEntry.equals(useOfPrecedingEntry.successorEntry) && !symbol.equals(useOfPrecedingEntry.symbol)) {
                    assert useOfPrecedingEntry.symbol instanceof RuleSymbol<?>;
                    otherUseOfThisPair = (RuleSymbol<T>) useOfPrecedingEntry.symbol;
                    break;
                }
            }
            if (otherUseOfThisPair != null) {
                // search the other symbol for the use of this pair
                int minUseFst = -1;
                int minUseSnd = -1;
                for (final Iterator<SymbolRuleEntry<T>> it2 = otherUseOfThisPair.getRuleEntryIterator(); it2.hasNext(); ) {
                    final SymbolRuleEntry<T> e = it2.next();
                    if (e.getSymbol().equals(precedingEntry.getSymbol()) && it2.hasNext()) {
                        // check if the next symbol entry matches too
                        final SymbolRuleEntry<T> e2 = it2.next();
                        if (e2.getSymbol().equals(thisEntry.getSymbol())) {
                            minUseFst = Math.min(precedingEntry.getCount(), e.getCount());
                            minUseSnd = Math.min(thisEntry.getCount(), e2.getCount());
                        }
                    }
                }
                assert minUseFst != -1 && minUseSnd != -1;
                final List<SymbolRuleEntry<T>> newRule = new ArrayList<SymbolRuleEntry<T>>(2);
                newRule.add(new SymbolRuleEntry<T>(precedingEntry.getSymbol(), minUseFst));
                newRule.add(new SymbolRuleEntry<T>(thisEntry.getSymbol(), minUseSnd));
                final RuleSymbol<T> newSymbol = newSymbol(newRule);
                // TODO
            }


        }
    }

    private RuleSymbol<T> newSymbol(final List<SymbolRuleEntry<T>> newRule) {
        // check if this symbol already exists, otherwise create it and restructure the rules
        // TODO Auto-generated method stub
        return null;
    }

}
