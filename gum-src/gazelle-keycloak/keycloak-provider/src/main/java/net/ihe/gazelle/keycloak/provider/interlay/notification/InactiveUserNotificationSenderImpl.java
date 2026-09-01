package net.ihe.gazelle.keycloak.provider.interlay.notification;

import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InactiveUserNotificationSenderImpl implements InactiveUserNotificationSender {

    private final EmailTemplateProvider emailTemplateProvider;
    private final UserModel recipient;

    public InactiveUserNotificationSenderImpl(KeycloakSession session, RealmModel realm, UserModel recipient) {
        emailTemplateProvider = new FreeMarkerEmailTemplateProvider(session)
                .setRealm(realm).setUser(recipient);
        this.recipient = recipient;
    }

    @Override
    public void sendInactiveAccountVendor(String rootTestBedUrl) throws EmailException {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("firstName", recipient.getFirstName());
        attributes.put("lastName", recipient.getLastName());
        attributes.put("testBedUrl", rootTestBedUrl);
        attributes.put("email", recipient.getEmail());

        Optional<GroupModel> optionalGroupModel = recipient.getGroupsStream().findFirst();
        if (optionalGroupModel.isEmpty()) {
            attributes.put("organization", "no group found");
        } else {
            attributes.put("organization", optionalGroupModel.get().getName());
        }

        emailTemplateProvider.send(
                "net.ihe.gazelle.gum.accountInactiveSubject",
                "email-inactive-vendor.ftl",
                attributes);
    }

    @Override
    public void sendInactiveAccountVendorAdmin(String rootTestBedUrl, String activationLink) throws EmailException {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("firstName", recipient.getFirstName());
        attributes.put("lastName", recipient.getLastName());
        attributes.put("testBedUrl", rootTestBedUrl);
        attributes.put("email", recipient.getEmail());

        attributes.put("activationLink", activationLink);

        emailTemplateProvider.send(
                "net.ihe.gazelle.gum.accountInactiveSubject",
                "email-inactive-vendor-admin.ftl",
                attributes);
    }

}
