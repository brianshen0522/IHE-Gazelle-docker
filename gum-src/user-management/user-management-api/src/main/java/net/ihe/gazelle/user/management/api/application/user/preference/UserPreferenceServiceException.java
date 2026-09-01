package net.ihe.gazelle.user.management.api.application.user.preference;

/**
 * Exception thrown when user preference operations fail.
 */
public class UserPreferenceServiceException extends RuntimeException {

    /**
     * Creates a new exception with no message.
     */
    public UserPreferenceServiceException() {
        super();
    }

    /**
     * Creates a new exception with a message.
     *
     * @param message error message
     */
    public UserPreferenceServiceException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with a message and cause.
     *
     * @param message error message
     * @param cause root cause
     */
    public UserPreferenceServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception with a cause.
     *
     * @param cause root cause
     */
    public UserPreferenceServiceException(Throwable cause) {
        super(cause);
    }
}
