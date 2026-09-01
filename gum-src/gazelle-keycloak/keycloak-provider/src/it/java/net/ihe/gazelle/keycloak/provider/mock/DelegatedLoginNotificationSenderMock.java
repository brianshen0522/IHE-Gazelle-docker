package net.ihe.gazelle.keycloak.provider.mock;

import net.ihe.gazelle.keycloak.provider.interlay.notification.DelegatedLoginNotificationSender;
import org.keycloak.email.EmailException;

public class DelegatedLoginNotificationSenderMock implements DelegatedLoginNotificationSender {
    @Override
    public void notifyDelegatedCannotLoginLocally(String rootTestBedUrl, String idp) throws EmailException {
        //nothing to do
    }
}
