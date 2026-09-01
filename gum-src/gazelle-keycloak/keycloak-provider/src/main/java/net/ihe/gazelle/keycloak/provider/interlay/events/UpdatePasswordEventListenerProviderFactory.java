package net.ihe.gazelle.keycloak.provider.interlay.events;

import org.keycloak.models.KeycloakSession;

public class UpdatePasswordEventListenerProviderFactory extends GazelleEventListenerProviderFactory {

    @Override
    public UpdatePasswordEventListenerProvider create(KeycloakSession keycloakSession) {
        return new UpdatePasswordEventListenerProvider(keycloakSession);
    }

    @Override
    public String getId() {
        return "gzl-update-password";
    }
}
