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

public class BlockedUserNotificationSenderImpl implements BlockedUserNotificationSender {

    private final EmailTemplateProvider emailTemplateProvider;
    private final UserModel recipient;
    private final ApplicationConfig applicationConfig;

    public BlockedUserNotificationSenderImpl(KeycloakSession session, RealmModel realm, UserModel recipient) {
        emailTemplateProvider = new FreeMarkerEmailTemplateProvider(session)
                .setRealm(realm).setUser(recipient);
        this.recipient = recipient;
        this.applicationConfig = new ApplicationConfigImpl();
    }

    /**
     * Email a user to notify that their account is blocked and provides a link to reset the password
     *
     * @param rootTestBedUrl:  the root of the url testbed
     * @param keycloakBaseUrl: the base url of Keycloak
     * @param clientId:        the client id from where the user tries to log
     * @throws EmailException if email cannot be sent
     */
    @Override
    public void sendBlockedAccount(String rootTestBedUrl, String keycloakBaseUrl, String clientId) throws EmailException {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("firstName", recipient.getFirstName());
        attributes.put("lastName", recipient.getLastName());
        attributes.put("testBedUrl", rootTestBedUrl);
        attributes.put("email", recipient.getEmail());
        attributes.put("resetPasswordLink", keycloakBaseUrl + "/realms/" + applicationConfig.getRealmName()
                + "/login-actions/reset-credentials?client_id=" + clientId);
        // Send email
        emailTemplateProvider.send("net.ihe.gazelle.gum.accountBlockedSubject", "email-blocked.ftl", attributes);
    }


}
