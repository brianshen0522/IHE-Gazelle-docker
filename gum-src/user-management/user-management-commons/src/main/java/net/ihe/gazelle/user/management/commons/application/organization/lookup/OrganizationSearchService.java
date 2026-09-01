package net.ihe.gazelle.user.management.commons.application.organization.lookup;

import net.ihe.gazelle.search.api.IndexService;
import net.ihe.gazelle.search.api.SearchService;
import net.ihe.gazelle.search.api.SuggestionService;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;

/**
 * Service responsible to search for organization based on Search API
 */
public interface OrganizationSearchService extends
        SearchService<Organization, OrganizationSearchCriteria>,
        IndexService,
        SuggestionService<OrganizationSearchCriteria> {


}
