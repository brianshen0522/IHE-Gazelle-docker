package net.ihe.gazelle.user.management.core.interlay.transactional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationManagementService;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationEventPublisher;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationManagementDAO;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationManagementServiceImpl;

/**
 * Transactional implementation of OrganizationManagementService, which delegates the calls to a non-transactional implementation
 */
@RequestScoped
@Default
public class OrganizationManagementServiceTransactional implements OrganizationManagementService {

    private final OrganizationManagementService organizationManagementService;

    @Inject
    public OrganizationManagementServiceTransactional(OrganizationManagementDAO organizationRegistrationDAO, OrganizationEventPublisher organizationManagementEventPublisher, Authz authz) {
        this.organizationManagementService = new OrganizationManagementServiceImpl(organizationRegistrationDAO, organizationManagementEventPublisher, authz);
    }

    @Override
    @Transactional
    public Organization createOrganization(Organization organization, GazelleIdentity identity) {
        return organizationManagementService.createOrganization(organization, identity);
    }

    @Override
    public boolean isOrganizationAlreadyExist(Organization organization) {
        return organizationManagementService.isOrganizationAlreadyExist(organization);
    }

    @Override
    @Transactional
    public void joinOrganization(String userId, String organizationId) {
        organizationManagementService.joinOrganization(userId, organizationId);
    }

    @Override
    @Transactional
    public Organization updateOrganization(String organizationId, Organization organization, GazelleIdentity identity) {
        return organizationManagementService.updateOrganization(organizationId, organization, identity);
    }

    @Override
    @Transactional
    public void archiveOrganization(String organizationId, GazelleIdentity identity) {
        organizationManagementService.archiveOrganization(organizationId, identity);
    }
}
