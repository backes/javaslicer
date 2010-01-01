package de.unisb.cs.st.javaslicer.progress;

import de.unisb.cs.st.javaslicer.common.classRepresentation.TraceIterator;


public interface ProgressMonitor {

    /**
     * Notifies this progress monitor that trace iteration starts on the given iterator.
     * The progress monitor can then asynchronously check the percentage done, and
     * process this information.
     *
     * @param iterator the iterator which is started to iterate
     */
    public void start(TraceIterator iterator);

    /**
     * Notifies this progress monitor that trace iteration has finished.
     */
    public void end();

}
