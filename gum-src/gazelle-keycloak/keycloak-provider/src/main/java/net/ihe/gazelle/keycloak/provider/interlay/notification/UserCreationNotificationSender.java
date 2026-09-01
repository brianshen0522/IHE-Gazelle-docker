package net.ihe.gazelle.keycloak.provider.interlay.notification;

import org.keycloak.email.EmailException;

public interface UserCreationNotificationSender {
    /**
     * Email a user to notify that their account has been created by an admin
     *
     * @throws EmailException if email cannot be sent
     */
    void sendAccountCreatedByAdmin() throws EmailException;
}
