/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package net.ihe.gazelle.keycloak.provider.interlay.authenticator;

import jakarta.persistence.EntityManager;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.commons.application.user.delegation.UserDelegationDAO;
import net.ihe.gazelle.user.management.commons.application.user.delegation.UserDelegationServiceImpl;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserDelegationDAOImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserEditDAOImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserLookupDAOImpl;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.broker.IdpCreateUserIfUniqueAuthenticatorFactory;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;

/**
 * Factory for creating Gazelle delegation "Create User If Unique" authenticators.
 *
 * This factory extends the standard Keycloak IdpCreateUserIfUniqueAuthenticatorFactory
 * to provide Gazelle-specific user creation and delegation logic. It detects if there
 * is an existing Keycloak account with the same email as the identity provider and
 * creates a new user if none exists, with proper delegation handling.
 *
 */
public class GazelleDelegationCreateUserIfUniqueAuthenticatorFactory extends IdpCreateUserIfUniqueAuthenticatorFactory {

    /**
     * The provider identifier for this authenticator factory.
     */
    public static final String PROVIDER_ID = "gazelle-idp-create-user-if-unique";

    /**
     * Creates a new authenticator instance for the given Keycloak session.
     *
     * This method sets up the necessary DAO and service dependencies for
     * user delegation operations and returns a configured authenticator.
     *
     * @param session the Keycloak session
     * @return a new GazelleDelegationCreateUserIfUniqueAuthenticator instance
     */
    @Override
    public Authenticator create(KeycloakSession session) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class, "gum-store").getEntityManager();
        UserDelegationDAO userDelegationDAO = new UserDelegationDAOImpl(em);
        UserLookupDAO userLookupDAO = new UserLookupDAOImpl(em);
        UserEditDAO userEditDAO = new UserEditDAOImpl(em);
        UserDelegationService userDelegationService = new UserDelegationServiceImpl(userDelegationDAO, userLookupDAO, userEditDAO);
        return new GazelleDelegationCreateUserIfUniqueAuthenticator(userDelegationService);
    }

    /**
     * Gets the unique identifier for this authenticator factory.
     *
     * @return the provider ID
     */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /**
     * Gets the reference category for this authenticator.
     *
     * @return the reference category name
     */
    @Override
    public String getReferenceCategory() {
        return "GazelleCreateUserIfUnique";
    }

    /**
     * Gets the display type name for this authenticator.
     *
     * @return the display type shown in the Keycloak admin UI
     */
    @Override
    public String getDisplayType() {
        return "Create User If Unique (Gazelle)";
    }

    /**
     * Gets the help text describing this authenticator's functionality.
     *
     * @return a description of what this authenticator does
     */
    @Override
    public String getHelpText() {
        return "(Gazelle) Detect if there is existing Keycloak account with same email like identity provider. If no, create new user";
    }
}
