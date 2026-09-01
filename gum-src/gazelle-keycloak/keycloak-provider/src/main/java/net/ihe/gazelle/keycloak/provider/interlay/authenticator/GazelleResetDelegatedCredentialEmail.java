package net.ihe.gazelle.keycloak.provider.interlay.authenticator;

import net.ihe.gazelle.keycloak.core.application.error.ErrorServiceGUI;
import net.ihe.gazelle.keycloak.core.interlay.KeycloakUtils;
import net.ihe.gazelle.keycloak.provider.interlay.GazelleUserModelAdapter;
import net.ihe.gazelle.user.management.api.domain.user.DelegatedUser;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.resetcred.ResetCredentialEmail;
import org.keycloak.models.UserModel;
import org.keycloak.theme.Theme;

import java.util.Properties;

public class GazelleResetDelegatedCredentialEmail extends ResetCredentialEmail implements Authenticator {

    private static final String ACCOUNT_DELEGATED = "resetPasswordDelegatedAccount";
    private final ErrorServiceGUI errorService;

    public GazelleResetDelegatedCredentialEmail(ErrorServiceGUI errorService) {
        this.errorService = errorService;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel userModel = context.getUser();
        if (!isDelegatedUser(userModel)) {
            super.authenticate(context);
        } else {
            Properties messagesBundle = KeycloakUtils.getMessages(context, Theme.Type.LOGIN, userModel);
            String message = messagesBundle.getProperty(ACCOUNT_DELEGATED);
            errorService.generateCustomErrorPage(context, message);
        }
    }

    public boolean isDelegatedUser(UserModel userModel) {
        return userModel instanceof GazelleUserModelAdapter gumUser && gumUser.getUser() instanceof DelegatedUser;
    }
}
