package net.ihe.gazelle.keycloak.provider.mock;

import net.ihe.gazelle.keycloak.provider.interlay.notification.UserEditNotificationSender;
import org.keycloak.email.EmailException;

public class UserEditNotificationSenderMock implements UserEditNotificationSender {
    @Override
    public void notifyPasswordUpdated(String rootTestBedUrl) throws EmailException {
        //nothing to do
    }
}
