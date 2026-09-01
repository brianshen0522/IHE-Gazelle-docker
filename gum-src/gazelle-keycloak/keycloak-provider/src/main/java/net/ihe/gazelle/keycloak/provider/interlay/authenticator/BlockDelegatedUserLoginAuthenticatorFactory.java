package net.ihe.gazelle.keycloak.provider.interlay.authenticator;

import jakarta.persistence.EntityManager;
import net.ihe.gazelle.keycloak.core.application.service.DelegatedOrganizationServiceImpl;
import net.ihe.gazelle.keycloak.core.interlay.dao.OrganizationManagementDAOKeycloak;
import net.ihe.gazelle.keycloak.core.interlay.publisher.OrganizationEventPublisherBootstrap;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.AuthzImpl;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.user.management.api.application.organization.DelegatedOrganizationService;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationManagementService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.commons.application.organization.delegation.DelegatedOrganizationDAO;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationEventPublisher;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationManagementDAO;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationManagementServiceImpl;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupServiceImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.organization.DelegatedOrganizationDAOImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserLookupDAOImpl;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordFormFactory;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating BlockDelegatedUserLoginAuthenticator instances.
 *
 * This factory extends the UsernamePasswordFormFactory to create authenticators
 * that block login attempts for users belonging to delegated organizations.
 * It sets up the necessary services and dependencies required for checking
 * user and organization delegation status.
 *
 */
public class BlockDelegatedUserLoginAuthenticatorFactory extends UsernamePasswordFormFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

    private static final Logger LOG = LoggerFactory.getLogger(BlockDelegatedUserLoginAuthenticatorFactory.class);

    /**
     * The provider identifier for this authenticator factory.
     */
    public static final String PROVIDER_ID = "gazelle-delegated-authenticator";
    private Authz authz;
    private OrganizationEventPublisher organizationEventPublisher;

    /**
     * Default constructor.
     *
     * Creates a new BlockDelegatedUserLoginAuthenticatorFactory instance.
     */
    public BlockDelegatedUserLoginAuthenticatorFactory() {
        // Default constructor
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        initRequiredServiceIfNecessary();
        EntityManager em = session.getProvider(JpaConnectionProvider.class, "gum-store").getEntityManager();
        UserLookupDAO userLookupDAO = new UserLookupDAOImpl(em);
        DelegatedOrganizationService delegatedOrganizationService = getDelegatedOrganizationService(em);
        UserLookupService userLookupService = new UserLookupServiceImpl(userLookupDAO,authz);
        return new BlockDelegatedUserLoginAuthenticator(delegatedOrganizationService, userLookupService);
    }

    /**
     * We don't want to init publisher each time we log in
     * To avoid this with keep instance if possible
     */
    private void initRequiredServiceIfNecessary() {
        if (organizationEventPublisher == null)
            organizationEventPublisher = OrganizationEventPublisherBootstrap.fromEnvironment(LOG);
        if (authz == null)
            authz = new AuthzImpl(new PermissionStoreSPIProvider());
    }

    private DelegatedOrganizationService getDelegatedOrganizationService(EntityManager em) {
        DelegatedOrganizationDAO delegatedOrganizationRestClient = new DelegatedOrganizationDAOImpl(em);
        OrganizationManagementDAO organizationRegistrationDAO = new OrganizationManagementDAOKeycloak(em);
        OrganizationManagementService organizationManagementService = new OrganizationManagementServiceImpl(organizationRegistrationDAO, null, authz);
        return new DelegatedOrganizationServiceImpl(delegatedOrganizationRestClient, organizationManagementService, organizationEventPublisher);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getReferenceCategory() {
        return PasswordCredentialModel.TYPE;
    }

    @Override
    public String getDisplayType() {
        return "Block user if delegated (Gazelle)";
    }

    @Override
    public String getHelpText() {
        return "(Gazelle) Detect if the user is a delegated user. If yes, blocks the local login.";
    }
}
