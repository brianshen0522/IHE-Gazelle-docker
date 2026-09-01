package net.ihe.gazelle.user.management.api.application.user.edit;

import net.ihe.gazelle.user.management.api.application.GazelleServiceException;

/**
 * Exception thrown when a user edit operation fails.
 */
public class UserEditException extends GazelleServiceException {

    /**
     * Instantiates a new UserEditService exception.
     *
     * @param message message
     */
    public UserEditException(String message) {
        super(message);
    }

    /**
     * Instantiates a new UserEditService exception.
     *
     * @param message message
     * @param cause root cause
     */
    public UserEditException(String message, Throwable cause) {
        super(message, cause);
    }
}
