package net.ihe.gazelle.keycloak.provider.interlay.events;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

public class GazelleEventListenerProvider implements EventListenerProvider {

    private final KeycloakSession keycloakSession;


    public GazelleEventListenerProvider(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }

    public KeycloakSession getKeycloakSession() {
        return keycloakSession;
    }


    @Override
    public void onEvent(Event event) {
        // Nothing to do on event
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        // Nothing to do on admin event
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
