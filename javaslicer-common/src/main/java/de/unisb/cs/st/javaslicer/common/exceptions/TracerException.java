/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.exceptions
 *    Class:     TracerException
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/exceptions/TracerException.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.common.exceptions;

public class TracerException extends RuntimeException {

    private static final long serialVersionUID = -8175726329711097102L;

    public TracerException() {
        super();
    }

    public TracerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TracerException(final String message) {
        super(message);
    }

    public TracerException(final Throwable cause) {
        super(cause);
    }

}
