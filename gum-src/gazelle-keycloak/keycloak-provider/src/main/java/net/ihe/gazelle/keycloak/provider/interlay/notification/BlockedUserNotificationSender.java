package net.ihe.gazelle.keycloak.provider.interlay.notification;

import org.keycloak.email.EmailException;

public interface BlockedUserNotificationSender {

    /**
     * Send email to notify that the account is now blocked
     * @param rootTestBedUrl the url of the test bed instance
     * @param keycloakBaseUrl the url of keycloak
     * @param clientId the id of the client
     * @throws EmailException if the email could not be sent
     */
    void sendBlockedAccount(String rootTestBedUrl, String keycloakBaseUrl, String clientId) throws EmailException;
}
