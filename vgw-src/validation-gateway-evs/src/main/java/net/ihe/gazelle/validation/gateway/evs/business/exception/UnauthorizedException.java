package net.ihe.gazelle.validation.gateway.evs.business.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
