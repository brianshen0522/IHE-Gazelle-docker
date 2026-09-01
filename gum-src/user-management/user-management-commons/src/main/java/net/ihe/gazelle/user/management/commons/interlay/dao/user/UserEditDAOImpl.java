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
import jakarta.transaction.Transactional;
import net.ihe.gazelle.user.management.api.application.ConflictException;
import net.ihe.gazelle.user.management.api.domain.group.Group;
import net.ihe.gazelle.user.management.api.domain.user.Credentials;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditDAO;
import net.ihe.gazelle.user.management.commons.interlay.cipher.CredentialsSerializer;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.CredentialsEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.GroupEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.UserEntity;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.PREFIX_ORGANIZATION_ADMIN;
import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.PREFIX_ORGANIZATION_MEMBER;
import static net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage.USER_NOT_FOUND;

/**
 * Implementation of UserEditDAO using JPA EntityManager to interact with the database.
 */
public class UserEditDAOImpl implements UserEditDAO {

    public static final String USER_ID = "userId";
    EntityManager entityManager;

    CredentialsSerializer credentialsHandler;

    public UserEditDAOImpl(EntityManager entityManager) {
        this();
        this.entityManager = entityManager;
    }

    public UserEditDAOImpl() {
        credentialsHandler = new CredentialsSerializer();
    }

    @Override
    @Transactional
    public void updateCredentialsForUserId(String userId, Credentials inputCredentials) {
        UserEntity userEntity = entityManager.find(UserEntity.class, userId);
        if (userEntity == null)
            throw new NoSuchElementException(USER_NOT_FOUND.getMessage());

        CredentialsEntity credentialsEntity;
        try {
            credentialsEntity = entityManager.createQuery("SELECT c FROM CredentialsEntity c WHERE c.user.id = :userId", CredentialsEntity.class)
                    .setParameter(USER_ID, userId)
                    .getSingleResult();
            credentialsEntity.setResetPassword(false);
        } catch (NoResultException e) {
            credentialsEntity = new CredentialsEntity();
        }
        credentialsEntity.setUser(userEntity);
        String credentialsAsString = credentialsHandler.mapCredentialsToString(inputCredentials);
        credentialsEntity.setCredentials(credentialsAsString);

        entityManager.persist(credentialsEntity);
    }

    @Override
    @Transactional
    public User updateAttributes(String userId, User user) {
        UserEntity userEntity = entityManager.find(UserEntity.class, userId);
        if (userEntity == null)
            throw new NoSuchElementException(USER_NOT_FOUND.getMessage());

        if (user.getLastName() != null) userEntity.setLastName(user.getLastName());
        if (user.getFirstName() != null) userEntity.setFirstName(user.getFirstName());
        if (user.isActivated() != null) userEntity.setActivated(user.isActivated());
        if (user.getActivationCode() != null) userEntity.setActivationCode(user.getActivationCode());
        if (user.getEmail() != null) userEntity.setEmail(user.getEmail());
        if (user.getOrganizationId() != null)
            updateUserOrganization(userId, user.getOrganizationId());
        if (user.getGroupIds() != null && !user.getGroupIds().isEmpty())
            grantGroupEntitiesAndCreateThemIfNecessary(userEntity, user.getGroupIds());

        try {
            return entityManager.merge(userEntity).asUser();
        } catch (ConstraintViolationException e) {
            throw new ConflictException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateUserOrganization(String userId, String orgaId) {
        UserEntity userEntity = entityManager.find(UserEntity.class, userId);
        if (userEntity == null)
            throw new GazelleDAOException(ErrorMessage.USER_NOT_FOUND.getMessage());

        userEntity.setOrganizationId(orgaId);
        String groupId = userEntity.isOrgaAdministrator() ?
                PREFIX_ORGANIZATION_ADMIN.getName() + orgaId : PREFIX_ORGANIZATION_MEMBER.getName() + orgaId;
        GroupEntity groupEntity = entityManager.find(GroupEntity.class, groupId);
        if (groupEntity == null) {
            groupEntity = new GroupEntity(new Group(groupId));
            entityManager.persist(groupEntity);
        }

        userEntity.addGroupEntity(groupEntity);
        entityManager.merge(userEntity);
    }

    private void grantGroupEntitiesAndCreateThemIfNecessary(UserEntity userEntity, Set<String> groupIds) {
        // Grant group that exists
        Stream<GroupEntity> result = entityManager.createQuery("SELECT g FROM GroupEntity g", GroupEntity.class).getResultStream();
        Set<GroupEntity> groupEntities = result
                .filter(groupEntity -> groupIds.contains(groupEntity.getId()))
                .collect(Collectors.toSet());
        userEntity.setGroupEntities(groupEntities);

        // Create and grant groups that doesn't exist but valid
        List<String> groupEntityIds = groupEntities.stream().map(GroupEntity::getId).toList();
        List<String> groupIdsNotExisting = groupIds.stream().filter(groupId -> !groupEntityIds.contains(groupId)).toList();
        for (String groupIdNotExisting : groupIdsNotExisting) {
            try {
                Group newGroup = new Group(groupIdNotExisting);
                GroupEntity newGroupEntity = new GroupEntity(newGroup);
                entityManager.persist(newGroupEntity);
                userEntity.addGroupEntity(newGroupEntity);
            } catch (IllegalArgumentException e) {
                // Do nothing, do not add invalid groups
            }
        }
    }

    @Override
    @Transactional
    public void updateActivatedStatusOfUser(String userId, Boolean activate) {
        int result = entityManager.createQuery("UPDATE UserEntity u SET u.activated = :activate WHERE u.id = :userId")
                .setParameter(USER_ID, userId)
                .setParameter("activate", activate)
                .executeUpdate();
        if (result == 0) {
            throw new NoSuchElementException(USER_NOT_FOUND.getMessage());
        }
    }

    @Override
    @Transactional
    public void clearActivationCode(String userId) {
        entityManager.createQuery("UPDATE UserEntity u SET u.activationCode = NULL WHERE u.id = :userId")
                .setParameter(USER_ID, userId)
                .executeUpdate();
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        int result = entityManager.createQuery("DELETE UserEntity u WHERE u.id = :userId")
                .setParameter(USER_ID, userId)
                .executeUpdate();
        if (result == 0) {
            throw new NoSuchElementException(USER_NOT_FOUND.getMessage());
        }
    }

    @Override
    @Transactional
    public void archiveOrgaIfNoMembers(String orgaId) {
        Long count = entityManager.createQuery("SELECT COUNT(u) FROM UserEntity u WHERE u.organizationId = :orgaId", Long.class)
                .setParameter("orgaId", orgaId)
                .getSingleResult();
        if (count == 0) {
            entityManager.createQuery("UPDATE OrganizationEntity o SET o.archived = true WHERE o.id = :orgaId")
                    .setParameter("orgaId", orgaId)
                    .executeUpdate();
        }
    }

    @Override
    public User getUserFromUserId(String userId) {
        UserEntity user = entityManager.find(UserEntity.class, userId);
        return user != null ? user.asUser() : null;
    }
}
