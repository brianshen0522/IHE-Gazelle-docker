package net.ihe.gazelle.keycloak.provider.interlay.notification;

import org.keycloak.email.EmailException;

public interface InactiveUserNotificationSender {
    /**
     * Email a user without vendor admin group to notify that the account is inactive
     *
     * @param rootTestBedUrl: the root of the url testbed
     * @throws EmailException if email cannot be sent
     */
    void sendInactiveAccountVendor(String rootTestBedUrl) throws EmailException;

    /**
     * Email a user with vendor admin group to notify that the account is inactive
     *
     * @param rootTestBedUrl: the root of the url testbed
     * @param activationLink: the link to activate the user account
     * @throws EmailException if email cannot be sent
     */
    void sendInactiveAccountVendorAdmin(String rootTestBedUrl, String activationLink) throws EmailException;
}
