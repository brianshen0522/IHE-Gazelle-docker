package net.ihe.gazelle.keycloak.provider.interlay.service;

import net.ihe.gazelle.keycloak.core.application.event.GazelleEventException;
import net.ihe.gazelle.keycloak.provider.interlay.notification.DelegatedLoginNotificationSender;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.api.domain.user.DelegatedUser;
import org.keycloak.email.EmailException;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class DelegatedLoginEventServiceImpl implements DelegatedLoginEventService {
    private static final String NOTIFIED_DELEGATED_ACCOUNT = "NOTIFIED_DELEGATED_ACCOUNT";

    private final KeycloakSession session;
    private final ApplicationConfig applicationConfig;

    public DelegatedLoginEventServiceImpl(KeycloakSession session, ApplicationConfig applicationConfig) {
        this.session = session;
        this.applicationConfig = applicationConfig;
    }

    @Override
    public void eventLoginOnDelegatedAccount(RealmModel realm, UserModel userModel, Event event,
                                             UserDelegationService userDelegationService, DelegatedLoginNotificationSender delegatedLoginNotificationSender) {
        if (session == null || userModel == null || realm == null || event == null || userDelegationService == null || delegatedLoginNotificationSender == null)
            throw new IllegalArgumentException("One of the parameters is null");

        FederatedIdentityModel federatedIdentity = session.users()
                .getFederatedIdentitiesStream(realm, userModel)
                .findFirst()
                .orElse(null);

        if (federatedIdentity != null) {
            DelegatedUser user = userDelegationService.getDelegatedUserById(userModel.getUsername());

            String alias = federatedIdentity.getIdentityProvider();
            String displayName = realm.getIdentityProviderByAlias(alias).getDisplayName();
            // If the user is delegated and was not already notified at last login attempt
            if (user != null && !userModel.getAttributes().containsKey(NOTIFIED_DELEGATED_ACCOUNT) && isLoginError(event)) {
                try {
                    delegatedLoginNotificationSender.notifyDelegatedCannotLoginLocally(applicationConfig.getRootTestBedUrl(), displayName);

                    // Set notified delegated account attribute
                    userModel.setSingleAttribute(NOTIFIED_DELEGATED_ACCOUNT, "true");
                } catch (EmailException e) {
                    throw new GazelleEventException("Unable to send email for delegated account", e);
                }
            }
            if (user != null && isLoginSuccess(event)) {
                // Catch login event to reset notified delegated account attribute
                userModel.removeAttribute(NOTIFIED_DELEGATED_ACCOUNT);
            }
        }
    }

    private boolean isLoginSuccess(Event event) {
        return EventType.LOGIN.equals(event.getType());
    }

    private boolean isLoginError(Event event) {
        return EventType.LOGIN_ERROR.equals(event.getType());
    }


}
