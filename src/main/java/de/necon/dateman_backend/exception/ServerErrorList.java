package de.necon.dateman_backend.exception;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public class ServerErrorList extends RuntimeException {

    private List<String> errors;

    public ServerErrorList(List<String> errors) {
        this(errors, null);
    }

    public ServerErrorList(ConstraintViolationException e) {
        super();
        this.errors = new ArrayList<>();

        for(var violation : e.getConstraintViolations()) {
            this.errors.add(violation.getMessageTemplate());
        }
    }

    public ServerErrorList(List<String> errors, Throwable t) {
        super(t);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}