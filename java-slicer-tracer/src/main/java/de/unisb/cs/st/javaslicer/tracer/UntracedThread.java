package de.unisb.cs.st.javaslicer.tracer;

public class UntracedThread extends Thread {

    public UntracedThread() {
        super();
    }

    public UntracedThread(final Runnable target, final String name) {
        super(target, name);
    }

    public UntracedThread(final Runnable target) {
        super(target);
    }

    public UntracedThread(final String name) {
        super(name);
    }

    public UntracedThread(final ThreadGroup group, final Runnable target, final String name, final long stackSize) {
        super(group, target, name, stackSize);
    }

    public UntracedThread(final ThreadGroup group, final Runnable target, final String name) {
        super(group, target, name);
    }

    public UntracedThread(final ThreadGroup group, final Runnable target) {
        super(group, target);
    }

    public UntracedThread(final ThreadGroup group, final String name) {
        super(group, name);
    }

}
