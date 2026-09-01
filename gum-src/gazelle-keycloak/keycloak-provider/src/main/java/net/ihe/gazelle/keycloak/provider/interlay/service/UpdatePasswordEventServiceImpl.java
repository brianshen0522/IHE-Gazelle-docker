package net.ihe.gazelle.keycloak.provider.interlay.service;

import net.ihe.gazelle.keycloak.core.application.event.GazelleEventException;
import net.ihe.gazelle.keycloak.core.interlay.configuration.ApplicationConfigImpl;
import net.ihe.gazelle.keycloak.provider.interlay.notification.UserEditNotificationSender;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import org.keycloak.email.EmailException;

public class UpdatePasswordEventServiceImpl implements UpdatePasswordEventService {

    @Override
    public void eventUpdatePassword(UserEditNotificationSender userEditNotificationSender) {
        if (userEditNotificationSender == null)
            throw new IllegalArgumentException("Email sender is null");
        ApplicationConfig applicationConfig = new ApplicationConfigImpl();
        try {
            userEditNotificationSender.notifyPasswordUpdated(applicationConfig.getRootTestBedUrl());
        } catch (EmailException e) {
            throw new GazelleEventException("Unable to send email for updated password", e);
        }
    }
}
