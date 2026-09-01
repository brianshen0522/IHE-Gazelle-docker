package net.ihe.gazelle.keycloak.provider.interlay.notification;

import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.HashMap;
import java.util.Map;

public class DelegatedLoginNotificationSenderImpl implements DelegatedLoginNotificationSender {
    private final EmailTemplateProvider emailTemplateProvider;
    private final UserModel recipient;

    public DelegatedLoginNotificationSenderImpl(KeycloakSession session, RealmModel realm, UserModel recipient) {
        emailTemplateProvider = new FreeMarkerEmailTemplateProvider(session)
                .setRealm(realm).setUser(recipient);
        this.recipient = recipient;
    }

    @Override
    public void notifyDelegatedCannotLoginLocally(String rootTestBedUrl, String idp) throws EmailException {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("firstName", recipient.getFirstName());
        attributes.put("lastName", recipient.getLastName());
        attributes.put("testBedUrl", rootTestBedUrl);
        attributes.put("email", recipient.getEmail());
        attributes.put("idp", idp);
        emailTemplateProvider.send("net.ihe.gazelle.gum.accountDelegatedSubject", "account-delegated.ftl", attributes);
    }

}
