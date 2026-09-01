/*
 * Copyright 2026 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.user.management.commons.interlay.dao.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import net.ihe.gazelle.user.management.api.application.user.lookup.SortOrder;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserQueryParams;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.UserEntity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the UserLookupDAO interface using JPA EntityManager to perform database operations for user lookup.
 */
public class UserLookupDAOImpl implements UserLookupDAO {

    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String EMAIL = "email";
    public static final String ORGANIZATION_ID = "organizationId";
    public static final String ACTIVATED = "activated";
    public static final String LAST_LOGIN_TIMESTAMP = "lastLoginTimestamp";
    public static final String LOGIN_COUNTER = "loginCounter";
    public static final String LAST_UPDATE_TIMESTAMP = "lastUpdateTimestamp";
    public static final String DELEGATED = "delegated";
    public static final String GROUPS = "groups";
    private final EntityManager entityManager;

    public UserLookupDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @Override
    public List<User> searchForUsers(UserQueryParams userQueryParams, Integer offset, Integer limit, String sortBy, SortOrder sortOrder) {
        List<Criterion> criteria = buildCriterionList(userQueryParams);

        StringBuilder queryBuilder = new StringBuilder("SELECT u FROM UserEntity u");
        addJoinForDelegationCriteriaInQuery(userQueryParams, queryBuilder);

        joinGroups(userQueryParams, queryBuilder);
        appendCriteria(criteria, queryBuilder);
        if (isValidSortBy(sortBy)) {
            appendSort(sortBy, sortOrder, queryBuilder);
        }

        TypedQuery<UserEntity> typedQuery = entityManager.createQuery(queryBuilder.toString(), UserEntity.class);
        for (Criterion criterion : criteria) {
            if (criterion.param != null)
                typedQuery.setParameter(criterion.param, criterion.value);
        }
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(limit);

        return typedQuery.getResultList().stream()
                .map(UserEntity::asUser)
                .toList();
    }

    @Override
    public List<User> searchForUsersSummary(UserQueryParams userQueryParams, Integer offset, Integer limit, String sortBy, SortOrder sortOrder) {
        List<Criterion> criteria = buildCriterionList(userQueryParams);

        StringBuilder queryBuilder = new StringBuilder("SELECT u.id, u.lastName, u.firstName, u.organizationId FROM UserEntity u");
        addJoinForDelegationCriteriaInQuery(userQueryParams, queryBuilder);

        joinGroups(userQueryParams, queryBuilder);
        appendCriteria(criteria, queryBuilder);
        if (isValidSortBy(sortBy)) {
            appendSort(sortBy, sortOrder, queryBuilder);
        }

        TypedQuery<UserEntity> typedQuery = entityManager.createQuery(queryBuilder.toString(), UserEntity.class);
        for (Criterion criterion : criteria) {
            if (criterion.param != null)
                typedQuery.setParameter(criterion.param, criterion.value);
        }
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(limit);

        return typedQuery.getResultList().stream()
                .map(userEntity -> {
                    User user = new User(userEntity.getId());
                    user.setLastName(userEntity.getLastName());
                    user.setFirstName(userEntity.getFirstName());
                    user.setOrganizationId(userEntity.getOrganizationId());
                    return user;
                })
                .toList();
    }


    @SuppressWarnings("SqlSourceToSinkFlow")
    @Override
    public Long countFilteredUsers(UserQueryParams userQueryParams) {
        List<Criterion> criteria = buildCriterionList(userQueryParams);

        StringBuilder queryBuilder = new StringBuilder("SELECT count(u) FROM UserEntity u");
        addJoinForDelegationCriteriaInQuery(userQueryParams, queryBuilder);
        joinGroups(userQueryParams, queryBuilder);
        appendCriteria(criteria, queryBuilder);

        TypedQuery<Long> typedQuery = entityManager.createQuery(queryBuilder.toString(), Long.class);
        for (Criterion criterion : criteria) {
            if (criterion.param != null)
                typedQuery.setParameter(criterion.param, criterion.value);
        }

        return typedQuery.getSingleResult();
    }

    @Override
    public User getUserById(String userId) {
        List<?> userEntities = entityManager.createQuery("SELECT u FROM UserEntity u WHERE lower(u.id) = :id ORDER BY u.id ASC")
                .setParameter("id", userId.toLowerCase())
                .getResultList();

        if (userEntities.isEmpty())
            throw new NoSuchElementException(ErrorMessage.USER_NOT_FOUND.getMessage());

        return ((UserEntity) userEntities.getLast()).asUser();
    }

    @Override
    public User getUserSummaryById(String userId) {
        List<?> userEntities = entityManager.createQuery("SELECT u.id, u.lastName, u.firstName, u.organizationId FROM UserEntity u WHERE lower(u.id) = :id ORDER BY u.id ASC", UserEntity.class)
                .setParameter("id", userId.toLowerCase())
                .getResultList();

        if (userEntities.isEmpty())
            throw new NoSuchElementException(ErrorMessage.USER_NOT_FOUND.getMessage());

        UserEntity userFound = (UserEntity) userEntities.getLast();
        User user = new User(userFound.getId());
        user.setLastName(userFound.getLastName());
        user.setFirstName(userFound.getFirstName());
        user.setOrganizationId(userFound.getOrganizationId());
        return user;
    }

    @Override
    public User getUserByEmail(String email) {
        Query query = entityManager.createQuery("SELECT u FROM UserEntity u WHERE lower(u.email) = :email")
                .setParameter(EMAIL, email.toLowerCase())
                .setFirstResult(0)
                .setMaxResults(1);
        try {
            UserEntity userEntities = (UserEntity) query.getSingleResult();
            return userEntities.asUser();
        } catch (NoResultException e) {
            throw new NoSuchElementException(ErrorMessage.USER_NOT_FOUND.getMessage());
        }
    }

    @Override
    public String getActivationCodeForUserId(String userId) {
        UserEntity userEntity = entityManager.find(UserEntity.class, userId);
        if (userEntity == null)
            throw new NoSuchElementException(ErrorMessage.USER_NOT_FOUND.getMessage());
        return userEntity.getActivationCode();
    }

    @Override
    public User getUserByActivationCode(String activationCode) {
        Query query = entityManager.createQuery("SELECT u FROM UserEntity u WHERE u.activationCode = :activationCode")
                .setParameter("activationCode", activationCode);

        try {
            UserEntity userEntities = (UserEntity) query.getSingleResult();
            return userEntities.asUser();
        } catch (NoResultException e) {
            throw new NoSuchElementException(ErrorMessage.USER_NOT_FOUND.getMessage());
        }
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @Override
    public Map<String, Long> getValueCount(String propertyName, UserQueryParams userQueryParams) {

        List<Criterion> criteria = buildCriterionList(userQueryParams);
        StringBuilder queryBuilder = new StringBuilder("SELECT " + getColumnName(propertyName) + ", count(u)");
        if (GROUPS.equals(propertyName)) {
            queryBuilder.append(" FROM GroupEntity g JOIN UserEntity u ON g MEMBER OF u.groupEntities");
        } else {
            queryBuilder.append(" FROM UserEntity u");
            joinGroups(userQueryParams, queryBuilder);
        }

        appendCriteria(criteria, queryBuilder);

        queryBuilder.append(" GROUP BY ").append(getColumnName(propertyName));
        TypedQuery<Object[]> query = entityManager.createQuery(queryBuilder.toString(), Object[].class);
        for (Criterion criterion : criteria) {
            query.setParameter(criterion.param, criterion.value);
        }

        return query.getResultList().stream().collect(Collectors.toMap(
                row -> Objects.toString(row[0]),
                row -> (Long) row[1])
        );
    }

    private List<Criterion> buildCriterionList(UserQueryParams userQueryParams) {
        List<Criterion> criteria = new ArrayList<>();
        if (userQueryParams.search() != null && !userQueryParams.search().isEmpty()) {
            criteria.add(new Criterion("""
                    (LOWER(u.id) LIKE :search OR LOWER(u.email) LIKE :search OR LOWER(u.firstName) LIKE
                     :search OR LOWER(u.lastName) LIKE :search)
                     """,
                    "search", "%" + userQueryParams.search().toLowerCase() + "%"));
        }
        if (userQueryParams.firstName() != null) {
            criteria.add(new Criterion(" u.firstName = :firstname", "firstname", userQueryParams.firstName()));
        }
        if (userQueryParams.lastName() != null) {
            criteria.add(new Criterion(" u.lastName = :lastname", "lastname", userQueryParams.lastName()));
        }
        if (userQueryParams.email() != null) {
            criteria.add(new Criterion(" u.email = :email", EMAIL, userQueryParams.email()));
        }
        if (userQueryParams.organizationId() != null) {
            criteria.add(new Criterion(" u.organizationId = :organizationId", ORGANIZATION_ID, userQueryParams.organizationId()));
        }
        if (userQueryParams.activated() != null) {
            criteria.add(new Criterion(" u.activated = :activated", ACTIVATED, userQueryParams.activated()));
        }
        if (userQueryParams.group() != null) {
            criteria.add(new Criterion(" g.id LIKE CONCAT('%',:group,'%')", "group", userQueryParams.group()));
        }
        if (userQueryParams.externalId() != null) {
            criteria.add(new Criterion(" u.externalId = :externalId", "externalId", userQueryParams.externalId()));
        }
        if (userQueryParams.idpId() != null) {
            criteria.add(new Criterion(" u.idpId = :idpId", "idpId", userQueryParams.idpId()));
        }
        if (Boolean.FALSE.equals(userQueryParams.delegated())) {
            criteria.add(new Criterion(" du.id is null", null, null));
        }

        return criteria;
    }

    private static void addJoinForDelegationCriteriaInQuery(UserQueryParams userQueryParams, StringBuilder queryBuilder) {
        if (userQueryParams.delegated() == null) {
            queryBuilder.append(" LEFT JOIN DelegatedUserEntity du ON du.id = u.id ");
        } else if (Boolean.TRUE.equals(userQueryParams.delegated())) {
            queryBuilder.append(" JOIN DelegatedUserEntity du ON du.id = u.id ");
        } else if (Boolean.FALSE.equals(userQueryParams.delegated())) {
            queryBuilder.append(" LEFT JOIN DelegatedUserEntity du ON du.id = u.id ");
        }
    }

    private String getColumnName(String propertyName) {
        return switch (propertyName) {
            case FIRST_NAME -> "u.firstName";
            case LAST_NAME -> "u.lastName";
            case ORGANIZATION_ID -> "u.organizationId";
            case ACTIVATED -> "u.activated";
            case GROUPS -> "g.id";
            default -> throw new IllegalArgumentException("Invalid property name.");
        };
    }

    private static void joinGroups(UserQueryParams userQueryParams, StringBuilder queryBuilder) {
        if (userQueryParams.group() != null) {
            queryBuilder.append(" JOIN GroupEntity g ON g MEMBER OF u.groupEntities");
        }
    }

    private static void appendCriteria(List<Criterion> criteria, StringBuilder queryBuilder) {
        if (!criteria.isEmpty()) {
            queryBuilder.append(" WHERE");

            Iterator<Criterion> criterionIterator = criteria.stream().iterator();
            while (criterionIterator.hasNext()) {
                queryBuilder.append(criterionIterator.next().query);
                if (criterionIterator.hasNext()) {
                    queryBuilder.append(" AND");
                }
            }
        }
    }

    private void appendSort(String sortBy, SortOrder sortOrder, StringBuilder queryBuilder) {
        if (DELEGATED.equals(sortBy)) {
            sortBy = "du.externalId";
        } else {
            sortBy = "u." + sortBy;
        }

        queryBuilder.append(" ORDER BY ")
                    .append(sortBy)
                    .append(" ")
                    .append(sortOrder.name());
    }

    private boolean isValidSortBy(String sortBy) {
        return FIRST_NAME.equals(sortBy) ||
                LAST_NAME.equals(sortBy) ||
                EMAIL.equals(sortBy) ||
                ORGANIZATION_ID.equals(sortBy) ||
                ACTIVATED.equals(sortBy) ||
                LAST_LOGIN_TIMESTAMP.equals(sortBy) ||
                LOGIN_COUNTER.equals(sortBy) ||
                LAST_UPDATE_TIMESTAMP.equals(sortBy) ||
                DELEGATED.equals(sortBy);
    }

    private record Criterion(String query, String param, Object value) {
    }
}
