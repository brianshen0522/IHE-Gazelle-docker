package net.ihe.gazelle.user.management.commons.application.organization.delegation;

import net.ihe.gazelle.user.management.api.domain.organization.DelegatedOrganization;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public interface DelegatedOrganizationDAO {

    /**
     * Retrieve an organization by its id
     * @param organizationId the id of the organization
     * @return the corresponding organization
     */
    Organization getOrganizationById(String organizationId);

    /**
     * Retrieve the list of organizations corresponding to the name of the organization
     * @param organizationName the name of the organization
     * @return the organization corresponding to the name of the organization, null if not found
     */
    Organization getOrganizationByName(String organizationName);

    /**
     * Retrieve the list of organizations corresponding to the parameters
     * @param parameters the parameters of search
     * @return the organization corresponding to the delegated organization, else null
     * @throws GazelleDAOException if the retrieval failed
     * @throws NoSuchElementException if the delegated organization is not found
     */
    List<Organization> searchForOrganization(Map<String,String> parameters);

    /**
     * Create a delegated organization
     *
     * @param delegatedOrganization: the organization.
     * @throws GazelleDAOException if registration failed.
     */
    void createDelegatedOrganization(DelegatedOrganization delegatedOrganization);

    /**
     * Check if delegated organization already exist using externalId and idpId.
     *
     * @param delegatedOrganization: the organization.
     * @throws GazelleDAOException if request failed.
     */
    boolean isDelegatedOrganizationExist(DelegatedOrganization delegatedOrganization);

    /**
     * Update the name of a delegated organization (as an HTTP PATCH)
     * @param organizationId the id of the delegated organization to update
     * @param delegatedOrganization the parameters to apply patch
     * @throws NoSuchElementException if no delegated organization is found
     * @throws GazelleDAOException if the delegated organization could not be updated
     */
    Organization updateDelegatedOrganization(String organizationId, DelegatedOrganization delegatedOrganization);
}
