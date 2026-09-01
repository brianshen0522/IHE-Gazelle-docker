package net.ihe.gazelle.keycloak.core.interlay.identity;

import net.ihe.gazelle.security.business.GazelleIdentity;

import java.security.Principal;
import java.util.Set;

import static net.ihe.gazelle.user.management.commons.application.user.edit.UserEditServiceImpl.KEYCLOAK_ADMIN;

/**
 * A simple implementation of the GazelleIdentity interface for Keycloak. This class represents a Keycloak admin user with a fixed ID, name, and group membership.
 */
public class KeycloakIdentity implements GazelleIdentity {

    /**
     * Default constructor for KeycloakIdentity. This constructor does not perform any initialization, as the identity information is hardcoded in the methods of this class.
     */
    public KeycloakIdentity() {
        // Nothing to initialize
    }

    @Override
    public String getId() {
        return "keycloakID";
    }

    @Override
    public String getName() {
        return "Keycloak admin";
    }

    @Override
    public Set<String> getGroups() {
        return Set.of(KEYCLOAK_ADMIN);
    }

    @Override
    public String getOrganizationGroup() { return null; }

    @Override
    public String getOrganizationId() { return null; }

    @Override
    public Principal getPrincipal() {
        return null;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public boolean hasGroup(String groupName) {
        return getGroups().contains(groupName);
    }
}
