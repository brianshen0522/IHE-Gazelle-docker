package net.ihe.gazelle.user.management.commons.interlay.cipher;

/**
 * Exception thrown when an error occurs during MD5 hashing operations.
 */
public class MD5HashException extends RuntimeException{

    /**
     * Instantiates a new MD5 hash exception.
     * @param message the detail message
     */
    public MD5HashException(String message) {
        super(message);
    }
}
