package de.necon.dateman_backend.controller;

import de.necon.dateman_backend.network.ErrorListDto;
import de.necon.dateman_backend.exception.ServiceError;
import de.necon.dateman_backend.util.MessageExtractor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorListDto processConstraintViolationException(ConstraintViolationException e) {
        return new ErrorListDto(MessageExtractor.extract(e));
    }

    @ExceptionHandler(ServiceError.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorListDto processServiceError(ServiceError e) {
        return new ErrorListDto(e.getErrors());
    }
}