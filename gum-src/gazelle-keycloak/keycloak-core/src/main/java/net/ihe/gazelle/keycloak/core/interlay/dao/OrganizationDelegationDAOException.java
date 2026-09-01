package net.ihe.gazelle.keycloak.core.interlay.dao;


/**
 * Exception for errors during organization delegation DAO operations in Gazelle User Management.
 */
public class OrganizationDelegationDAOException extends RuntimeException {
    /**
     * Constructs a new exception with the specified detail message.
     * @param message the detail message
     */
    public OrganizationDelegationDAOException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause.
     * @param cause the cause
     */
    public OrganizationDelegationDAOException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public OrganizationDelegationDAOException(String message, Throwable cause) {
        super(message, cause);
    }
}