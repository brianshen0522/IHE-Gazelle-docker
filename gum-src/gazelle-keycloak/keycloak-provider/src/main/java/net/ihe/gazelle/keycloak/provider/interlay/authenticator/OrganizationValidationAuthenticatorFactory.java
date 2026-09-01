package net.ihe.gazelle.keycloak.provider.interlay.authenticator;

import jakarta.persistence.EntityManager;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationLookupService;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationLookupDAO;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationLookupServiceImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.organization.OrganizationLookupDAOImpl;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for the OrganizationValidationAuthenticator.
 * <p>
 * This factory registers the authenticator in Keycloak and provides its configuration.
 */
public class OrganizationValidationAuthenticatorFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "organization-validation-auth";
    private static final String DISPLAY_TYPE = "Organization Validation (Gazelle)";
    private static final String REFERENCE_CATEGORY = "org.keycloak.authentication.AuthenticationFlowCallbackFactory";
    private static final String HELP_TEXT = "Validates that the user has a valid organization assigned in the GUM database. " +
            "This authenticator ensures organization consistency and prevents login if the organization is not found.";

    @Override
    public String getDisplayType() {
        return DISPLAY_TYPE;
    }

    @Override
    public String getReferenceCategory() {
        return REFERENCE_CATEGORY;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[]{
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.CONDITIONAL,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return HELP_TEXT;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return new ArrayList<>();
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class, "gum-store").getEntityManager();
        OrganizationLookupDAO organizationLookupDAO = new OrganizationLookupDAOImpl(em);
        OrganizationLookupService organizationLookupService = new OrganizationLookupServiceImpl(organizationLookupDAO);
        return new OrganizationValidationAuthenticator(organizationLookupService);
    }

    @Override
    public void init(org.keycloak.Config.Scope config) {
        // No initialization needed
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // No post-initialization needed
    }

    @Override
    public void close() {
        // Nothing to close
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}

