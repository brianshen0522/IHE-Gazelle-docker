package net.ihe.gazelle.keycloak.provider.interlay.authenticator;

import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.keycloak.provider.interlay.GazelleUserModelAdapter;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationLookupService;
import net.ihe.gazelle.user.management.api.domain.user.User;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authenticator that validates that a user has a valid organization assigned.
 * <p>
 * This authenticator is executed during the authentication flow and ensures that:
 * 1. The user belongs to at least one group (organization)
 * 2. The organization exists in the GUM database
 * <p>
 * If validation fails, the authentication process is immediately stopped and an error is presented to the user.
 */
public class OrganizationValidationAuthenticator implements Authenticator {

    private static final Logger log = LoggerFactory.getLogger(OrganizationValidationAuthenticator.class);
    private final OrganizationLookupService organizationLookupService;

    public OrganizationValidationAuthenticator(OrganizationLookupService organizationLookupService) {
        this.organizationLookupService = organizationLookupService;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel userContext = context.getUser();
        if (userContext == null) {
            // User not yet identified, proceed
            context.success();
            return;
        }

        try {
            // Use Keycloak user manager: it resolves federated users via the configured storage provider.
            GazelleUserModelAdapter user = (GazelleUserModelAdapter) context.getSession().users().getUserById(context.getRealm(), userContext.getId());
            if (user != null) {
                context.setUser(user);
            } else {
                throw new IllegalStateException("Unable to load user from federation provider.");
            }

            User userGazelle = user.getUser();
            String organizationId = userGazelle.getOrganizationId();
            if (organizationId == null) {
                throw new IllegalStateException("The user does not belong to any organization. Please contact support.");
            }
            validateUserOrganization(organizationId);
            context.success();
        } catch (Exception e) {
            log.error("Organization validation failed for user: {}", userContext.getEmail(), e);
            failAuthentication(context, e.getMessage());
        }
    }

    /**
     * Validates that the user has a valid organization.
     *
     * @param organizationId the ID of the organization to validate
     * @throws IllegalStateException if the validation fails
     */
    private void validateUserOrganization(String organizationId) {
        // Verify that the organization exists in the GUM database
        try {
            if (organizationId == null) {
                String errorMessage = "No organization found for the user.";
                log.error(errorMessage);
                throw new IllegalStateException("The user does not belong to any organization. Please contact support.");
            }

            organizationLookupService.getOrganizationById(organizationId);
        } catch (Exception e) {
            String errorMessage = String.format("Organization not found in GUM database for id: %s", organizationId);
            log.error(errorMessage, e);
            throw new IllegalStateException("The user belongs to an unknown organization. Please contact support.");
        }
    }

    /**
     * Fails the authentication with an error page.
     *
     * @param context the authentication flow context
     */
    private void failAuthentication(AuthenticationFlowContext context, String message) {
        Response challengeResponse = context.form()
                .setError(message)
                .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR);
        context.failure(AuthenticationFlowError.INTERNAL_ERROR, challengeResponse);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // No action needed for this authenticator
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // No required actions
    }

    @Override
    public void close() {
        // Nothing to close
    }
}


