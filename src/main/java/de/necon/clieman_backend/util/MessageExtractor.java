package de.necon.clieman_backend.util;

import org.springframework.dao.DataIntegrityViolationException;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility for extracting messages from exceptions.
 */
public class MessageExtractor {

    public static List<String> extract(ConstraintViolationException e) {
        return e.getConstraintViolations().stream().
                map(v -> v.getMessageTemplate()).
                collect(Collectors.toUnmodifiableList());
    }

    public static List<String> extract(DataIntegrityViolationException e) {
        return List.of(e.getMessage());
    }
}