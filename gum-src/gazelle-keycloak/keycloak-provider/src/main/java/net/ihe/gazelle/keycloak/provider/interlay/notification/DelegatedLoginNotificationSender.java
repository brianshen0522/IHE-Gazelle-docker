package net.ihe.gazelle.keycloak.provider.interlay.notification;

import org.keycloak.email.EmailException;

public interface DelegatedLoginNotificationSender {
    /**
     * Email a user to notify that his account is delegated, and he must log in with his external IDP
     *
     * @param rootTestBedUrl: the root of the url testbed
     * @param idp:            the name of the idp of the delegated user
     * @throws EmailException if email cannot be sent
     */
    void notifyDelegatedCannotLoginLocally(String rootTestBedUrl, String idp) throws EmailException;
}
