package net.ihe.gazelle.keycloak.provider.interlay.events;

import org.keycloak.models.KeycloakSession;

/**
 * Factory for creating AccountCreationEventListenerProvider instances.
 *
 * This factory extends the base GazelleEventListenerProviderFactory to provide
 * creation of event listener providers specifically designed to handle account
 * creation events in the Gazelle User Management system.
 *
 */
public class AccountCreationEventListenerProviderFactory extends GazelleEventListenerProviderFactory {

    /**
     * Creates a new AccountCreationEventListenerProvider instance.
     *
     * @param keycloakSession the Keycloak session
     * @return a new AccountCreationEventListenerProvider instance
     */
    @Override
    public AccountCreationEventListenerProvider create(KeycloakSession keycloakSession) {
        return new AccountCreationEventListenerProvider(keycloakSession);
    }

    /**
     * Gets the unique identifier for this provider factory.
     *
     * @return the provider ID
     */
    @Override
    public String getId() {
        return "gzl-creation-account";
    }
}
