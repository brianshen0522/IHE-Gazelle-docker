package net.ihe.gazelle.user.management.commons.application.organization.management;

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationManagementService;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.api.application.ConflictException;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;

import java.util.UUID;

import static net.ihe.gazelle.user.management.commons.application.permission.GUMPermissionStore.*;

public class OrganizationManagementServiceImpl implements OrganizationManagementService {

    private final OrganizationManagementDAO organizationManagementDAO;
    private final OrganizationEventPublisher organizationEventPublisher;
    private final Authz authz;

    public OrganizationManagementServiceImpl(OrganizationManagementDAO organizationManagementDAO, OrganizationEventPublisher organizationEventPublisher, Authz authz) {
        this.organizationManagementDAO = organizationManagementDAO;
        this.organizationEventPublisher = organizationEventPublisher;
        this.authz = authz;
    }

    @Override
    public Organization createOrganization(Organization organization, GazelleIdentity identity) {
        authz.assertAuthorized(identity, ORGANIZATION_CREATE);
        if (organization == null)
            throw new IllegalArgumentException("Organization cannot be null");
        if (organization.getName() == null || organization.getName().isEmpty())
            throw new IllegalArgumentException("Organization name cannot be null or empty");
        assertShortnameIsValid(organization.getShortname());
        if (isOrganizationAlreadyExist(organization)) {
            throw new ConflictException("Organization with same shortname or name already exists.");
        }
        Organization newOrga = new Organization()
                .setId(UUID.randomUUID().toString())
                .setName(organization.getName())
                .setShortname(organization.getShortname());

        organizationManagementDAO.createOrganization(newOrga);
        if (organizationEventPublisher != null)
            organizationEventPublisher.publishOrganizationCreateEvent(newOrga);
        return newOrga;
    }

    private static void assertShortnameIsValid(String shortname) {
        if (shortname == null || shortname.isEmpty())
            throw new IllegalArgumentException("Organization shortname cannot be null or empty");
        if (!shortname.matches("^[a-zA-Z0-9_\\-]*$"))
            throw new IllegalArgumentException("Organization shortname must only contain alphanumeric characters, underscores, or hyphens");
    }

    @Override
    public boolean isOrganizationAlreadyExist(Organization organization) {
        if (organization == null)
            throw new IllegalArgumentException("Organization cannot be null");

        return organizationManagementDAO.isOrganizationAlreadyExist(organization);
    }

    @Override
    public void joinOrganization(String userId, String organizationId) {
        assertOrganizationParametersValid(userId, organizationId);
        organizationManagementDAO.joinOrganization(userId, organizationId);
    }

    @Override
    public Organization updateOrganization(String organizationId, Organization organization, GazelleIdentity identity) {
        authz.assertAuthorized(identity, ORGANIZATION_UPDATE, null, organizationId);
        if (organizationId == null)
            throw new IllegalArgumentException(ErrorMessage.ORGANIZATION_ID_IS_NULL.getMessage());
        if (organization == null)
            throw new IllegalArgumentException("Organization cannot be null");

        Organization newOrganizationAttributes = organizationManagementDAO.getOrganizationFromId(organizationId);
        if (organization.getName() != null && !organization.getName().isEmpty())
            newOrganizationAttributes.setName(organization.getName());

        Organization updatedOrganization = organizationManagementDAO.updateOrganization(organizationId, newOrganizationAttributes);
        if (organizationEventPublisher != null)
            organizationEventPublisher.publishOrganizationUpdateEvent(updatedOrganization);
        return updatedOrganization;
    }

    @Override
    public void archiveOrganization(String organizationId, GazelleIdentity identity) {
        authz.assertAuthorized(identity, ORGANIZATION_ARCHIVE, organizationId);
        if (organizationId == null || organizationId.isBlank())
            throw new IllegalArgumentException(ErrorMessage.ORGANIZATION_ID_IS_NULL.getMessage());

        organizationManagementDAO.archiveOrganization(organizationId);
        organizationManagementDAO.disableUserOfOrganization(organizationId);
    }

    private void assertOrganizationParametersValid(String userId, String organizationId) {
        if (userId == null)
            throw new IllegalArgumentException(ErrorMessage.USER_ID_IS_NULL.getMessage());
        if (organizationId == null)
            throw new IllegalArgumentException(ErrorMessage.ORGANIZATION_ID_IS_NULL.getMessage());
    }
}
