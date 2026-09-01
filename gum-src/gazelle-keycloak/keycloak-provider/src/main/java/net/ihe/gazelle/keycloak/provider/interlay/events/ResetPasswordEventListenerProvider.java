package net.ihe.gazelle.keycloak.provider.interlay.events;

import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionProvider;

public class ResetPasswordEventListenerProvider extends GazelleEventListenerProvider {

    public ResetPasswordEventListenerProvider(KeycloakSession keycloakSession) {
        super(keycloakSession);
    }

    @Override
    public void onEvent(Event event) {
        // If the user has reset his password, we need to remove all his sessions
        if (EventType.SEND_RESET_PASSWORD.equals(event.getType())) {
            RealmModel realm = getKeycloakSession().getContext().getRealm();
            UserModel user =  getKeycloakSession().users().getUserById(realm, event.getUserId());
            if (user != null) {
                UserSessionProvider sessions = getKeycloakSession().sessions();
                sessions.removeUserSessions(realm, user);
            }
        }
    }
}
