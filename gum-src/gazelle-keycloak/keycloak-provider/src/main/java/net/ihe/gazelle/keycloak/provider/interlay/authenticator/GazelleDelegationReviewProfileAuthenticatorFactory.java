package net.ihe.gazelle.keycloak.provider.interlay.authenticator;

import net.ihe.gazelle.keycloak.core.application.error.ErrorServiceGUI;
import net.ihe.gazelle.keycloak.core.interlay.error.ErrorServiceGUIImpl;
import org.keycloak.authentication.authenticators.broker.IdpReviewProfileAuthenticatorFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

public class GazelleDelegationReviewProfileAuthenticatorFactory extends IdpReviewProfileAuthenticatorFactory {
    public static final String PROVIDER_ID = "gazelle-idp-review-profile";

    @Override
    public GazelleDelegationReviewProfileAuthenticator create(KeycloakSession session) {
        ErrorServiceGUI errorService = new ErrorServiceGUIImpl();
        return new GazelleDelegationReviewProfileAuthenticator(errorService);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Review profile (Gazelle)";
    }

    @Override
    public String getHelpText() {
        return "Review profile of a delegated user, that wants to link his user account with identity provider Gazelle. " +
                "It will block the authentication if the user is missing one or more of the following attributes:" +
                " first name, last name, email, organization id.";
    }

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        ProviderConfigProperty organizationIdProperty;
        organizationIdProperty = new ProviderConfigProperty();
        organizationIdProperty.setName(GazelleDelegationReviewProfileAuthenticator.ORGANIZATION_ID_CLAIM_PROPERTY);
        organizationIdProperty.setLabel("The name of the claim containing the organization id");
        organizationIdProperty.setType(ProviderConfigProperty.STRING_TYPE);
        organizationIdProperty.setHelpText("Define the claim to check the organization id");

        ProviderConfigProperty organizationNameProperty;
        organizationNameProperty = new ProviderConfigProperty();
        organizationNameProperty.setName(GazelleDelegationReviewProfileAuthenticator.ORGANIZATION_NAME_CLAIM_PROPERTY);
        organizationNameProperty.setLabel("The name of the claim containing the organization name");
        organizationNameProperty.setType(ProviderConfigProperty.STRING_TYPE);
        organizationNameProperty.setHelpText("Define the claim to check the organization name");

        configProperties.add(organizationIdProperty);
        configProperties.add(organizationNameProperty);
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
}
