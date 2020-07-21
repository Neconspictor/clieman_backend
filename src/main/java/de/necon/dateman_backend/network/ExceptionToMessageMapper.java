package de.necon.dateman_backend.network;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static de.necon.dateman_backend.config.ServiceErrorMessages.INTERNAL_SERVER_ERROR;

public class ExceptionToMessageMapper {

    private Map<Class<? extends Exception>, String> exceptionToCode = new HashMap<>();

    public String mapExceptionToMessageCode(Exception e) {
        return exceptionToCode.getOrDefault(e.getClass(), INTERNAL_SERVER_ERROR);
    }

    public ExceptionToMessageMapper register(Class<? extends Exception> exceptionClass, String message) {
        return register(new MessageCode(exceptionClass, message));
    }

    public ExceptionToMessageMapper register(MessageCode messageCode) {
        exceptionToCode.put(messageCode.getExceptionClass(), messageCode.getMessage());
        return this;
    }

    public ExceptionToMessageMapper registerAll(Collection<MessageCode> collection) {
        collection.forEach(messageCode -> register(messageCode));
        return this;
    }
}
