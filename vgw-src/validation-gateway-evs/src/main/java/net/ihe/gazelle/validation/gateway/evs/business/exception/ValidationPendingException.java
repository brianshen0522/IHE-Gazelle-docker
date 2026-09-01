package net.ihe.gazelle.validation.gateway.evs.business.exception;

public class ValidationPendingException extends RuntimeException {

    public ValidationPendingException(String message) {
        super(message);
    }
}
