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
import net.ihe.gazelle.user.management.api.domain.user.Credentials;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;
import net.ihe.gazelle.user.management.commons.application.user.login.UserLoginDAO;
import net.ihe.gazelle.user.management.commons.interlay.cipher.CredentialsSerializer;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.CredentialsEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.UserEntity;

import java.sql.Timestamp;
import java.util.NoSuchElementException;

/**
 * Implementation of UserLoginDAO using JPA EntityManager to interact with the database.
 */
public class UserLoginDAOImpl implements UserLoginDAO {

    EntityManager entityManager;
    CredentialsSerializer credentialsHandler;

    public UserLoginDAOImpl(EntityManager entityManager) {
        this();
        this.entityManager = entityManager;
    }

    public UserLoginDAOImpl() {
        credentialsHandler = new CredentialsSerializer();
    }

    @Override
    public Credentials getCredentialsForUserId(String userId) {
        CredentialsEntity credentialsEntity = entityManager.find(CredentialsEntity.class, userId);

        if(credentialsEntity == null)
            throw new NoSuchElementException(ErrorMessage.CREDENTIALS_NOT_FOUND.getMessage());

        return credentialsHandler.mapStringToCredentials(credentialsEntity.getCredentials());
    }

    @Override
    public void updateLoginMetricsForUserId(String userId, Timestamp timestamp) {
        UserEntity userEntity = entityManager.find(UserEntity.class, userId);
        if (userEntity == null) throw new GazelleDAOException(ErrorMessage.USER_NOT_FOUND.getMessage());
        userEntity.setLastLoginTimestamp(timestamp);
        userEntity.setLoginCounter(userEntity.getLoginCounter() + 1);
        entityManager.merge(userEntity);
    }

    @Override
    public boolean needToChangePassword(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId shall not be null");
        }

        CredentialsEntity credentialsEntity = entityManager.find(CredentialsEntity.class, userId);

        if(credentialsEntity == null)
            return false;

        return credentialsEntity.getResetPassword();
    }
}
