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

import net.ihe.gazelle.keycloak.core.interlay.KeycloakUtils;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.IdpEmailVerificationAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.util.Objects;

public class GazelleDelegationEmailVerificationAuthenticator extends IdpEmailVerificationAuthenticator {

    private final UserDelegationService userDelegationService;

    public GazelleDelegationEmailVerificationAuthenticator(UserDelegationService userDelegationService) {
        super();
        this.userDelegationService = userDelegationService;
    }

    @Override
    protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        super.authenticateImpl(context, serializedCtx, brokerContext);

        //If a delegated user logs with another IDP, we remove the link of the previous IDP and retransform the user to be linked
        //with the new IDP. With this ensure a delegated user is linked to only one IDP at a time.

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        if (Objects.equals(authSession.getAuthNote(VERIFY_ACCOUNT_IDP_USERNAME), brokerContext.getUsername())) {
            if (userDelegationService.isUserDelegatedFromEmail(brokerContext.getEmail())) {
                context.getSession()
                        .users()
                        .getFederatedIdentitiesStream(context.getRealm(), brokerContext.getAuthenticationSession().getAuthenticatedUser())
                        .findFirst()
                        .ifPresent(federatedIdentity ->
                                removeCurrentIdentityProviderLink(context, brokerContext, federatedIdentity)
                        );
            }

            String issuer = getIssuer(brokerContext);
            userDelegationService.transformUserIntoDelegatedUser(brokerContext.getEmail(), brokerContext.getId(), issuer);
        }
    }

    private void removeCurrentIdentityProviderLink(AuthenticationFlowContext context, BrokeredIdentityContext brokerContext, FederatedIdentityModel federatedIdentity) {
        context.getSession().users().removeFederatedIdentity(context.getRealm(), brokerContext.getAuthenticationSession().getAuthenticatedUser(), federatedIdentity.getIdentityProvider());
    }

    private String getIssuer(BrokeredIdentityContext brokerContext) {
        JsonWebToken idToken = KeycloakUtils.getJsonWebTokenFromBrokerContext(brokerContext);
        return idToken != null ? idToken.getIssuer() : null;
    }
}
