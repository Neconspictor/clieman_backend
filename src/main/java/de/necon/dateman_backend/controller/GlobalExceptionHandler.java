package de.necon.dateman_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorList  processConstraintViolationException(ConstraintViolationException e) {

        var errors = e.getConstraintViolations().stream().map((constraint)->{
            return constraint.getMessage();
        }).collect(Collectors.toUnmodifiableList());

        return new ErrorList(errors);
    }


    public static class ErrorList {
        private List<String> errors;

        private ErrorList(List<String> errors) {
            this.errors = errors;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }
    }
}