package net.ihe.gazelle.user.management.core.interlay.factory;

import jakarta.enterprise.inject.Produces;
import net.ihe.gazelle.user.management.commons.application.user.login.HashPasswordServiceProvider;
import net.ihe.gazelle.user.management.commons.interlay.provider.HashPasswordServiceSPIProvider;

/**
 * Factory class for producing instances of HashPasswordServiceProvider using SPI.
 */
public class HashPasswordServiceSPIFactory {

    /** Default constructor for HashPasswordServiceSPIFactory */
    public HashPasswordServiceSPIFactory() {
        // Nothing to do here
    }

    /**
     * Produces an instance of HashPasswordServiceProvider using SPI.
     * @return a new instance of HashPasswordServiceSPIProvider
     */
    @Produces
    public HashPasswordServiceProvider getHashPasswordService() {
        return new HashPasswordServiceSPIProvider();
    }
}
