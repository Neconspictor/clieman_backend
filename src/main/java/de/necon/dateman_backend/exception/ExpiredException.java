package de.necon.dateman_backend.exception;

public class ExpiredException extends RuntimeException {

    public ExpiredException(String message) {
        this(message, null);
    }

    public ExpiredException(String message, Throwable t) {
        super(message, t);
    }
}

