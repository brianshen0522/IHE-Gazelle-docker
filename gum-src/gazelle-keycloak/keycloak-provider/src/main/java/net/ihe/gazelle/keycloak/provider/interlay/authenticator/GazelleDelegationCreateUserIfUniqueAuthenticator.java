package net.ihe.gazelle.keycloak.provider.interlay.authenticator;

import net.ihe.gazelle.keycloak.core.application.error.ErrorServiceGUI;
import net.ihe.gazelle.keycloak.core.interlay.KeycloakUtils;
import net.ihe.gazelle.keycloak.core.interlay.error.ErrorServiceGUIImpl;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.IdpCreateUserIfUniqueAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.ExistingUserInfo;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.UserModel;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.ErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * author : Valentin Lorand
 * date : 2023-02-01
 * reason : Customize IDPCreateUserIfUniqueAuthenticator to allow the creation of a delegated user when it first connects
 *          with the IDP.
 * keycloak version : 24.0.4
 */
public class GazelleDelegationCreateUserIfUniqueAuthenticator extends IdpCreateUserIfUniqueAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(GazelleDelegationCreateUserIfUniqueAuthenticator.class);

    private final UserDelegationService userDelegationService;
    private final ErrorServiceGUI errorServiceGUI;

    public GazelleDelegationCreateUserIfUniqueAuthenticator(UserDelegationService userDelegationService) {
        this.userDelegationService = userDelegationService;
        this.errorServiceGUI = new ErrorServiceGUIImpl();
    }

    @Override
    protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        log.debug("process authenticate method of GazelleIdpCreateUserIfUniqueAuthenticator for user: {}", brokerContext.getUsername());
        context.getAuthenticationSession().setAuthNote("GZL_IDP_ID", getIssuer(brokerContext));
        context.getAuthenticationSession().setAuthNote("GZL_IDP_USER_EXTERNAL_ID", brokerContext.getId());
        try {
            super.authenticateImpl(context, serializedCtx, brokerContext);
        } catch (ErrorResponseException e) {
            errorServiceGUI.generateCustomErrorPage(context, e.getErrorDescription());
        }
    }


    @Override
    protected ExistingUserInfo checkExistingUser(AuthenticationFlowContext context, String username, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        // Check if user exist with externalId and idpName

        String externalId = brokerContext.getId();
        String idpId = getIssuer(brokerContext);

        if (userDelegationService.isDelegatedUserExisting(externalId, idpId)) {
            throw errorServiceGUI.generateIdentityProviderCustomErrorPage(
                    "This user is already delegated for the given external IDP",
                    this.getClass(),
                    "This user is already delegated for the given external IDP");
        } else {
            if (brokerContext.getEmail() != null && !context.getRealm().isDuplicateEmailsAllowed()) {
                UserModel existingUser = context.getSession().users().getUserByEmail(context.getRealm(), brokerContext.getEmail());
                if (existingUser != null) {
                    return new ExistingUserInfo(existingUser.getId(), UserModel.EMAIL, existingUser.getEmail());
                }
            }
        }
        return null;

    }

    private String getIssuer(BrokeredIdentityContext brokerContext) {
        JsonWebToken idToken = KeycloakUtils.getJsonWebTokenFromBrokerContext(brokerContext);
        return idToken != null ? idToken.getIssuer() : null;
    }
}
