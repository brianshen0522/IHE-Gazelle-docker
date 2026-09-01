package net.ihe.gazelle.keycloak.provider.interlay.service;

import net.ihe.gazelle.keycloak.provider.interlay.notification.UserCreationNotificationSender;

/**
 * Service interface for handling account creation events.
 *
 * This service provides methods to process and respond to account creation
 * events, particularly when users are created by administrators in the
 * Keycloak system. It handles the necessary notifications and business logic
 * associated with user account creation.
 *
 */
public interface AccountCreationEventService {

    /**
     * Handles the event when an account is created by an administrator.
     *
     * This method processes the account creation event and triggers appropriate
     * notifications to inform relevant parties about the new user account.
     *
     * @param userCreationNotificationSender the notification sender for user creation events
     */
    void eventAccountCreationByAdmin(UserCreationNotificationSender userCreationNotificationSender);
}
