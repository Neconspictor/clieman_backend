package de.necon.dateman_backend.controller;

import de.necon.dateman_backend.network.ErrorListDto;
import de.necon.dateman_backend.exception.ServiceError;
import de.necon.dateman_backend.util.MessageExtractor;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static de.necon.dateman_backend.config.ServiceErrorMessages.MALFORMED_DATA;

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


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ErrorListDto handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        return new ErrorListDto(List.of(MALFORMED_DATA));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ErrorListDto handleNotReadableException (
            HttpMessageNotReadableException ex) {
        return new ErrorListDto(List.of(MALFORMED_DATA));
    }
}