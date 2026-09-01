package net.ihe.gazelle.keycloak.provider.interlay.notification;

import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.HashMap;
import java.util.Map;

public class UserEditNotificationSenderImpl implements UserEditNotificationSender {

    private final EmailTemplateProvider emailTemplateProvider;
    private final UserModel recipient;

    public UserEditNotificationSenderImpl(KeycloakSession session, RealmModel realm, UserModel recipient) {
        emailTemplateProvider = new FreeMarkerEmailTemplateProvider(session)
                .setRealm(realm).setUser(recipient);
        this.recipient = recipient;
    }

    /**
     * Email a user to notify that their password has been updated
     * @param rootTestBedUrl: the root of the url testbed
     * @throws EmailException if email cannot be sent
     */
    @Override
    public void notifyPasswordUpdated(String rootTestBedUrl) throws EmailException {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("firstName", recipient.getFirstName());
        attributes.put("lastName", recipient.getLastName());
        attributes.put("testBedUrl", rootTestBedUrl);
        attributes.put("email", recipient.getEmail());


        emailTemplateProvider.send(
                "net.ihe.gazelle.gum.passwordUpdatedSubject",
                "password-updated.ftl",
                attributes);
    }


}
