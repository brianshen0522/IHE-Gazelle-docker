package net.ihe.gazelle.user.management.commons.interlay.dao.organization;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchParameter;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.Sort;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationIndexService;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationLookupDAO;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationSearchCriteria;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.DelegatedOrganizationEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.OrganizationEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implementation of the OrganizationLookupDAO interface using JPA for database access.
 * This class provides methods to search for organizations based on various criteria and to retrieve organization details by ID.
 */
public class OrganizationLookupDAOImpl implements OrganizationLookupDAO {
    private static final String VALUE_SUFFIX = "Value";
    private final EntityManager entityManager;

    public OrganizationLookupDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public SearchResult<Organization> search(OrganizationSearchCriteria criteria, Range range, List<Sort> sortParameters, GazelleIdentity identity) {
        List<String> whereClauses = new ArrayList<>();
        List<Criterion> criteriaToBind = new ArrayList<>();

        computeSearchCriteria(criteria, whereClauses, criteriaToBind);

        StringBuilder partialQueryBuilder = new StringBuilder();
        if (!whereClauses.isEmpty()) {
            partialQueryBuilder.append(" WHERE ").append(String.join(" AND ", whereClauses));
        }

        StringBuilder selectQuery = new StringBuilder("SELECT DISTINCT o FROM OrganizationEntity o")
                .append(partialQueryBuilder);
        addOrderBy(selectQuery, sortParameters);

        TypedQuery<OrganizationEntity> query = entityManager
                .createQuery(selectQuery.toString(), OrganizationEntity.class)
                .setFirstResult(range.getOffset())
                .setMaxResults(range.getLimit());
        bindCriteria(criteriaToBind, query);

        String countQueryStr = "SELECT COUNT(DISTINCT o.id) FROM OrganizationEntity o" + partialQueryBuilder;
        TypedQuery<Long> countQuery = entityManager.createQuery(countQueryStr, Long.class);
        bindCriteria(criteriaToBind, countQuery);

        List<Organization> organizations = query.getResultList().stream()
                .map(OrganizationEntity::asOrganization)
                .toList();

        return new SearchResult<>(organizations, range.getOffset(), range.getLimit(), countQuery.getSingleResult().intValue());
    }

    @Override
    public List<String> getSuggestions(String field, OrganizationSearchCriteria criteria) {
        String fieldSql = getFieldSql(field);
        StringBuilder queryBuilder = new StringBuilder()
                .append("SELECT DISTINCT ").append(fieldSql)
                .append(" FROM OrganizationEntity o");

        List<String> whereClauses = new ArrayList<>();
        List<Criterion> criteriaToBind = new ArrayList<>();
        computeSearchCriteria(criteria, whereClauses, criteriaToBind);

        if (!whereClauses.isEmpty()) {
            queryBuilder.append(" WHERE ").append(String.join(" AND ", whereClauses));
        }
        addOrderBy(queryBuilder, List.of(new Sort(field, Sort.Order.ASCENDING)));

        TypedQuery<String> query = entityManager
                .createQuery(queryBuilder.toString(), String.class)
                .setMaxResults(100);
        bindCriteria(criteriaToBind, query);

        return query.getResultList();
    }

    private void computeSearchCriteria(OrganizationSearchCriteria criteria,
                                       List<String> whereClauses,
                                       List<Criterion> criteriaToBind) {
        SearchParameter searchParam = criteria.getSearchParam();
        if (searchParam != null && searchParam.getValues() != null && !searchParam.getValues().isEmpty()) {
            addDefaultSearchCriteria(whereClauses, criteriaToBind, OrganizationIndexService.SEARCH, searchParam);
        }

        SearchParameter shortnameParam = criteria.getShortnameParam();
        if (shortnameParam != null && shortnameParam.getValues() != null && !shortnameParam.getValues().isEmpty()) {
            addLikeCriteria(whereClauses, criteriaToBind, "o.shortname", OrganizationIndexService.SHORTNAME, shortnameParam);
        }

        SearchParameter nameParam = criteria.getNameParam();
        if (nameParam != null && nameParam.getValues() != null && !nameParam.getValues().isEmpty()) {
            addLikeCriteria(whereClauses, criteriaToBind, "o.name", OrganizationIndexService.NAME, nameParam);
        }

        SearchParameter archivedParam = criteria.getArchivedParam();
        if (archivedParam != null && archivedParam.getFirstValue() != null) {
            addBooleanCriteria(whereClauses, criteriaToBind, "o.archived", OrganizationIndexService.ARCHIVED, archivedParam);
        }

        SearchParameter delegatedParam = criteria.getDelegatedParam();
        if (delegatedParam != null && delegatedParam.getFirstValue() != null) {
            Boolean isDelegated = (Boolean) delegatedParam.getFirstValue();
            whereClauses.add("TYPE(o) = :orgType");
            criteriaToBind.add(new Criterion("orgType", Boolean.TRUE.equals(isDelegated) ? DelegatedOrganizationEntity.class : OrganizationEntity.class));
        }
    }

    private void addBooleanCriteria(List<String> whereClauses, List<Criterion> criteriaToBind,
                                 String columnExpression, String parameterPrefix, SearchParameter parameter) {
        if (parameter.getValues().size() == 1) {
            String parameterName = parameterPrefix + VALUE_SUFFIX;
            whereClauses.add(columnExpression + " = :" + parameterName );
            criteriaToBind.add(new Criterion(parameterName,parameter.getFirstValue()));
        } else {
            throw new IllegalArgumentException("Multiple values not supported for boolean criteria: " + parameterPrefix);
        }
    }


    private void addDefaultSearchCriteria(List<String> whereClauses, List<Criterion> criteriaToBind, String parameterPrefix, SearchParameter parameter) {
        if (parameter.getValues().size() == 1) {
            String parameterName = parameterPrefix + VALUE_SUFFIX;
            whereClauses.add("LOWER(name) LIKE LOWER(:" + parameterName + ") OR LOWER(shortname) LIKE LOWER(:" + parameterName + ")");
            criteriaToBind.add(new Criterion(parameterName, "%" + parameter.getFirstValue() + "%"));
        } else {
            throw new IllegalArgumentException("Multiple values not supported for boolean criteria: " + parameterPrefix);
        }
    }

    private void addLikeCriteria(List<String> whereClauses, List<Criterion> criteriaToBind,
                                 String columnExpression, String parameterPrefix, SearchParameter parameter) {
        if (parameter.getValues().size() == 1) {
            String parameterName = parameterPrefix + VALUE_SUFFIX;
            whereClauses.add("LOWER(" + columnExpression + ") LIKE LOWER(:" + parameterName + ")");
            criteriaToBind.add(new Criterion(parameterName, "%" + parameter.getFirstValue() + "%"));
            return;
        }

        List<String> likes = new ArrayList<>();
        for (int i = 0; i < parameter.getValues().size(); i++) {
            String parameterName = parameterPrefix + i;
            likes.add("LOWER(" + columnExpression + ") LIKE LOWER(:" + parameterName + ")");
            criteriaToBind.add(new Criterion(parameterName, "%" + parameter.getValues().get(i) + "%"));
        }
        whereClauses.add("(" + String.join(" OR ", likes) + ")");
    }

    private void addOrderBy(StringBuilder queryBuilder, List<Sort> sortParameters) {
        if (sortParameters == null || sortParameters.isEmpty()) {
            return;
        }
        List<String> orderClauses = new ArrayList<>();
        for (Sort sort : sortParameters) {
            String fieldSql = getFieldSql(sort.field());
            orderClauses.add(fieldSql + " " + (sort.getOrder() == Sort.Order.DESCENDING ? "DESC" : "ASC"));
        }
        queryBuilder.append(" ORDER BY ").append(String.join(", ", orderClauses));
    }

    private String getFieldSql(String field) {
        return switch (field) {
            case OrganizationIndexService.SHORTNAME -> "o.shortname";
            case OrganizationIndexService.NAME, OrganizationIndexService.SEARCH -> "o.name";
            case OrganizationIndexService.DELEGATED -> "TYPE(o)";
            case OrganizationIndexService.LAST_UPDATE_TIMESTAMP -> "o.lastUpdateTimestamp";
            case OrganizationIndexService.ARCHIVED -> "o.archived";
            default -> "o." + field;
        };
    }

    private void bindCriteria(List<Criterion> criteriaToBind, TypedQuery<?> query) {
        for (Criterion criterion : criteriaToBind) {
            query.setParameter(criterion.name(), criterion.value());
        }
    }

    private record Criterion(String name, Object value) {
    }

    @Override
    public Organization getOrganizationById(String organizationId) {
        OrganizationEntity organizationEntity = entityManager.find(OrganizationEntity.class, organizationId);
        if (organizationEntity == null) {
            throw new NoSuchElementException("Organization with id " + organizationId + " not found");
        }
        return organizationEntity.asOrganization();
    }
}
