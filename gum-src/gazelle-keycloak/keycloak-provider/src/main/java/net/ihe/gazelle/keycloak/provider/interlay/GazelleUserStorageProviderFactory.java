package net.ihe.gazelle.keycloak.provider.interlay;

import jakarta.persistence.EntityManager;
import net.ihe.gazelle.keycloak.core.interlay.configuration.ApplicationConfigImpl;
import net.ihe.gazelle.keycloak.core.interlay.dao.OrganizationManagementDAOKeycloak;
import net.ihe.gazelle.keycloak.core.interlay.email.UserEditEmailManagerMockImpl;
import net.ihe.gazelle.keycloak.core.interlay.email.UserRegistrationEmailManagerMockImpl;
import net.ihe.gazelle.keycloak.core.interlay.publisher.OrganizationEventPublisherBootstrap;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.AuthzImpl;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.application.group.GroupService;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationLookupService;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationManagementService;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.api.application.user.edit.UserEditService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.api.application.user.registration.ConsentService;
import net.ihe.gazelle.user.management.api.application.user.registration.UserRegistrationService;
import net.ihe.gazelle.user.management.commons.application.group.GroupDAO;
import net.ihe.gazelle.user.management.commons.application.group.GroupServiceImpl;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationLookupDAO;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationLookupServiceImpl;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationManagementDAO;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationEventPublisher;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationManagementServiceImpl;
import net.ihe.gazelle.user.management.commons.application.user.delegation.UserDelegationDAO;
import net.ihe.gazelle.user.management.commons.application.user.delegation.UserDelegationServiceImpl;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditDAO;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditEmailManager;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditServiceImpl;
import net.ihe.gazelle.user.management.commons.application.user.login.HashPasswordServiceProvider;
import net.ihe.gazelle.user.management.commons.application.user.login.UserLoginDAO;
import net.ihe.gazelle.user.management.commons.application.user.login.UserLoginServiceImpl;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupServiceImpl;
import net.ihe.gazelle.user.management.commons.application.user.registration.*;
import net.ihe.gazelle.user.management.commons.interlay.dao.ConsentDAOImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.GroupDAOImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.organization.OrganizationLookupDAOImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.*;
import net.ihe.gazelle.user.management.commons.interlay.provider.HashPasswordServiceSPIProvider;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.storage.UserStorageProviderFactory;
import org.slf4j.Logger;

import java.util.List;

public class GazelleUserStorageProviderFactory implements UserStorageProviderFactory<GazelleUserStorageProvider> {

    private final Logger log = org.slf4j.LoggerFactory.getLogger(GazelleUserStorageProviderFactory.class);
    private UserRegistrationEmailManager userRegistrationEmailManager;
    private UserEditEmailManager userEditEmailManager;
    private HashPasswordServiceProvider hashPasswordServiceProvider;
    private ApplicationConfig applicationConfig;
    private Authz authz;
    private OrganizationEventPublisher organizationEventPublisher;

    @Override
    public void init(Config.Scope config) {
        applicationConfig = new ApplicationConfigImpl();
        userEditEmailManager = new UserEditEmailManagerMockImpl();
        authz = new AuthzImpl(new PermissionStoreSPIProvider());
        hashPasswordServiceProvider = new HashPasswordServiceSPIProvider();
        userRegistrationEmailManager = new UserRegistrationEmailManagerMockImpl();
        organizationEventPublisher = OrganizationEventPublisherBootstrap.fromEnvironment(log);
    }

    @Override
    public GazelleUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class, "gum-store").getEntityManager();

        // Instantiate DAOs
        OrganizationLookupDAO organizationLookupDAO = new OrganizationLookupDAOImpl(em);
        UserLookupDAO userLookupDAO = new UserLookupDAOImpl(em);
        UserLookupService userLookupService = new UserLookupServiceImpl(userLookupDAO, authz);
        UserEditDAO userEditDAO = new UserEditDAOImpl(em);
        GroupDAO groupDAO = new GroupDAOImpl(em);
        UserLoginDAO userLoginDAO = new UserLoginDAOImpl(em);
        UserRegistrationDAO userRegistrationDAO = new UserRegistrationDAOImpl(em);
        OrganizationManagementDAO organizationRegistrationDAO = new OrganizationManagementDAOKeycloak(em);
        ConsentDAO consentDAO = new ConsentDAOImpl(em);
        ConsentService consentService = new ConsentServiceImpl(consentDAO);
        UserDelegationDAO userDelegationDAO = new UserDelegationDAOImpl(em);

        // Instantiate services
        OrganizationLookupService organizationLookupService = new OrganizationLookupServiceImpl(organizationLookupDAO);
        UserDelegationService userDelegationService = new UserDelegationServiceImpl(userDelegationDAO, userLookupDAO, userEditDAO);
        GroupService groupService = new GroupServiceImpl(authz, groupDAO, userLookupDAO);
        net.ihe.gazelle.user.management.api.application.user.login.UserLoginService userLoginService = new UserLoginServiceImpl(userLoginDAO, hashPasswordServiceProvider);
        UserEditService userEditService = new UserEditServiceImpl(userEditDAO, hashPasswordServiceProvider, authz,
                userEditEmailManager, userLookupService, userDelegationService, organizationLookupService);
        OrganizationManagementService organizationManagementService = new OrganizationManagementServiceImpl(organizationRegistrationDAO, organizationEventPublisher, authz);
        UserRegistrationService userRegistrationService = new UserRegistrationServiceImpl(userEditService, organizationLookupService,
                organizationManagementService, consentService, userRegistrationDAO, applicationConfig, userRegistrationEmailManager, authz);

        return new GazelleUserStorageProvider(session, model, userLookupService, userLoginService, userEditService,
                userRegistrationService, groupService, organizationManagementService, userDelegationService);
    }

    @Override
    public String getId() {
        log.debug("getId()");
        return "gazelle-user-provider";
    }


    // Configuration support methods
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of();
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel componentModel) {
        // No implementation
    }

    @Override
    public void onUpdate(KeycloakSession session, RealmModel realm, ComponentModel oldModel, ComponentModel newModel) {
        log.trace("onUpdate()");
    }

    @Override
    public void onCreate(KeycloakSession session, RealmModel realm, ComponentModel model) {
        log.trace("onCreate()");
    }
}
