package net.ihe.gazelle.user.management.api.application.organization;

import net.ihe.gazelle.user.management.api.domain.organization.Organization;

/**
 * Component used to lookup organizations
 */
public interface OrganizationLookupService {

    /**
     * Get an organization by its id (keyword)
     *
     * @param organizationId the id (keyword) of the organization
     * @return the organization
     * @throws java.util.NoSuchElementException if no Organization is found
     */
    Organization getOrganizationById(String organizationId);
}
