package de.unisb.cs.st.javaslicer.tracer.util.sequitur.input;

public class NonTerminal<T> extends Symbol<T> {

    private final Rule<T> rule;
    private int count = 1;

    public NonTerminal(final Rule<T> rule) {
        assert rule != null;
        this.rule = rule;
        rule.incUseCount();
    }

    @Override
    public void remove() {
        super.remove();
        this.rule.decUseCount();
    }

    public Rule<T> getRule() {
        return this.rule;
    }

    @Override
    protected boolean singleEquals(final Symbol<?> obj) {
        if (obj.getClass() != this.getClass())
            return false;
        final NonTerminal<?> other = (NonTerminal<?>) obj;
        return this.count == other.count && this.rule.equals(other.rule);
    }

    @Override
    protected int singleHashcode() {
        return this.rule.hashCode() + 31*this.count;
    }

    public void checkExpand() {
        assert this.rule.getUseCount() > 0;
        assert this.count >= 1;
        if (this.count == 1 && this.rule.getUseCount() == 1) {
            remove();
            linkTogether(this.prev, this.rule.dummy.next);
            linkTogether(this.rule.dummy.prev, this.next);
        }
    }

    @Override
    public boolean meltDigram() {
        if (this.next.getClass() != this.getClass())
            return false;

        final NonTerminal<?> otherNonT = (NonTerminal<?>) this.next;
        if (otherNonT.rule.equals(this.rule)) {
            this.count += otherNonT.count;
            otherNonT.remove();
            checkExpand();
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected NonTerminal<T> clone() {
        final NonTerminal<T> clone = (NonTerminal<T>) super.clone();
        clone.rule.incUseCount();
        return clone;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('R').append(this.rule.ruleNr);
        assert this.count >= 1;
        if (this.count > 1) {
            sb.append('^').append(this.count);
        }
        return sb.toString();
    }

}
