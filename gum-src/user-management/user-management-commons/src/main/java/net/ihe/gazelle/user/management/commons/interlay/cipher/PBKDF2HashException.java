package net.ihe.gazelle.user.management.commons.interlay.cipher;

/**
 * Exception thrown when an error occurs during the PBKDF2 hashing process.
 */
public class PBKDF2HashException extends RuntimeException{

    /**
     * Instantiates a new PBKDF2 hash exception.
     * @param message the detail message
     */
    public PBKDF2HashException(String message) {
        super(message);
    }

    /**
     * Instantiates a new PBKDF2 hash exception.
     * @param message the detail message
     * @param cause the cause
     */
    public PBKDF2HashException(String message, Throwable cause) {
        super(message, cause);
    }
}
