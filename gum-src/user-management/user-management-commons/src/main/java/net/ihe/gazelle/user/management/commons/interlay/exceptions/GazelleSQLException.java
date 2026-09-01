package net.ihe.gazelle.user.management.commons.interlay.exceptions;

/**
 * Gazelle SQL query exception.
 */
public class GazelleSQLException extends RuntimeException {

    /**
     * Creates a new GazelleSQLException with the given message.
     * @param message the detail message of the exception
     */
    public GazelleSQLException(String message) {
        super(message);
    }

    /**
     * Creates a new GazelleSQLException with the given message and cause.
     * @param message the detail message of the exception
     * @param cause the cause of the exception
     */
    public GazelleSQLException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new GazelleSQLException with the given cause.
     * @param cause the cause of the exception
     */
    public GazelleSQLException(Throwable cause) {
        super(cause);
    }
}
