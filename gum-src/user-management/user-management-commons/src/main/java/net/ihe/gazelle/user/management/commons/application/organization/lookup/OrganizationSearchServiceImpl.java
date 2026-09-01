package net.ihe.gazelle.user.management.commons.application.organization.lookup;

import net.ihe.gazelle.search.api.*;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationIndexService;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;

import java.util.List;
import java.util.Map;

/**
 *
 * Implementation of Organization Search service
 */
public class OrganizationSearchServiceImpl implements OrganizationSearchService {

    private static final OrganizationIndexService INDEX_SERVICE = new OrganizationIndexService();

    private final OrganizationLookupDAO organizationLookupDAO;

    /**
     * Constructor for ServiceLookupImpl.
     *
     * @param organizationLookupDAO the DAO used to perform organization lookups
     */
    public OrganizationSearchServiceImpl(OrganizationLookupDAO organizationLookupDAO) {
        this.organizationLookupDAO = organizationLookupDAO;
    }

    @Override
    public Map<String, IndexedField> getIndexes() {
        return INDEX_SERVICE.getIndexes();
    }

    @Override
    public List<String> getSuggestions(String field, OrganizationSearchCriteria criteria, GazelleIdentity identity) {
        if (field == null || field.trim().isEmpty()) {
            return List.of();
        }

        if (OrganizationIndexService.DELEGATED.equals(field) || OrganizationIndexService.ARCHIVED.equals(field)) {
            return List.of("true", "false");
        }

        return organizationLookupDAO.getSuggestions(field, criteria);
    }

    @Override
    public SearchResult<Organization> search(SearchQuery<OrganizationSearchCriteria> query, GazelleIdentity identity) {
        if (query == null) {
            query = new SearchQuery<>(new OrganizationSearchCriteria(), getDefaultRange(), getDefaultSort()); // Default to no criteria if query is null
        } else {
            List<Sort> sorts = query.sorts() != null && !query.sorts().isEmpty() ? query.sorts() : getDefaultSort();
            Range range = query.range() != null ? query.range() : getDefaultRange();
            OrganizationSearchCriteria criteria = query.searchCriteria() != null ? query.searchCriteria() : new OrganizationSearchCriteria();
            query = new SearchQuery<>(criteria, range, sorts);
        }

        Range.validateRange(query.range());
        return organizationLookupDAO.search(query.searchCriteria(), query.range(), query.sorts(), identity);

    }

    private static Range getDefaultRange() {
        return new Range().setOffset(Range.DEFAULT_OFFSET).setLimit(Range.DEFAULT_LIMIT);
    }

    private static List<Sort> getDefaultSort() {
        return List.of(new Sort("name", Sort.Order.ASCENDING));
    }

    @Override
    public SearchResult<Organization> search(SearchQuery<OrganizationSearchCriteria> query, List<String> attributePaths, GazelleIdentity gazelleIdentity) {
        throw new UnsupportedOperationException("Presentation schema search not implemented");
    }
}
