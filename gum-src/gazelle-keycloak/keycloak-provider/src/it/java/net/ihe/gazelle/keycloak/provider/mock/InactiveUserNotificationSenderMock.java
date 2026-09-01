package net.ihe.gazelle.keycloak.provider.mock;

import net.ihe.gazelle.keycloak.core.application.event.GazelleEventException;
import net.ihe.gazelle.keycloak.provider.interlay.notification.InactiveUserNotificationSender;
import org.keycloak.email.EmailException;

public class InactiveUserNotificationSenderMock implements InactiveUserNotificationSender {
    @Override
    public void sendInactiveAccountVendor(String rootTestBedUrl) throws EmailException {
        //nothing to do
    }

    @Override
    public void sendInactiveAccountVendorAdmin(String rootTestBedUrl, String activationLink) throws EmailException {
        throw new GazelleEventException(activationLink);
    }
}
