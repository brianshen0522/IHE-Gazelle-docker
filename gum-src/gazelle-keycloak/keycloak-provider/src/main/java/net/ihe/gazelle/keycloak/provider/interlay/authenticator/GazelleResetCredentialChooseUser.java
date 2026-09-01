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

package net.ihe.gazelle.keycloak.provider.interlay.authenticator;

import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.resetcred.ResetCredentialChooseUser;
import org.keycloak.models.DefaultActionTokenKey;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GazelleResetCredentialChooseUser extends ResetCredentialChooseUser {

    private static final Logger logger = LoggerFactory.getLogger(GazelleResetCredentialChooseUser.class);
    public static final String PROVIDER_ID = "gzl-reset-credentials-choose-user";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Choose User (Gazelle)";
    }

    /**
     * We reimplemented this authenticator because we don't want to directly receive mail when we reach the /login-actions/reset-credentials endpoint for logged users
     * The following part of this method has been removed ( AuthenticationManager.authenticateIdentityCookie() )
     * VLD : 10/09/2024
     */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        String existingUserId = context.getAuthenticationSession().getAuthNote(AbstractIdpAuthenticator.EXISTING_USER_INFO);
        if (existingUserId != null) {
            UserModel existingUser = AbstractIdpAuthenticator.getExistingUser(context.getSession(), context.getRealm(), context.getAuthenticationSession());

            logger.debug("Forget-password triggered when reauthenticating user after first broker login. Prefilling reset-credential-choose-user screen with user {}", existingUser.getUsername());
            context.setUser(existingUser);
            Response challenge = context.form().createPasswordReset();
            context.challenge(challenge);
            return;
        }

        String actionTokenUserId = context.getAuthenticationSession().getAuthNote(DefaultActionTokenKey.ACTION_TOKEN_USER_ID);
        if (actionTokenUserId != null) {
            UserModel existingUser = context.getSession().users().getUserById(context.getRealm(), actionTokenUserId);

            // Action token logics handles checks for user ID validity and user being enabled

            logger.debug("Forget-password triggered when reauthenticating user after authentication via action token. Skipping reset-credential-choose-user screen and using user {}", existingUser.getUsername());
            context.setUser(existingUser);
            context.success();
            return;
        }

        Response challenge = context.form().createPasswordReset();
        context.challenge(challenge);
    }
}
