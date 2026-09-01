package net.ihe.gazelle.keycloak.provider.interlay.service;

import net.ihe.gazelle.keycloak.provider.interlay.notification.InactiveUserNotificationSender;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public interface InactiveUserEventService {

    /**
     * Manage login events on inactive accounts
     * @param realm the current realm
     * @param user the user
     * @param userLookupService the user lookup service
     * @param inactiveUserNotificationSender the notification sender
     */
    void eventLoginOnInactiveAccount(RealmModel realm, UserModel user, UserLookupService userLookupService, InactiveUserNotificationSender inactiveUserNotificationSender);

    /**
     * Manage login events on delegated and inactive accounts
     * @param userModel the user
     * @param userDelegationService the user delegation service
     */
    void eventLoginOnDelegatedInactiveAccount(UserModel userModel, UserDelegationService userDelegationService);
}
