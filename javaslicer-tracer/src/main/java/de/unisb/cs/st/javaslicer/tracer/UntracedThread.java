/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer
 *    Class:     UntracedThread
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/UntracedThread.java
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
