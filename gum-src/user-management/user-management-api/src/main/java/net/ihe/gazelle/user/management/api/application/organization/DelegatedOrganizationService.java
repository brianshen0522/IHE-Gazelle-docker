package net.ihe.gazelle.user.management.api.application.organization;

import net.ihe.gazelle.user.management.api.domain.organization.DelegatedOrganization;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;

import java.util.function.Function;

/**
 * Service interface for managing delegated organizations.
 *
 * This service provides operations for managing organizations that are
 * delegated to external identity providers. It handles the mapping between
 * external organization data and local organization entities, as well as
 * user-organization associations.
 *
 */
public interface DelegatedOrganizationService {

    /**
     * Upsert a delegated organization for a user
     * @param delegatedOrganization the delegated organization to upsert
     * @param userId the id of the concerned user
     * @param localOrganizationMatcher a function to match the delegated organization with the local organization
     */
    void upsertDelegatedOrganizationForUser(DelegatedOrganization delegatedOrganization, String userId, Function<DelegatedOrganization, Organization> localOrganizationMatcher);

    /**
     * Get the default local organization matcher (match by name)
     * @return the function to match the delegated organization with the local organization
     */
    Function<DelegatedOrganization, Organization> getDefaultLocalOrganizationMatcher();

    /**
     * Get the organization by its id (keyword)
     * @param organizationId the id (keyword) of the organization
     * @return the corresponding organization
     */
    Organization getOrganizationById(String organizationId);
}
