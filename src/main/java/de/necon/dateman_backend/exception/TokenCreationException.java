package de.necon.dateman_backend.exception;

public class TokenCreationException extends RuntimeException {

    public TokenCreationException(String message) {
        this(message, null);
    }

    public TokenCreationException(String message, Throwable t) {
        super(message, t);
    }
}