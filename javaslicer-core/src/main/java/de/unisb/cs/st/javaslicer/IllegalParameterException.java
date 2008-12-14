package de.unisb.cs.st.javaslicer;

public class IllegalParameterException extends Exception {

    private static final long serialVersionUID = 1624276143822444877L;

    public IllegalParameterException() {
        super();
    }

    public IllegalParameterException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public IllegalParameterException(final String message) {
        super(message);
    }

    public IllegalParameterException(final Throwable cause) {
        super(cause);
    }

}
