package net.ihe.gazelle.keycloak.provider.interlay.service;

import net.ihe.gazelle.keycloak.core.application.event.GazelleEventException;
import net.ihe.gazelle.keycloak.provider.interlay.notification.BlockedUserNotificationSender;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import org.keycloak.email.EmailException;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.BruteForceProtector;

public class BlockedAccountEventServiceImpl implements BlockedAccountEventService {
    private static final String NOTIFIED_BLOCKED_ACCOUNT = "NOTIFIED_BLOCKED_ACCOUNT";
    private final KeycloakSession session;
    private final ApplicationConfig applicationConfig;

    public BlockedAccountEventServiceImpl(KeycloakSession session, ApplicationConfig applicationConfig) {
        this.session = session;
        this.applicationConfig = applicationConfig;
    }

    @Override
    public void eventLoginOnBlockedAccount(RealmModel realm, UserModel user, Event event, BlockedUserNotificationSender blockedUserNotificationSender) {
        if (user == null || realm == null || event == null || blockedUserNotificationSender == null)
            throw new IllegalArgumentException("One of the parameters is null");
        // If the event is a login error
        if (isLoginError(event)) {
            // If the user is temporarily disabled and was not already disabled at last login attempt
            if (isTemporarilyDisabled(session, realm, user) && !user.getAttributes().containsKey(NOTIFIED_BLOCKED_ACCOUNT)) {
                try {
                    blockedUserNotificationSender.sendBlockedAccount(
                            applicationConfig.getRootTestBedUrl(),
                            applicationConfig.getSSOBaseUrl(),
                            event.getClientId());

                    // Set notified blocked account attribute
                    user.setSingleAttribute(NOTIFIED_BLOCKED_ACCOUNT, "true");
                } catch (EmailException e) {
                    throw new GazelleEventException("Unable to send email for blocked account", e);
                }
            }
        } else if (isLoginSuccess(event)) {
            // Catch login event to reset notified blocked account attribute
            user.removeAttribute(NOTIFIED_BLOCKED_ACCOUNT);
        }
    }

    private boolean isLoginError(Event event) {
        return EventType.LOGIN_ERROR.equals(event.getType());
    }

    private boolean isLoginSuccess(Event event) {
        return EventType.LOGIN.equals(event.getType());
    }


    private boolean isTemporarilyDisabled(KeycloakSession session, RealmModel realm, UserModel userModel) {
        return session.getProvider(BruteForceProtector.class).isTemporarilyDisabled(session, realm, userModel);
    }

}
