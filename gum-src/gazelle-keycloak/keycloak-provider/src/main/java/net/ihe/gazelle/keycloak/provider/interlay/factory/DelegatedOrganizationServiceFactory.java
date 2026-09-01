package net.ihe.gazelle.keycloak.provider.interlay.factory;

import jakarta.persistence.EntityManager;
import net.ihe.gazelle.keycloak.core.application.service.DelegatedOrganizationServiceImpl;
import net.ihe.gazelle.keycloak.core.interlay.dao.OrganizationManagementDAOKeycloak;
import net.ihe.gazelle.keycloak.core.interlay.publisher.OrganizationEventPublisherBootstrap;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.AuthzImpl;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.user.management.api.application.organization.DelegatedOrganizationService;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationManagementService;
import net.ihe.gazelle.user.management.commons.application.organization.delegation.DelegatedOrganizationDAO;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationEventPublisher;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationManagementDAO;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationManagementServiceImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.organization.DelegatedOrganizationDAOImpl;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelegatedOrganizationServiceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DelegatedOrganizationServiceFactory.class);

    private final KeycloakSession keycloakSession;

    public DelegatedOrganizationServiceFactory(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }

    public DelegatedOrganizationService getOrganizationDelegationService() {
        Authz authz = new AuthzImpl(new PermissionStoreSPIProvider());
        EntityManager entityManager = keycloakSession.getProvider(JpaConnectionProvider.class, "gum-store").getEntityManager();
        DelegatedOrganizationDAO organizationDelegationDAO = new DelegatedOrganizationDAOImpl(entityManager);
        OrganizationManagementDAO organizationRegistrationDAO = new OrganizationManagementDAOKeycloak(entityManager);
        OrganizationEventPublisher organizationEventPublisher = OrganizationEventPublisherBootstrap.fromEnvironment(LOG);
        OrganizationManagementService organizationManagementService = new OrganizationManagementServiceImpl(organizationRegistrationDAO, organizationEventPublisher, authz);
        return new DelegatedOrganizationServiceImpl(organizationDelegationDAO, organizationManagementService, organizationEventPublisher);
    }
}
