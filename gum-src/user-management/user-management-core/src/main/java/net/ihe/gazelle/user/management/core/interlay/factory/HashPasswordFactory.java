package net.ihe.gazelle.user.management.core.interlay.factory;

import jakarta.enterprise.inject.Produces;
import net.ihe.gazelle.user.management.commons.application.user.login.HashPasswordService;
import net.ihe.gazelle.user.management.commons.interlay.cipher.PBKDF2HashService;

/**
 * Factory class for creating instances of HashPasswordService.
 */
public class HashPasswordFactory {

    /** Default constructor for HashPasswordFactory. */
    public HashPasswordFactory() {
        // Nothing to initialize
    }

    /**
     * Produces an instance of HashPasswordService using PBKDF2 hashing algorithm.
     * @return a new instance of PBKDF2HashService
     */
    @Produces
    public HashPasswordService getHashPasswordService() {
        return new PBKDF2HashService();
    }
}
