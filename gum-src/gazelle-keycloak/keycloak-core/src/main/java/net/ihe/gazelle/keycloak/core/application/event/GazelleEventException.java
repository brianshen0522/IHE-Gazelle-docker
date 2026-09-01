package net.ihe.gazelle.keycloak.core.application.event;

/**
 * Custom exception class for handling errors related to Gazelle events in the Keycloak application.
 */
public class GazelleEventException extends RuntimeException {

    /**
     * Constructs a new GazelleEventException with the specified detail message.
     * @param message the detail message explaining the reason for the exception
     */
    public GazelleEventException(String message) {
        super(message);
    }

    /**
     * Constructs a new GazelleEventException with the specified detail message and cause.
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause of the exception, which can be used for debugging and error handling purposes
     */
    public GazelleEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
