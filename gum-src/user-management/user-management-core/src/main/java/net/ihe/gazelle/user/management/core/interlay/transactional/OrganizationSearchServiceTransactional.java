package net.ihe.gazelle.user.management.core.interlay.transactional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import net.ihe.gazelle.search.api.IndexedField;
import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationLookupDAO;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationSearchCriteria;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationSearchService;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationSearchServiceImpl;

import java.util.List;
import java.util.Map;

/**
 * Transactional implementation of ConsentService, which delegates the calls to a non-transactional implementation
 */
@RequestScoped
@Default
public class OrganizationSearchServiceTransactional implements OrganizationSearchService {

    private final OrganizationSearchService organizationSearchService;

    @Inject
    public OrganizationSearchServiceTransactional(OrganizationLookupDAO organizationLookupDAO) {
        this.organizationSearchService = new OrganizationSearchServiceImpl(organizationLookupDAO);
    }


    @Override
    public Map<String, IndexedField> getIndexes() {
        return organizationSearchService.getIndexes();
    }

    @Override
    public SearchResult<Organization> search(SearchQuery<OrganizationSearchCriteria> query, GazelleIdentity identity) {
        return organizationSearchService.search(query, identity);
    }

    @Override
    public SearchResult<Organization> search(SearchQuery<OrganizationSearchCriteria> query, List<String> attributePaths, GazelleIdentity identity) {
        return organizationSearchService.search(query, attributePaths, identity);
    }

    @Override
    public List<String> getSuggestions(String field, OrganizationSearchCriteria criteria, GazelleIdentity identity) {
        return organizationSearchService.getSuggestions(field, criteria, identity);
    }
}
