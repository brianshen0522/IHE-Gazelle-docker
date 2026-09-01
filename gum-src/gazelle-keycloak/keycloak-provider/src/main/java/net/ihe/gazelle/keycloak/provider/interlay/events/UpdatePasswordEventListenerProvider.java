package net.ihe.gazelle.keycloak.provider.interlay.events;

import net.ihe.gazelle.keycloak.provider.interlay.notification.UserEditNotificationSenderImpl;
import net.ihe.gazelle.keycloak.provider.interlay.service.UpdatePasswordEventService;
import net.ihe.gazelle.keycloak.provider.interlay.service.UpdatePasswordEventServiceImpl;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdatePasswordEventListenerProvider extends GazelleEventListenerProvider {
    private static final Logger log = LoggerFactory.getLogger(UpdatePasswordEventListenerProvider.class);

    public UpdatePasswordEventListenerProvider(KeycloakSession keycloakSession) {
        super(keycloakSession);
    }

    @Override
    public void onEvent(Event event) {
        if ((EventType.UPDATE_PASSWORD.equals(event.getType()) || EventType.UPDATE_CREDENTIAL.equals(event.getType())) && event.getUserId() != null) {
            log.trace("UpdatePasswordEventListenerProvider.onEvent()");
            RealmModel realm = getKeycloakSession().getContext().getRealm();
            UserModel user = getKeycloakSession().users().getUserById(realm, event.getUserId());
            UpdatePasswordEventService updatePasswordEventService = new UpdatePasswordEventServiceImpl();
            UserEditNotificationSenderImpl userEditNotificationSender = new UserEditNotificationSenderImpl(getKeycloakSession(), realm, user);
            // Call service to send email
            updatePasswordEventService.eventUpdatePassword(userEditNotificationSender);
        }
    }
}
