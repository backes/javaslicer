/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.progress
 *    Class:     ProgressMonitor
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/progress/ProgressMonitor.java
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
