package net.ihe.gazelle.keycloak.provider.interlay.service;

import net.ihe.gazelle.keycloak.provider.interlay.notification.DelegatedLoginNotificationSender;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import org.keycloak.events.Event;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public interface DelegatedLoginEventService {
    void eventLoginOnDelegatedAccount(RealmModel realm, UserModel userModel, Event event,
                                      UserDelegationService userDelegationService, DelegatedLoginNotificationSender delegatedLoginNotificationSender);
}
