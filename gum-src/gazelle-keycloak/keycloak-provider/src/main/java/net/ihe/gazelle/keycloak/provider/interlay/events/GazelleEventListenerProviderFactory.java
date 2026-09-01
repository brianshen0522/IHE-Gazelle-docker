package net.ihe.gazelle.keycloak.provider.interlay.events;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class GazelleEventListenerProviderFactory implements EventListenerProviderFactory {
    @Override
    public GazelleEventListenerProvider create(KeycloakSession keycloakSession) {
        return new GazelleEventListenerProvider(keycloakSession);
    }

    @Override
    public void init(Config.Scope scope) {
        // Nothing to initialize
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        // Nothing to do after initialization
    }

    @Override
    public void close() {
        // Nothing to close
    }

    @Override
    public String getId() {
        return null;
    }
}
