package de.necon.dateman_backend.exception;

import java.util.List;

public class ServerErrorList extends RuntimeException {

    private List<String> errors;

    public ServerErrorList(List<String> errors) {
        this(errors, null);
    }

    public ServerErrorList(List<String> errors, Throwable t) {
        super(t);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}