package net.ihe.gazelle.keycloak.provider.interlay.events;

import net.ihe.gazelle.keycloak.core.interlay.configuration.ApplicationConfigImpl;
import net.ihe.gazelle.keycloak.provider.interlay.notification.DelegatedLoginNotificationSenderImpl;
import net.ihe.gazelle.keycloak.provider.interlay.service.DelegatedLoginEventServiceImpl;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelegatedLoginEventListenerProvider extends GazelleEventListenerProvider {
    private static final Logger log = LoggerFactory.getLogger(DelegatedLoginEventListenerProvider.class);
    private final UserDelegationService userDelegationService;

    public DelegatedLoginEventListenerProvider(KeycloakSession keycloakSession,
                                               UserDelegationService userDelegationService) {
        super(keycloakSession);
        this.userDelegationService = userDelegationService;
    }

    @Override
    public void onEvent(Event event) {
        // If the event is a login error or a login
        if (event.getUserId() != null && (EventType.LOGIN_ERROR.equals(event.getType()) || EventType.LOGIN.equals(event.getType()))) {
            log.trace("DelegatedLoginEventListenerProvider.onEvent()");

            RealmModel realm = getKeycloakSession().getContext().getRealm();
            UserModel userModel = getKeycloakSession().users().getUserById(realm, event.getUserId());
            ApplicationConfig applicationConfig = new ApplicationConfigImpl();
            DelegatedLoginEventServiceImpl delegatedLoginEventService = new DelegatedLoginEventServiceImpl(getKeycloakSession(), applicationConfig);

            // Call service to send email or to set activated to true to the delegated user
            delegatedLoginEventService.
                    eventLoginOnDelegatedAccount(realm, userModel, event,
                            userDelegationService,
                            new DelegatedLoginNotificationSenderImpl(getKeycloakSession(), realm, userModel));
        }
    }
}
