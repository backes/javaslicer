package de.unisb.cs.st.javaslicer.common.progress;



public interface ProgressMonitor {

    /**
     * Notifies this progress monitor that trace iteration starts on the given iterator.
     * The progress monitor can then asynchronously check the percentage done, and
     * process this information.
     *
     * @param progressInfoProv an object from which the current percentage done can be
     *                         retrieved
     */
    public void start(ProgressInformationProvider progressInfoProv);

    /**
     * Notifies this progress monitor that trace iteration has finished.
     */
    public void end();

}
