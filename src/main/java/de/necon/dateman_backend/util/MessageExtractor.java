package de.necon.dateman_backend.util;

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
}