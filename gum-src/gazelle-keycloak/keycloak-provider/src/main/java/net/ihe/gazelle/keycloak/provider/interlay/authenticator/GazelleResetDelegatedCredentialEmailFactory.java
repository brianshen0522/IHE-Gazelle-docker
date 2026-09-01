package net.ihe.gazelle.keycloak.provider.interlay.authenticator;

import net.ihe.gazelle.keycloak.core.application.error.ErrorServiceGUI;
import net.ihe.gazelle.keycloak.core.interlay.error.ErrorServiceGUIImpl;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Collections;
import java.util.List;

public class GazelleResetDelegatedCredentialEmailFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

    public static final String PROVIDER_ID = "gazelle-delegated-reset-password";
    private GazelleResetDelegatedCredentialEmail singleton;

    @Override
    public Authenticator create(KeycloakSession session) {
        if (singleton == null) {
            ErrorServiceGUI errorService = new ErrorServiceGUIImpl();
            singleton = new GazelleResetDelegatedCredentialEmail(errorService);
        }
        return singleton;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getReferenceCategory() {
        return "GazelleDelegatedResetPassword";
    }

    @Override
    public String getDisplayType() {
        return "Block delegated users reset password (Gazelle)";
    }

    @Override
    public String getHelpText() {
        return "(Gazelle) Detect if the user is a delegated user. If yes, blocks the reset password.";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    public void init(Config.Scope config) {
        // No init
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // No post init
    }

    @Override
    public void close() {
        // No close
    }
}