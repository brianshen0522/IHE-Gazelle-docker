package net.ihe.gazelle.user.management.commons.application.user.registration;

/**
 * Exception thrown when an error occurs in the activation email management process.
 */
public class ActivationEmailManagerException extends RuntimeException {

    /**
     * Creates a new ActivationEmailManagerException with the specified message.
     * @param message the detail message explaining the reason for the exception
     */
    public ActivationEmailManagerException(String message) {
        super(message);
    }

    /**
     * Creates a new ActivationEmailManagerException with the specified message and cause.
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause of the exception
     */
    public ActivationEmailManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}
