/*
 * Copyright 2024 IHE International.
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

package net.ihe.gazelle.user.management.core.interlay.factory;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import net.ihe.gazelle.user.management.commons.application.user.login.UserLoginDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserLoginDAOImpl;

/**
 * Factory for producing UserLoginDAO instances.
 */
public class UserLoginDAOFactory {

    private final EntityManager entityManager;

    /**
     * Constructor for UserLoginDAOFactory.
     * @param entityManager the EntityManager to be used by the produced UserLoginDAO instances
     */
    @Inject
    public UserLoginDAOFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Produces a UserLoginDAO instance.
     * @return a new UserLoginDAO instance
     */
    @Produces
    @RequestScoped
    public UserLoginDAO getUserLoginDAO() {
        return new UserLoginDAOImpl(entityManager);
    }
}
