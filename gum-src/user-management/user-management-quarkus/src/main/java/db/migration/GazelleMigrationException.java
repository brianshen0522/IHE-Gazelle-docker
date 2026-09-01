package db.migration;

/**
 * Exception raised when the Gazelle migration fails.
 */
public class GazelleMigrationException extends RuntimeException {

    /**
     * Creates the exception with a message.
     *
     * @param message error message
     */
    public GazelleMigrationException(String message) {
        super(message);
    }

    /**
     * Creates the exception with a message and a cause.
     *
     * @param message error message
     * @param cause underlying cause
     */
    public GazelleMigrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
