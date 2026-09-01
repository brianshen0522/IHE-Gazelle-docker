package net.ihe.gazelle.user.management.commons.application.organization.lookup;

import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.Sort;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.interlay.exceptions.TMOrganizationException;

import java.util.List;
import java.util.NoSuchElementException;

public interface OrganizationLookupDAO {

    /**
     * Searches for deployed services based on the provided search criteria, range, and sorting parameters.
     *
     * @param testRunExecutionSearchCriteria the criteria to filter the test run executions
     * @param range                          the pagination range for the results
     * @param sortParameters                 the sorting parameters for the results
     * @return a SearchResult containing the test run executions matching the criteria
     */
    SearchResult<Organization> search(OrganizationSearchCriteria testRunExecutionSearchCriteria,
                                      Range range, List<Sort> sortParameters, GazelleIdentity identity);

    /**
     * Provides search suggestions based on a field, criteria, and identity.
     *
     * @param field    the field for which suggestions are requested
     * @param criteria the criteria to filter suggestions
     * @return a list of suggested strings matching the search text
     */
    List<String> getSuggestions(String field, OrganizationSearchCriteria criteria);

    /**
     * Retrieve an organization by its id
     * @param organizationId the id of the organization
     * @return the organization corresponding to the id, else null
     * @throws TMOrganizationException if the retrieval failed
     * @throws NoSuchElementException if the organization is not found
     */
    Organization getOrganizationById(String organizationId);
}
