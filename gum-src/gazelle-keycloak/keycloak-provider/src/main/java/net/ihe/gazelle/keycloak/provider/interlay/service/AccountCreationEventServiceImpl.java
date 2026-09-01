package net.ihe.gazelle.keycloak.provider.interlay.service;

import net.ihe.gazelle.keycloak.core.application.event.GazelleEventException;
import net.ihe.gazelle.keycloak.provider.interlay.notification.UserCreationNotificationSender;
import org.keycloak.email.EmailException;

/**
 * Implementation of the AccountCreationEventService interface.
 *
 * This service implementation handles the processing of account creation events
 * by triggering appropriate notifications when users are created by administrators.
 * It manages error handling and ensures that notification failures are properly
 * reported through exceptions.
 *
 */
public class AccountCreationEventServiceImpl implements AccountCreationEventService {

    /**
     * Default constructor.
     *
     * Creates a new instance of AccountCreationEventServiceImpl.
     */
    public AccountCreationEventServiceImpl() {
        // Default constructor
    }

    @Override
    public void eventAccountCreationByAdmin(UserCreationNotificationSender userCreationNotificationSender) {
        if (userCreationNotificationSender == null)
            throw new IllegalArgumentException("Email sender is null");
        try {
            userCreationNotificationSender.sendAccountCreatedByAdmin();
        } catch (EmailException e) {
            throw new GazelleEventException("Unable to send email for account creation", e);
        }
    }
}
