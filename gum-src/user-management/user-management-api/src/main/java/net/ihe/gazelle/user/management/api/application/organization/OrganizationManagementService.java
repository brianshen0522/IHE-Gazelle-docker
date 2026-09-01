package net.ihe.gazelle.user.management.api.application.organization;

import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.api.application.ConflictException;

/**
 * Component used to register organizations
 */
public interface OrganizationManagementService {

    /**
     * Create an organization
     * @param organization the organization to create
     * @throws ConflictException if the organization already exists
     * @throws IllegalArgumentException if the organization is null or has invalid fields
     * @return the created organization
     */
    Organization createOrganization(Organization organization, GazelleIdentity identity);

    /**
     * Check if an organization is already registered
     *
     * @param organization the organization to check
     * @return true if the organization is already registered, else false
     * @throws IllegalArgumentException if the organization is null
     */
    boolean isOrganizationAlreadyExist(Organization organization);

    /**
     * Join an organization, the user will leave the previous organization and join the new one
     * @param userId the id of the concerned user
     * @param organizationId the id of the organization to join
     * @throws IllegalArgumentException if the userId or organizationId is null or empty
     */
    void joinOrganization(String userId, String organizationId);

    /**
     * Update an organization
     * @param organizationId the id of the organization to update
     * @param organization the organization with the updated information
     * @throws IllegalArgumentException if the organizationId is null or empty, or if the organization is null or has invalid fields
     * @return the updated organization
     */
    Organization updateOrganization(String organizationId, Organization organization, GazelleIdentity identity);

    /**
     * Archive an organization
     * @param organizationId the id of the organization to archive
     * @throws IllegalArgumentException if the organizationId is null or empty
     */
    void archiveOrganization(String organizationId, GazelleIdentity identity);
}
