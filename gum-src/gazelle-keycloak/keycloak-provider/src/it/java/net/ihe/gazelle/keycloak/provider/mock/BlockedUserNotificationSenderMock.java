package net.ihe.gazelle.keycloak.provider.mock;

import net.ihe.gazelle.keycloak.provider.interlay.notification.BlockedUserNotificationSender;
import org.keycloak.email.EmailException;

public class BlockedUserNotificationSenderMock implements BlockedUserNotificationSender {
    @Override
    public void sendBlockedAccount(String rootTestBedUrl, String keycloakBaseUrl, String clientId) throws EmailException {
        //nothing to do
    }
}
