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
import net.ihe.gazelle.user.management.api.domain.user.DelegatedUser;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;
import net.ihe.gazelle.user.management.commons.application.user.delegation.UserDelegationDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.DelegatedUserEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.UserEntity;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static net.ihe.gazelle.user.management.commons.interlay.dao.user.UserEditDAOImpl.USER_ID;

/**
 * DAO implementation for managing delegated users, providing methods to create, retrieve, and transform users into delegated users.
 */
public class UserDelegationDAOImpl implements UserDelegationDAO {

    private final EntityManager em;
    private static final String ID_PARAMETER = "id";
    private static final String EXTERNAL_ID_PARAMETER = "externalId";
    private static final String IDP_PARAMETER = "idpId";

    public UserDelegationDAOImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public DelegatedUser getDelegatedUser(String externalId, String idpId) {
        Query query = em.createQuery("SELECT u FROM DelegatedUserEntity u WHERE u.externalId = :externalId AND u.idpId = :idpId")
                .setParameter(EXTERNAL_ID_PARAMETER, externalId)
                .setParameter(IDP_PARAMETER, idpId)
                .setFirstResult(0)
                .setMaxResults(1);
        try {
            DelegatedUserEntity userEntities = (DelegatedUserEntity) query.getSingleResult();
            return userEntities.asUser();
        } catch (NoResultException e) {
            throw new NoSuchElementException(ErrorMessage.USER_NOT_FOUND.getMessage());
        }
    }

    @Override
    public DelegatedUser getDelegatedUserById(String userId) {
        Query query = em.createQuery("SELECT u FROM DelegatedUserEntity u WHERE u.id = :userId")
                .setParameter(USER_ID, userId)
                .setFirstResult(0)
                .setMaxResults(1);
        try {
            DelegatedUserEntity userEntities = (DelegatedUserEntity) query.getSingleResult();
            return userEntities.asUser();
        } catch (NoResultException e) {
            throw new NoSuchElementException(ErrorMessage.USER_NOT_FOUND.getMessage());
        }
    }

    @Override
    public DelegatedUser createDelegatedUser(User user, String externalId, String idpId) {
        DelegatedUserEntity delegatedUser = new DelegatedUserEntity(user, externalId, idpId);
        delegatedUser.setExternalId(externalId);
        delegatedUser.setIdpId(idpId);
        delegatedUser.setGroupEntities(Set.of());
        em.persist(delegatedUser);
        return delegatedUser.asUser();
    }

    @Override
    public DelegatedUser transformUserIntoDelegatedUser(String userEmail, String externalId, String idpId) {
        List<?> userEntities = em.createQuery("SELECT u FROM UserEntity u WHERE u.email = :email ORDER BY u.id ASC")
                .setParameter("email", userEmail)
                .getResultList();

        if (userEntities.isEmpty())
            throw new NoSuchElementException(ErrorMessage.USER_NOT_FOUND.getMessage());

        UserEntity userEntity = (UserEntity) userEntities.getFirst();
        try {
            getDelegatedUserById(userEntity.getId());
            updateDelegatedUser(userEntity.getId(), externalId, idpId);
        } catch (NoSuchElementException _) {
            insertTransformedDelegatedUser(userEntity.getId(), externalId, idpId);
        }
        deleteDelegatedUserCredentials(userEntity.getId());
        removeUserGroups(userEntity.getId());
        return new DelegatedUserEntity(userEntity.asUser(), externalId, idpId).asUser();
    }

    @Override
    public boolean isDelegatedUserExisting(String externalId, String idpId) {
        Query query = em.createQuery("SELECT u FROM DelegatedUserEntity u WHERE u.externalId = :externalId AND u.idpId = :idpId")
                .setParameter(EXTERNAL_ID_PARAMETER, externalId)
                .setParameter(IDP_PARAMETER, idpId)
                .setFirstResult(0)
                .setMaxResults(1);
        try {
            DelegatedUserEntity userEntities = (DelegatedUserEntity) query.getSingleResult();
            return userEntities != null;
        } catch (NoResultException e) {
            return false;
        }
    }

    private void updateDelegatedUser(String userId, String externalId, String idpId) {
        em.createNativeQuery("UPDATE gum.delegated_user SET external_id = :externalId, idp_id = :idpId WHERE user_id = :id")
                .setParameter(ID_PARAMETER, userId)
                .setParameter(EXTERNAL_ID_PARAMETER, externalId)
                .setParameter(IDP_PARAMETER, idpId)
                .executeUpdate();
    }

    private void insertTransformedDelegatedUser(String userId, String externalId, String idpId) {
        em.createNativeQuery("INSERT INTO gum.delegated_user (user_id, external_id, idp_id) VALUES (:id, :externalId, :idpId)")
                .setParameter(ID_PARAMETER, userId)
                .setParameter(EXTERNAL_ID_PARAMETER, externalId)
                .setParameter(IDP_PARAMETER, idpId)
                .executeUpdate();
    }

    private void deleteDelegatedUserCredentials(String userId) {
        em.createQuery("DELETE FROM CredentialsEntity c WHERE c.user.id = :id")
                .setParameter("id", userId)
                .executeUpdate();
    }

    private void removeUserGroups(String userId) {
        em.createNativeQuery("delete from gum.user_group WHERE user_id = :id")
                .setParameter(ID_PARAMETER, userId)
                .executeUpdate();
    }
}
