package de.necon.dateman_backend.exception;

public class UserNotRegisteredException extends RuntimeException {

    public UserNotRegisteredException(String message) {
        this(message, null);
    }

    public UserNotRegisteredException(String message, Throwable t) {
        super(message, t);
    }
}