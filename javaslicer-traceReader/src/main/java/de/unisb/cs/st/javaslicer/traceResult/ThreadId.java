package de.unisb.cs.st.javaslicer.traceResult;

public class ThreadId implements Comparable<ThreadId> {

    private final long threadId;
    private final String threadName;

    public ThreadId(final long threadId, final String threadName) {
        this.threadId = threadId;
        this.threadName = threadName;
    }

    public long getJavaThreadId() {
        return this.threadId;
    }

    public String getThreadName() {
        return this.threadName;
    }

    @Override
    public String toString() {
        return this.threadId + ": " + this.threadName;
    }

    public int compareTo(final ThreadId other) {
        if (this.threadId == other.threadId) {
            final int nameCmp = this.threadName.compareTo(other.threadName);
            if (nameCmp == 0 && this != other)
                return System.identityHashCode(this) - System.identityHashCode(other);
            return nameCmp;
        }
        return Long.signum(this.threadId - other.threadId);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (this.threadId ^ (this.threadId >>> 32));
		result = prime * result + this.threadName.hashCode();
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
		final ThreadId other = (ThreadId) obj;
		if (this.threadId != other.threadId)
			return false;
		if (!this.threadName.equals(other.threadName))
			return false;
		return true;
	}

}