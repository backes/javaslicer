package de.unisb.cs.st.javaslicer.tracer.exceptions;

public class TracerException extends Exception {

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
