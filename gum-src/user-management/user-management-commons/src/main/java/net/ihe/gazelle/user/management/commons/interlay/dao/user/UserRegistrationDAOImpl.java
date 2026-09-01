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
import jakarta.persistence.Query;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;
import net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.GroupEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.UserEntity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.ihe.gazelle.user.management.commons.interlay.dao.user.UserEditDAOImpl.USER_ID;

/**
 * Implementation of the UserRegistrationDAO interface using JPA EntityManager to interact with the database.
 */
public class UserRegistrationDAOImpl implements UserRegistrationDAO {

    EntityManager entityManager;

    public UserRegistrationDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public User registerUser(User user) {
        UserEntity userEntity = new UserEntity(user);

        // For each group, check if it exists in the database, if not, create it
        Set<GroupEntity> groupEntitySet = userEntity.getGroupEntities().stream().map(
                this::retrieveGroupByIdAndCreateIfNotExist
        ).collect(Collectors.toSet());

        userEntity.setGroupEntities(groupEntitySet);
        entityManager.persist(userEntity);
        return user;
    }

    private GroupEntity retrieveGroupByIdAndCreateIfNotExist(GroupEntity group) {
        List<GroupEntity> inGroupEntities = entityManager.createQuery("select group from GroupEntity group where group.id = :id", GroupEntity.class)
                .setParameter("id", group.getId())
                .getResultList();
        if (inGroupEntities.isEmpty()) {
            inGroupEntities.add(group);
            entityManager.persist(inGroupEntities.get(0));
        }
        return inGroupEntities.get(0);
    }

    @Override
    public User activateUserWithActivationCode(String activationCode) {
        // Search corresponding user
        List<UserEntity> userEntities = entityManager.createQuery("SELECT u FROM UserEntity u WHERE u.activationCode = :activationCode", UserEntity.class)
                .setParameter("activationCode", activationCode).getResultList();

        if (userEntities.isEmpty())
            throw new GazelleDAOException(ErrorMessage.USER_NOT_FOUND.getMessage());

        UserEntity userEntity = userEntities.get(0);
        userEntity.setActivated(true);
        userEntity.setActivationCode(null);

        return entityManager.merge(userEntities.get(0)).asUser();
    }


    @Override
    public boolean isEmailAlreadyExist(String email) {
        Long result = entityManager.createQuery("select count(u) from UserEntity u where lower(u.email) = :email", Long.class)
            .setParameter("email", email.toLowerCase())
            .getSingleResult();

        return result.intValue() != 0;
    }

    @Override
    public List<User> getActiveAdminsOfOrganization(String organizationId) {
        Query query = entityManager.createQuery(
                "SELECT u FROM UserEntity u INNER JOIN u.groupEntities g WHERE g.id = :groupId AND u.activated = true", UserEntity.class)
                .setParameter("groupId", "org-adm:" + organizationId);

        List<?> rawList = query.getResultList();
        return rawList.stream()
                .map(element -> ((UserEntity) element).asUser())
                .toList();
    }

    @Override
    public int getAllUsersCount() {
        Query query = entityManager.createQuery("SELECT COUNT(u) FROM UserEntity u");
        return ((Long) query.getSingleResult()).intValue();
    }

    @Override
    public void rollbackUserRegistration(String userId) {
        // Delete the user + roles by cascading
        entityManager.createQuery("delete from UserEntity user where user.id = :userId")
                .setParameter(USER_ID, userId)
                .executeUpdate();
        // Delete the consent
        entityManager.createQuery("delete from ConsentEntity consent where consent.user.id = :userId")
                .setParameter(USER_ID, userId)
                .executeUpdate();
    }
}
