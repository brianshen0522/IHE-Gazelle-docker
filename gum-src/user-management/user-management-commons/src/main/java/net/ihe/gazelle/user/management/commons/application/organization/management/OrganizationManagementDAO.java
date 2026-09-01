package net.ihe.gazelle.user.management.commons.application.organization.management;

import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.api.application.ConflictException;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;

public interface OrganizationManagementDAO {

    /**
     * Create an organization
     *
     * @param organization the organization to create
     * @throws GazelleDAOException if creation failed
     */
    void createOrganization(Organization organization);

    /**
     * Check if an organization is already registered
     *
     * @param organization the organization ID to check
     * @return true if the organization is already registered, else false
     * @throws GazelleDAOException if the research failed
     */
    boolean isOrganizationAlreadyExist(Organization organization);

    /**
     * Get organization from its unique id
     * @param organizationId the id of the organization
     */
    Organization getOrganizationFromId(String organizationId);

    /**
     * Join an organization, the user will leave the previous organization and join the new one
     *
     * @param userId         the id of the concerned user
     * @param organizationId the id of the organization to join
     */
    void joinOrganization(String userId, String organizationId);

    /**
     * Update an organization
     *
     * @param organizationId the id of the organization to update
     * @param organization   the organization with the updated information
     * @return the updated organization
     * @throws ConflictException if some organization attributes are already used by another organization
     */
    Organization updateOrganization(String organizationId, Organization organization);

    /**
     * Archive an organization
     *
     * @param organizationId the id of the organization to delete
     **/
    void archiveOrganization(String organizationId);

    /**
     * Disable user of given organization
     * @param organizationId the id of the organization if the users to disable
     */
    void disableUserOfOrganization(String organizationId);
}
