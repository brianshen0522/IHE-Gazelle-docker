package net.ihe.gazelle.keycloak.provider.interlay.service;

import net.ihe.gazelle.keycloak.provider.interlay.notification.UserEditNotificationSender;

public interface UpdatePasswordEventService {
    void eventUpdatePassword(UserEditNotificationSender userEditNotificationSender);
}
