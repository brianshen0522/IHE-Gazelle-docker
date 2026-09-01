package net.ihe.gazelle.user.management.api.application.user.registration;

import net.ihe.gazelle.user.management.api.application.GazelleServiceException;

/**
 * Exception thrown when user registration fails.
 */
public class UserRegistrationException extends GazelleServiceException {

    /**
     * Creates the exception with a message.
     *
     * @param message error message
     */
    public UserRegistrationException(String message) {
            super(message);
        }
    /**
     * Creates the exception with a message and cause.
     *
     * @param message error message
     * @param cause root cause
     */
    public UserRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
