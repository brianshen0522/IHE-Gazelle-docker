package net.ihe.gazelle.keycloak.provider.interlay.service;

import net.ihe.gazelle.keycloak.provider.interlay.notification.BlockedUserNotificationSender;
import org.keycloak.events.Event;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public interface BlockedAccountEventService {

    void eventLoginOnBlockedAccount(RealmModel realm, UserModel user, Event event, BlockedUserNotificationSender blockedUserNotificationSender);
}
