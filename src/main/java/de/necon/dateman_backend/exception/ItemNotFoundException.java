package de.necon.dateman_backend.exception;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(String message, Throwable t) {
        super(message, t);
    }
}
