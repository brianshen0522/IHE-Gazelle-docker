package net.ihe.gazelle.keycloak.provider.interlay.events;

import net.ihe.gazelle.keycloak.provider.interlay.service.LastLoginEventServiceImpl;
import net.ihe.gazelle.user.management.api.application.user.login.UserLoginService;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class LastLoginEventListenerProvider extends GazelleEventListenerProvider {

    private final UserLoginService userLoginService;

    public LastLoginEventListenerProvider(KeycloakSession keycloakSession, UserLoginService userLoginService) {
        super(keycloakSession);
        this.userLoginService = userLoginService;
    }

    @Override
    public void onEvent(Event event) {
        if (EventType.LOGIN.equals(event.getType())) {
            RealmModel realm = getKeycloakSession().getContext().getRealm();
            UserModel user = getKeycloakSession().users().getUserById(realm, event.getUserId());
            LastLoginEventServiceImpl lastLoginEventService = new LastLoginEventServiceImpl();
            // Call service to update last login
            lastLoginEventService.eventLoginSuccessful(user, userLoginService);
        }
    }
}
