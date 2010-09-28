/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer
 *    Class:     UntracedThread
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/UntracedThread.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
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
