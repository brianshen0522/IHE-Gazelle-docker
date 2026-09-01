package net.ihe.gazelle.user.management.commons.interlay.dao.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchParameter;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.Sort;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchCriteria;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchIndexServiceImpl;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.DelegatedUserEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.GroupEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.OrganizationEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.UserEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Implementation of the UserSearchDAO interface using JPA EntityManager to perform database operations for user search.
 */
public class UserSearchDAOImpl implements UserSearchDAO {

    public static final String LAST_NAME = "lastName";
    public static final String FIRST_NAME = "firstName";
    public static final String EMAIL = "email";
    public static final String ORGANIZATION_ID = "organizationId";
    private final EntityManager entityManager;

    public UserSearchDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<String> getSuggestions(String field, UserSearchCriteria criteria) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> query = criteriaBuilder.createQuery();

        Root<UserEntity> root = query.from(UserEntity.class);

        Path<?> selectionBasePath = root;
        String columnName = getColumnName(field);
        List<Predicate> predicates = new ArrayList<>();

        if (UserSearchIndexServiceImpl.GROUP.equals(field)) {
            selectionBasePath = root.join("groupEntities", JoinType.LEFT);
            Predicate notLikeOrg = criteriaBuilder.notLike(selectionBasePath.get("id"), "%org:%");
            Predicate notLikeOrgAdm = criteriaBuilder.notLike(selectionBasePath.get("id"), "%org-adm:%");
            predicates.add(criteriaBuilder.and(notLikeOrg, notLikeOrgAdm));
        }
        if (UserSearchIndexServiceImpl.ORGANIZATION_NAME.equals(field)) {
            Root<OrganizationEntity> organizationRoot = query.from(OrganizationEntity.class);
            Path<String> organizationNameSelection = organizationRoot.get("name");
            Path<String> organizationShortnameSelection = organizationRoot.get("shortname");

            predicates.add(criteriaBuilder.equal(root.get(ORGANIZATION_ID), organizationRoot.get("id")));
            predicates.addAll(buildPredicates(criteria, criteriaBuilder, query, root));

            query.select(criteriaBuilder.array(organizationNameSelection, organizationShortnameSelection)).distinct(true);
            query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
            query.orderBy(criteriaBuilder.asc(organizationNameSelection), criteriaBuilder.asc(organizationShortnameSelection));

            return entityManager.createQuery(query)
                    .setMaxResults(100)
                    .getResultList()
                    .stream()
                    .flatMap(result -> {
                        Object[] values = (Object[]) result;
                        return Stream.of(values[0], values[1]);
                    })
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .distinct()
                    .toList();
        }

        Path<?> selection = selectionBasePath.get(columnName);
        query.select(selection).distinct(true);

        predicates.addAll(buildPredicates(criteria, criteriaBuilder, query, root));

        if (!predicates.isEmpty()) {
            query.where(criteriaBuilder.and(
                    criteriaBuilder.isNotNull(selection),
                    criteriaBuilder.and(predicates.toArray(new Predicate[0]))
            ));
        } else {
            query.where(criteriaBuilder.isNotNull(selection));
        }
        query.orderBy(criteriaBuilder.asc(selection));

        return entityManager.createQuery(query)
                .setMaxResults(100)
                .getResultList()
                .stream()
                .map(String::valueOf)
                .toList();
    }

    @Override
    public SearchResult<User> search(UserSearchCriteria criteria, Range range, List<Sort> sortParameters) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> query = criteriaBuilder.createQuery(UserEntity.class);

        Root<UserEntity> root = query.from(UserEntity.class);
        List<Predicate> predicates = buildPredicates(criteria, criteriaBuilder, query, root);
        if (!predicates.isEmpty()) {
            query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        }


        //Need to recreate a new Query, new Root and Predicates for count otherwise it will throw an exception
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<UserEntity> rootCount = countQuery.from(UserEntity.class);
        countQuery.select(criteriaBuilder.countDistinct(rootCount.get("id")));
        List<Predicate> countPredicates = buildPredicates(criteria, criteriaBuilder, countQuery, rootCount);
        if (!countPredicates.isEmpty()) {
            countQuery.where(criteriaBuilder.and(countPredicates.toArray(new Predicate[0])));
        }

        if (sortParameters != null && !sortParameters.isEmpty()) {
            sortElementInQuery(sortParameters, criteriaBuilder, root, query);
        }

        List<User> users = entityManager.createQuery(query)
                .setFirstResult(range.getOffset())
                .setMaxResults(range.getLimit())
                .getResultList()
                .stream()
                .map(UserEntity::asUser)
                .toList();
        Long totalObjects = entityManager.createQuery(countQuery).getSingleResult();
        return new SearchResult<>(users, range.getOffset(), range.getLimit(), totalObjects.intValue());
    }

    @Override
    public SearchResult<User> getUserById(String userId) {
        UserEntity userEntity = entityManager.find(UserEntity.class, userId);

        List<User> user = List.of(userEntity.asUser());
        //Offset and limit are set to default value even though the max result is 1
        return new SearchResult<>(user, 0, 25, user.size());

    }

    private static List<Predicate> buildPredicates(UserSearchCriteria criteria,
                                                   CriteriaBuilder cb,
                                                   CriteriaQuery<?> query,
                                                   Root<UserEntity> root) {
        List<Predicate> predicates = new ArrayList<>();

        SearchParameter emailParam = criteria.getEmailParam();
        if (isParameterValid(emailParam)) {
            buildQueryForProvidedParam(emailParam, cb, root, predicates, EMAIL);
        }

        SearchParameter firstNameParam = criteria.getFirstNameParam();
        if (isParameterValid(firstNameParam)) {
            buildQueryForProvidedParam(firstNameParam, cb, root, predicates, FIRST_NAME);
        }

        SearchParameter lastNameParam = criteria.getLastNameParam();
        if (isParameterValid(lastNameParam)) {
            buildQueryForProvidedParam(lastNameParam, cb, root, predicates, LAST_NAME);
        }

        SearchParameter groupParam = criteria.getGroupParam();
        if (isParameterValid(groupParam)) {
            Join<UserEntity, GroupEntity> groupJoin = root.join("groupEntities", JoinType.LEFT);
            buildQueryForProvidedParam(groupParam, cb, groupJoin, predicates, "id");
        }

        SearchParameter activatedParam = criteria.getActivatedParam();
        if (isParameterValid(activatedParam)) {
            buildQueryForProvidedParam(activatedParam, cb, root, predicates, "activated");
        }

        SearchParameter organizationNameParam = criteria.getOrganizationNameParam();
        if (isParameterValid(organizationNameParam)) {
            buildOrganizationNamePredicate(organizationNameParam, cb, query, root, predicates);
        }

        SearchParameter organizationIdParam = criteria.getOrganizationIdParam();
        if (isParameterValid(organizationIdParam)) {
            buildQueryForProvidedParam(organizationIdParam, cb, root, predicates, ORGANIZATION_ID);
        }

        SearchParameter delegatedParam = criteria.getDelegatedParam();
        if (isParameterValid(delegatedParam)) {
            buildDelegatedPredicate(delegatedParam, cb, root, predicates);
        }

        SearchParameter searchParameter = criteria.getSearchParam();
        if (isParameterValid(searchParameter)) {
            buildSearchPredicate(searchParameter, cb, root, predicates);
        }
        return predicates;
    }

    private static boolean isParameterValid(SearchParameter emailParam) {
        return emailParam != null && emailParam.getValues() != null && !emailParam.getValues().isEmpty();
    }

    private static void buildOrganizationNamePredicate(SearchParameter param,
                                                       CriteriaBuilder cb,
                                                       CriteriaQuery<?> query,
                                                       Root<UserEntity> root,
                                                       List<Predicate> predicates) {
        Subquery<String> organizationSubquery = query.subquery(String.class);
        Root<OrganizationEntity> organizationRoot = organizationSubquery.from(OrganizationEntity.class);

        List<Predicate> organizationPredicates = new ArrayList<>();

        for (String field : List.of("name", "shortname")) {

            if (param.getValues().size() == 1) {
                Predicate predicate = buildEqualOrLikePredicate(param.getFirstValue(), cb, organizationRoot, field);
                if (param.isNegated()) {
                    predicate = cb.not(predicate);
                }
                organizationPredicates.add(predicate);
            }
            // Multiple criteria, we need to combine them with OR
            else {
                List<Predicate> orPredicates = param.getValues().stream()
                        .map(value -> buildEqualOrLikePredicate(value, cb, organizationRoot, field))
                        .toList();
                Predicate orPredicate = cb.or(orPredicates.toArray(new Predicate[0]));
                if (param.isNegated()) {
                    orPredicate = cb.not(orPredicate);
                }
                organizationPredicates.add(orPredicate);
            }

        }

        organizationSubquery
                .select(organizationRoot.get("id"))
                .where(cb.or(organizationPredicates.toArray(new Predicate[0])));

        predicates.add(root.get(ORGANIZATION_ID).in(organizationSubquery));
    }

    private static void buildSearchPredicate(SearchParameter param,
                                                       CriteriaBuilder cb,
                                                       Root<UserEntity> root,
                                                       List<Predicate> predicates) {

        List<Predicate> searchPredicates = new ArrayList<>();

        for (String field : List.of(FIRST_NAME, LAST_NAME, EMAIL)) {

            if (param.getValues().size() == 1) {
                Predicate predicate = buildEqualOrLikePredicate(param.getFirstValue(), cb, root, field);
                if (param.isNegated()) {
                    predicate = cb.not(predicate);
                }
                searchPredicates.add(predicate);
            }
            // Multiple criteria, we need to combine them with OR
            else {
                List<Predicate> orPredicates = param.getValues().stream()
                        .map(value -> buildEqualOrLikePredicate(value, cb, root, field))
                        .toList();
                Predicate orPredicate = cb.or(orPredicates.toArray(new Predicate[0]));
                if (param.isNegated()) {
                    orPredicate = cb.not(orPredicate);
                }
                searchPredicates.add(orPredicate);
            }

        }

        predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
    }

    private static void sortElementInQuery(List<Sort> sorts, CriteriaBuilder cb, Root<UserEntity> root, CriteriaQuery<UserEntity> query) {
        List<Order> orders = new ArrayList<>();
        for (Sort sort : sorts) {
            if (sort.isValid()) {

                String columnName = getColumnName(sort.getField());
                Expression<?> orderByField;
                if (UserSearchIndexServiceImpl.DELEGATED.equals(sort.getField())) {
                    orderByField = cb.selectCase()
                            .when(cb.equal(root.type(), UserEntity.class), 1)
                            .otherwise(0);
                } else if (UserSearchIndexServiceImpl.ORGANIZATION_ID.equals(sort.getField())) {
                    Subquery<String> organizationSubquery = query.subquery(String.class);
                    Root<OrganizationEntity> organizationRoot = organizationSubquery.from(OrganizationEntity.class);
                    organizationSubquery.select(organizationRoot.get("name"));
                    organizationSubquery.where(cb.equal(organizationRoot.get("id"), root.get(ORGANIZATION_ID)));
                    orderByField = organizationSubquery;
                } else {
                    orderByField = root.get(columnName);
                }

                if (sort.getOrder() == Sort.Order.ASCENDING) {
                    orders.add(cb.asc(orderByField));
                } else {
                    orders.add(cb.desc(orderByField));
                }
            }
        }
        if (!orders.isEmpty()) {
            query.orderBy(orders);
        }
    }


    private static void buildQueryForProvidedParam(SearchParameter param,
                                                   CriteriaBuilder cb,
                                                   Path<?> path,
                                                   List<Predicate> predicates,
                                                   String fieldName) {
        // One criterion
        if (param.getValues().size() == 1) {
            Predicate predicate = buildEqualOrLikePredicate(param.getFirstValue(), cb, path, fieldName);
            if (param.isNegated()) {
                predicate = cb.not(predicate);
            }
            predicates.add(predicate);
        }
        // Multiple criteria, we need to combine them with OR
        else {
            List<Predicate> orPredicates = param.getValues().stream()
                    .map(value -> buildEqualOrLikePredicate(value, cb, path, fieldName))
                    .toList();
            Predicate orPredicate = cb.or(orPredicates.toArray(new Predicate[0]));
            if (param.isNegated()) {
                orPredicate = cb.not(orPredicate);
            }
            predicates.add(orPredicate);
        }
    }

    private static Predicate buildEqualOrLikePredicate(Object value, CriteriaBuilder cb, Path<?> path, String fieldName) {
        if (isBooleanValue(value) || ORGANIZATION_ID.equals(fieldName)) {
            return cb.equal(path.get(fieldName), value);
        } else {
            return cb.like(cb.lower(path.get(fieldName)), "%" + value.toString().toLowerCase() + "%");
        }
    }

    private static boolean isBooleanValue(Object value) {
        return value instanceof Boolean
                || "true".equalsIgnoreCase(String.valueOf(value))
                || "false".equalsIgnoreCase(String.valueOf(value));
    }

    private static void buildDelegatedPredicate(SearchParameter param,
                                                CriteriaBuilder cb,
                                                Root<UserEntity> root,
                                                List<Predicate> predicates) {
        List<Predicate> delegatedPredicates = param.getValues().stream()
                .map(value -> Boolean.parseBoolean(String.valueOf(value)))
                .map(isDelegated -> Boolean.TRUE.equals(isDelegated)
                        ? cb.equal(root.type(), DelegatedUserEntity.class)
                        : cb.equal(root.type(), UserEntity.class))
                .toList();

        Predicate predicate = delegatedPredicates.size() == 1
                ? delegatedPredicates.getFirst()
                : cb.or(delegatedPredicates.toArray(new Predicate[0]));

        if (param.isNegated()) {
            predicate = cb.not(predicate);
        }

        predicates.add(predicate);
    }

    private static String getColumnName(String propertyName) {
        return switch (propertyName) {
            case UserSearchIndexServiceImpl.FIRSTNAME -> FIRST_NAME;
            case UserSearchIndexServiceImpl.LASTNAME -> LAST_NAME;
            case UserSearchIndexServiceImpl.EMAIL, UserSearchIndexServiceImpl.SEARCH -> EMAIL;
            case UserSearchIndexServiceImpl.GROUP -> "id";
            case UserSearchIndexServiceImpl.LAST_LOGIN_TIMESTAMP -> "lastLoginTimestamp";
            case UserSearchIndexServiceImpl.ORGANIZATION_ID -> ORGANIZATION_ID;
            default -> propertyName;
        };
    }
}
