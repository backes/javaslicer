package de.unisb.cs.st.javaslicer.tracer.util.sequitur.input;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.unisb.cs.st.javaslicer.tracer.util.MyByteArrayInputStream;

// package-private
class Rule<T> {

    protected final List<Symbol<T>> symbols;

    protected Rule(final List<Symbol<T>> symbols) {
        this.symbols = symbols;
    }

    public void substituteRealRules(final Map<Long, Rule<T>> rules) {
        final ListIterator<Symbol<T>> it = this.symbols.listIterator();
        while (it.hasNext()) {
            final Symbol<T> sym = it.next();
            if (sym instanceof NonTerminal<?>)
                it.set(((NonTerminal<T>)sym).substituteRealRules(rules));
        }
    }

    public Set<Rule<T>> getUsedRules() {
        Set<Rule<T>> rules = new HashSet<Rule<T>>();
        long rulesAdded = 0;
        final Queue<Rule<T>> ruleQueue = new LinkedList<Rule<T>>();
        ruleQueue.add(this);

        while (!ruleQueue.isEmpty()) {
            final Rule<T> r = ruleQueue.poll();
            for (final Symbol<T> s: r.symbols) {
                if (s instanceof NonTerminal<?>) {
                    final Rule<T> newR = ((NonTerminal<T>)s).getRule();
                    if (rules.add(newR)) {
                        if (++rulesAdded == 1<<30)
                            rules = new TreeSet<Rule<T>>(rules);
                        ruleQueue.add(newR);
                    }
                }
            }
        }

        return rules;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("R").append(hashCode()).append(" -->");
        for (final Symbol<T> sym: this.symbols)
            sb.append(' ').append(sym);
        return sb.toString();
    }

    @SuppressWarnings("fallthrough")
    public static <T> Map<Long, Rule<T>> readAll(final ObjectInputStream objIn,
            final ObjectReader<? extends T> objectReader,
            final Class<? extends T> checkInstance) throws IOException, ClassNotFoundException {

        Map<Long, Rule<T>> rules = new HashMap<Long, Rule<T>>();
        long rulesRead = 0;
        readRules:
        while (true) {
            int header = objIn.read();
            long length;
            boolean ready = false;
            switch (header >> 6) {
            case 0:
                ready = true;
                // fall through
            case 1:
                length = DataInput.readLong(objIn);
                if (length == 0)
                    break readRules;
                break;
            case 2:
                length = 2;
                break;
            case 3:
                length = 3;
                break;
            default:
                throw new InternalError();
            }
            final long headerBytes = (length + 3) / 4;
            if (headerBytes > Integer.MAX_VALUE)
                throw new IOException("Rule longer than 4*Integer.MAX_VALUE??");
            final byte[] headerBuf = new byte[(int) headerBytes];
            final MyByteArrayInputStream headerInputStream = new MyByteArrayInputStream(headerBuf);
            objIn.readFully(headerBuf);
            final List<Symbol<T>> symbols = new ArrayList<Symbol<T>>((int)headerBytes);
            int pos = 3;
            while (length-- > 0) {
                if (--pos < 0) {
                    header = headerInputStream.read();
                    pos = 3;
                }
                switch ((header >> pos) & 3) {
                case 0:
                    {
                    final NonTerminal<T> readFrom = NonTerminal.readFrom(objIn, false);
                    symbols.add(readFrom);
                    }
                    break;
                case 1:
                    {
                    final NonTerminal<T> readFrom = NonTerminal.readFrom(objIn, true);
                    symbols.add(readFrom);
                    }
                    break;
                case 2:
                    symbols.add(Terminal.readFrom(objIn, false, objectReader, checkInstance));
                    break;
                case 3:
                    symbols.add(Terminal.readFrom(objIn, false, objectReader, checkInstance));
                    break;
                default:
                    throw new InternalError();
                }
            }
            if (rulesRead == 1<<30)
                rules = new TreeMap<Long, Rule<T>>(rules);
            rules.put(rulesRead++, new Rule<T>(symbols));
            if (ready)
                break;
        }

        return rules;
    }

}
