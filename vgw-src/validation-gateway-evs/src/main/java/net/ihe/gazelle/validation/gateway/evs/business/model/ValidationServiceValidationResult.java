package net.ihe.gazelle.validation.gateway.evs.business.model;

public class ValidationServiceValidationResult {

    private final boolean valid;
    private final String message;

    public ValidationServiceValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }
}
