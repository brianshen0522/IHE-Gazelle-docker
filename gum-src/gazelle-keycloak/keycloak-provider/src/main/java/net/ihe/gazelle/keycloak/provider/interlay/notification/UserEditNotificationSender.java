package net.ihe.gazelle.keycloak.provider.interlay.notification;

import org.keycloak.email.EmailException;

public interface UserEditNotificationSender {
    void notifyPasswordUpdated(String rootTestBedUrl) throws EmailException;
}
