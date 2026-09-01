package net.ihe.gazelle.keycloak.provider.interlay.events;

import net.ihe.gazelle.keycloak.core.interlay.configuration.ApplicationConfigImpl;
import net.ihe.gazelle.keycloak.provider.interlay.notification.BlockedUserNotificationSenderImpl;
import net.ihe.gazelle.keycloak.provider.interlay.service.BlockedAccountEventService;
import net.ihe.gazelle.keycloak.provider.interlay.service.BlockedAccountEventServiceImpl;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockLoginEventListenerProvider extends GazelleEventListenerProvider {

    private static final Logger log = LoggerFactory.getLogger(BlockLoginEventListenerProvider.class);

    public BlockLoginEventListenerProvider(KeycloakSession keycloakSession) {
        super(keycloakSession);
    }

    @Override
    public void onEvent(Event event) {
        // If the event is a login error or a login
        if (event.getUserId() != null && (EventType.LOGIN_ERROR.equals(event.getType()) || EventType.LOGIN.equals(event.getType()))) {
            log.trace("BlockLoginEventListenerProvider.onEvent()");

            RealmModel realm = getKeycloakSession().getContext().getRealm();
            UserModel userModel = getKeycloakSession().users().getUserById(realm, event.getUserId());
            ApplicationConfig applicationConfig = new ApplicationConfigImpl();
            BlockedAccountEventService blockedAccountEventService = new BlockedAccountEventServiceImpl(getKeycloakSession(), applicationConfig);
            BlockedUserNotificationSenderImpl blockedUserNotificationSender = new BlockedUserNotificationSenderImpl(getKeycloakSession(), realm, userModel);

            blockedAccountEventService.eventLoginOnBlockedAccount(realm, userModel, event, blockedUserNotificationSender);
        }
    }
}
