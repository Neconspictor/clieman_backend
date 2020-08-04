package de.necon.clieman_backend.exception;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class ServiceError extends RuntimeException {

    private List<String> errors;

    public ServiceError(List<String> errors) {
        this((String)null);
        addErrors(errors);

    }

    public ServiceError(List<String> errors, Throwable cause) {
        this((String)null, cause);
        addErrors(errors);

    }

    public ServiceError(String message) {
        this(message, null);
    }

    public ServiceError(String message, Throwable t) {
        super(message, t);
        this.errors = new ArrayList<>();

        addError(message);
    }

    public void addError(String error) {
        if (error != null && !error.isBlank()) {
            this.errors.add(error);
        }
    }

    public void addErrors(@NotNull List<String> errors) {
        errors.forEach(error -> addError(error));
    }

    public List<String> getErrors() {
        return List.copyOf(this.errors);
    }
}