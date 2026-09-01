package net.ihe.gazelle.user.management.commons.interlay.exceptions;

/**
 * Exception thrown when an error occurs in the TM organization management process.
 */
public class TMOrganizationException extends RuntimeException {

    /**
     * Creates a new TMOrganizationException with the specified message.
     * @param message the detail message explaining the reason for the exception
     */
    public TMOrganizationException(String message) {
        super(message);
    }

    /**
     * Creates a new TMOrganizationException with the specified message and cause.
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause of the exception
     */
    public TMOrganizationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new TMOrganizationException with the specified cause.
     * @param cause the cause of the exception
     */
    public TMOrganizationException(Throwable cause) {
        super(cause);
    }
}
