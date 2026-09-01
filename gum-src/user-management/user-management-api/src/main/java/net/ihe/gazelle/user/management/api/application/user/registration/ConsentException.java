package net.ihe.gazelle.user.management.api.application.user.registration;

/**
 * Exception thrown when there are issues related to user consent during registration.
 *
 * This runtime exception is used to indicate problems that occur when processing
 * user consent information, such as missing consent, invalid consent data, or
 * consent validation failures during the user registration process.
 *
 */
public class ConsentException extends RuntimeException {

    /**
     * Constructs a new ConsentException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public ConsentException(String message) {
        super(message);
    }

    /**
     * Constructs a new ConsentException with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause of this exception (which is saved for later retrieval)
     */
    public ConsentException(String message, Throwable cause) {
        super(message, cause);
    }
}
