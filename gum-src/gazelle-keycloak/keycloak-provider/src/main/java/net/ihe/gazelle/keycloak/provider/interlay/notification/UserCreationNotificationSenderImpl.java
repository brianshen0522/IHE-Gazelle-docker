package net.ihe.gazelle.keycloak.provider.interlay.notification;

import net.ihe.gazelle.keycloak.core.interlay.configuration.ApplicationConfigImpl;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.HashMap;
import java.util.Map;

public class UserCreationNotificationSenderImpl implements UserCreationNotificationSender {
    private final EmailTemplateProvider emailTemplateProvider;
    private final UserModel recipient;
    private final ApplicationConfig applicationConfig;

    public UserCreationNotificationSenderImpl(KeycloakSession session, RealmModel realm, UserModel recipient) {
        emailTemplateProvider = new FreeMarkerEmailTemplateProvider(session)
                .setRealm(realm).setUser(recipient);
        this.recipient = recipient;
        this.applicationConfig = new ApplicationConfigImpl();
    }

    @Override
    public void sendAccountCreatedByAdmin() throws EmailException {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("firstName", recipient.getFirstName());
        attributes.put("lastName", recipient.getLastName());
        attributes.put("testBedUrl", applicationConfig.getRootTestBedUrl());
        attributes.put("email", recipient.getEmail());

        attributes.put("resetPasswordLink", applicationConfig.getSSOBaseUrl() + "/realms/" + applicationConfig.getRealmName()
                + "/login-actions/reset-credentials");
        // Send email
        emailTemplateProvider.send("net.ihe.gazelle.gum.accountCreatedSubject", "account-created.ftl", attributes);
    }

}
