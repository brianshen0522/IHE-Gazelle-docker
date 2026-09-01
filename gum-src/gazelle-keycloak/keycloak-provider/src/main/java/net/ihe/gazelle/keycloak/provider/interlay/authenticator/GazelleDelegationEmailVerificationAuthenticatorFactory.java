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
import org.keycloak.authentication.authenticators.broker.IdpEmailVerificationAuthenticatorFactory;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;

public class GazelleDelegationEmailVerificationAuthenticatorFactory extends IdpEmailVerificationAuthenticatorFactory {

    public static final String PROVIDER_ID = "gazelle-idp-email-verification";

    @Override
    public Authenticator create(KeycloakSession session) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class, "gum-store").getEntityManager();
        UserDelegationDAO userDelegationDAO = new UserDelegationDAOImpl(em);
        UserLookupDAO userLookupDAO = new UserLookupDAOImpl(em);
        UserEditDAO userEditDAO = new UserEditDAOImpl(em);
        UserDelegationService userDelegationService = new UserDelegationServiceImpl(userDelegationDAO, userLookupDAO, userEditDAO);
        return new GazelleDelegationEmailVerificationAuthenticator(userDelegationService);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getDisplayType() {
        return "Verify existing account by Email (Gazelle)";
    }

    @Override
    public String getHelpText() {
        return "Email verification of existing Keycloak user, that wants to link his user account with identity provider Gazelle";
    }
}
