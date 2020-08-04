package de.necon.clieman_backend.network;

public class MessageCode {

    private final Class<? extends Exception> exceptionClass;
    private final String message;

    public MessageCode(Class<? extends Exception> exceptionClass, String message) {
        this.exceptionClass = exceptionClass;
        this.message = message;
    }

    public Class<? extends Exception> getExceptionClass() {
        return exceptionClass;
    }

    public String getMessage() {
        return message;
    }
}
