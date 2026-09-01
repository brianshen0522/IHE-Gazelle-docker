package net.ihe.gazelle.keycloak.provider.interlay.events;

import net.ihe.gazelle.keycloak.core.interlay.configuration.ApplicationConfigImpl;
import net.ihe.gazelle.keycloak.provider.interlay.notification.InactiveUserNotificationSenderImpl;
import net.ihe.gazelle.keycloak.provider.interlay.service.InactiveUserEventService;
import net.ihe.gazelle.keycloak.provider.interlay.service.InactiveUserEventServiceImpl;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import org.keycloak.events.Errors;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InactiveLoginEventListenerProvider extends GazelleEventListenerProvider {
    private static final Logger log = LoggerFactory.getLogger(InactiveLoginEventListenerProvider.class);
    private final UserLookupService userLookupService;
    private final UserDelegationService userDelegationService;

    public InactiveLoginEventListenerProvider(KeycloakSession keycloakSession, UserLookupService userLookupService, UserDelegationService userDelegationService) {
        super(keycloakSession);
        this.userLookupService = userLookupService;
        this.userDelegationService = userDelegationService;
    }

    @Override
    public void onEvent(Event event) {
        if (shouldHandleEvent(event)) {
            log.debug("InactiveLoginEventListenerProvider.onEvent()");
            RealmModel realm = getKeycloakSession().getContext().getRealm();
            UserModel userModel = getKeycloakSession().users().getUserById(realm, event.getUserId());
            ApplicationConfig applicationConfig = new ApplicationConfigImpl();
            InactiveUserEventService inactiveUserEventService = new InactiveUserEventServiceImpl(applicationConfig);
            InactiveUserNotificationSenderImpl inactiveUserNotificationSenderImpl = new InactiveUserNotificationSenderImpl(getKeycloakSession(), realm, userModel);

            // Call service to send email
            inactiveUserEventService.eventLoginOnInactiveAccount(realm, userModel, userLookupService, inactiveUserNotificationSenderImpl);
            inactiveUserEventService.eventLoginOnDelegatedInactiveAccount(userModel, userDelegationService);
        }
    }

    private boolean shouldHandleEvent(Event event) {
        return EventType.LOGIN_ERROR.equals(event.getType()) && !event.getError().equals(Errors.INVALID_USER_CREDENTIALS) && event.getUserId() != null;
    }
}
