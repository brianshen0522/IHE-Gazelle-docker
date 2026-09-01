package net.ihe.gazelle.user.management.commons.application.user.login;

import net.ihe.gazelle.user.management.api.domain.user.Credentials;

/**
 * Component used to perform hash operations on a password
 */
public interface HashPasswordService {

    /**
     * Hash the password with the given parameters
     *
     * @param password the password to hash
     * @return the hashed password
     */
    Credentials hash(String password);

    /**
     * Verify if the password is the same as the hashed value
     *
     * @param credentials the credentials to verify
     * @return is the password is corresponding to the hashed value
     * @throws IllegalArgumentException if one of the required parameters is null
     */
    boolean verify(Credentials credentials, String password);

    /**
     * Method name used to identify the hash method for SPI context
     * @return the name of the hash method
     */
    String getHashMethodName();
}
