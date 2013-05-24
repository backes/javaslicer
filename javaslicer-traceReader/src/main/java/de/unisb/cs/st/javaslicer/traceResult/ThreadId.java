/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult
 *    Class:     ThreadId
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/ThreadId.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
 */
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

    @Override
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
