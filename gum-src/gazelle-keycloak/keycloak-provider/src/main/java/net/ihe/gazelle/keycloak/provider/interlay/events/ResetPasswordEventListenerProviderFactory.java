package net.ihe.gazelle.keycloak.provider.interlay.events;

import org.keycloak.models.KeycloakSession;

public class ResetPasswordEventListenerProviderFactory extends GazelleEventListenerProviderFactory {

    @Override
    public ResetPasswordEventListenerProvider create(KeycloakSession session) {
        return new ResetPasswordEventListenerProvider(session);
    }

    @Override
    public String getId() {
        return "gzl-reset-password-logout";
    }
}
