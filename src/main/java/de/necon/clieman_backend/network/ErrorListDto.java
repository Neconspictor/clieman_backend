package de.necon.clieman_backend.network;

import java.util.ArrayList;
import java.util.List;

public class ErrorListDto {
    private List<String> errors;

    public ErrorListDto() {
        this.errors = new ArrayList<>();
    }

    public ErrorListDto(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}