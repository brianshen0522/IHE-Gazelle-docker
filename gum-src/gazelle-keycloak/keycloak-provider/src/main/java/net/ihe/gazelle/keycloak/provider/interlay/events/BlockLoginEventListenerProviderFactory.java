package net.ihe.gazelle.keycloak.provider.interlay.events;

import org.keycloak.models.KeycloakSession;

public class BlockLoginEventListenerProviderFactory extends GazelleEventListenerProviderFactory {

    @Override
    public BlockLoginEventListenerProvider create(KeycloakSession keycloakSession) {
        return new BlockLoginEventListenerProvider(keycloakSession);
    }

    @Override
    public String getId() {
        return "gzl-block-login";
    }
}
