package net.ihe.gazelle.user.management.commons.application.exception;

/**
 * Gazelle DAO exception.
 */
public class GazelleDAOException extends RuntimeException {

    /**
     * Instantiates a new Gazelle query exception.
     * @param message message
     */
    public GazelleDAOException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Gazelle query exception.
     * @param cause cause
     */
    public GazelleDAOException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new Gazelle query exception.
     * @param message message
     * @param cause   cause
     */
    public GazelleDAOException(String message,Throwable cause) {
        super(message);
    }

}
